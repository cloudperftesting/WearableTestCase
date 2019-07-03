'use strict';

const express = require('express');
const {Datastore} = require('@google-cloud/datastore');
const ds = new Datastore();
const app = express();

app.set("view engine", "ejs");

app.get('/', (req, res) => {
    res.render("index");
});

// [START update]
app.post('/:user/:day/:hour/:step/', async (req, res, next) => {
   const userID = req.params.user;
   const day = req.params.day;
   const hour = req.params.hour;
   const step = req.params.step;
   //console.log("user: "+ userID);
   //console.log("day: "+ day);
   //console.log("hour: "+ hour);
   //console.log("step: "+ step);
   const stepI = parseInt(step);
   const hourI = parseInt(hour);
   const dayI = parseInt(day);
   if (dayI < 0 || hourI < 0 || stepI < 0 || hourI > 23 || stepI > 5000) {
      res.send('invalid number');
      res.status(400);
      return
   }
   const user_recent_key = ds.key(['RecentDay', userID]);
   const recent_day_value = await ds.get(user_recent_key);
   const unique_key = userID + "#" + day + "#" + hour;
   const step_record_key = ds.key(['StepRecord', unique_key]);
   if (recent_day_value[0] === undefined || recent_day_value[0].recent_day < dayI){
      const recent_day_record = {
         userID: userID,
         recent_day:dayI,
      }
      const step_record = {
         // record for each userID, day, hour, step
         userID: userID,
         day : dayI,
         hour : hourI,
         step : stepI,
      }
      const entities = [
         {
            key:step_record_key,
            data:step_record,
         },
         {
            key: user_recent_key,
            data: recent_day_record,
         },
      ];
      await ds.upsert(entities);
   }
   else{
      const step_record = {
         // record for each userID, day, hour, step
         userID: userID,
         day : dayI,
         hour : hourI,
         step : stepI,
      }
      const step_record_entity = {
         key:step_record_key,
         data:step_record,
      };
      await ds.upsert(step_record_entity);
   }
   res.send("posted");
});
// [End update]

// [Start Current Day Handler]
app.get('/current/:user',async (req, res, next)=>{
   const userID = req.params.user;
   const user_recent_key = ds.key(['RecentDay', userID]);
   const recent_day_value = await ds.get(user_recent_key);
   //console.log(recent_day_value);
   if(recent_day_value[0] === undefined){
      res.send("user "+ userID +" not found");
      return
   }
   const dayI = recent_day_value[0].recent_day;
   // use filter
   const query = ds
   .createQuery('StepRecord')
   .filter('userID', '=', userID)
   .filter('day', '=', dayI);
   const [tasks] = await ds.runQuery(query);
   //console.log('Tasks:');
   //tasks.forEach(task => console.log(task));
   const steps = [];
   tasks.forEach(task => steps.push(task.step));
   //console.log(steps);
   var sum = steps.reduce(function(a, b) { return a + b; }, 0);
   console.log(sum)
   res.send("Total step count on day " + dayI + " for " + userID + " is " + sum);
});
// [End Current Day Handler]

// [Start Single Day Handler]
app.get('/single/:user/:day',async (req, res, next)=>{
   const userID = req.params.user;
   const dayI = parseInt(req.params.day);
   const user_recent_key = ds.key(['RecentDay', userID]);
   const recent_day_record = await ds.get(user_recent_key);
   if(recent_day_record[0] === undefined){
      res.send("user "+ userID + " not found")
      return
   }
   const query = ds
   .createQuery('StepRecord')
   .filter('userID', '=', userID)
   .filter('day', '=', dayI);
   const [tasks] = await ds.runQuery(query);
   //console.log('Tasks:');
   //tasks.forEach(task => console.log(task));
   const steps = [];
   tasks.forEach(task => steps.push(task.step));
   var sum = steps.reduce(function(a, b) { return a + b; }, 0);
   console.log(sum)
   res.send("Total step count on day " + dayI + " for " + userID + " is " + sum);
});
// [End Single Day Handler]

// [Start Range Day Handler]
app.get('/range/:user/:startDay/:numDays',async (req, res, next)=>{
   const userID = req.params.user;
   const start_day = parseInt(req.params.startDay);
   const num_days = parseInt(req.params.numDays);
   const user_recent_key = ds.key(['RecentDay',userID]);
   const recent_day_record = await ds.get(user_recent_key);
   if(recent_day_record[0] === undefined){
      res.send("user "+ userID + " not found")
      return
   }
   const recent_day = recent_day_record[0].recent_day;
   //console.log(recent_day);
   if(start_day > recent_day){
      res.send('start day is later than most recent day')
      return
   }
   const max_day = Math.min(recent_day,start_day + num_days - 1);
   // console.log(start_day);
   // console.log(max_day);
   // const query = ds
   // .createQuery('StepRecord')
   // .filter('userID', '=', userID)
   // .filter('day', '>=', start_day)
   // .filter('day', '<=', max_day);
   //const [tasks] = await ds.runQuery(query);
   //const steps = [];
   //tasks.forEach(task => steps.push(task.step));
   const steps = [];
   for (var i = start_day; i <= max_day; i++) {
      //console.log("i : " + i);
      const query = ds
      .createQuery('StepRecord')
      .filter('userID', '=', userID)
      .filter('day', '=', i);
      const [tasks] = await ds.runQuery(query);
      //console.log('Tasks:');
      //tasks.forEach(task => console.log(task));
      tasks.forEach(task => steps.push(task.step));
   }
   //console.log(steps);
   var sum = steps.reduce(function(a, b) { return a + b; }, 0);
   console.log(sum)
   res.send("Total step count from day " + start_day + " to " + max_day + " for " + userID + " is " + sum);
});
// [End Range Day Handler]


//[Start emptying the database]
app.delete('/delete', async (req, res, next) => {
   const step_record_query = ds.createQuery("StepRecord");
   const [step_records] = await ds.runQuery(step_record_query);
   const recent_day_query = ds.createQuery('RecentDay');
   const [recent_days] = await ds.runQuery(recent_day_query);
   const batch = [];
   console.log(batch);
   for(let i in step_records){
      batch.push(step_records[i][ds.KEY]);
   }
   for(let i in recent_days){
      batch.push(recent_days[i][ds.KEY]);
   }
   await ds.delete(batch);
   res.send("deleted");
});
// [End emptying the database]
if (module === require.main) {
   // [START server]
   // Start the server
   const server = app.listen(process.env.PORT || 8030, () => {
      const port = server.address().port;
      console.log(`App listening on port ${port}`);
   });
   // [END server]
}

module.exports = app;