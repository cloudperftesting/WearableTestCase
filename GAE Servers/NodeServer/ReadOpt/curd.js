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
    //console.log("user: "+userID);
    //console.log("day: "+day);
    //console.log("hour: "+hour);
    //console.log("step: "+step);
    const stepI = parseInt(step);
    const hourI = parseInt(hour);
    const dayI = parseInt(day);
    if (dayI < 0 || hourI < 0 || stepI < 0 || hourI > 23 || stepI > 5000) {
        res.send('invalid number');
        res.status(400);
        return
    }
    const update_record_key = ds.key(['UpdateRecord', userID]);
    const recordUpdate = await ds.get(update_record_key);
    const unique_key = userID + "#" +  day;
    const step_record_key = ds.key(['StepRecord', unique_key]);
    const recordStep = await ds.get(step_record_key);
    //console.log(recordUpdate);
    //console.log(recordStep);
    if(recordUpdate[0] === undefined && recordStep[0] === undefined){
        const step_record = {
            step: new Array(24).fill(0),
            step_sum: 0
        }
        step_record.step[hourI] = stepI;
        step_record.step_sum = stepI;
        const update_record = {
            mostRecentDay:dayI,
        }
        const entities = [
            {
                key:step_record_key,
                data:step_record,
            },
            {
                key:update_record_key,
                data:update_record
            },
        ];
        await ds.upsert(entities);
    }
    else{
        const entities = [];
        //console.log(records[0]);
        //console.log(recordUpdate);
        if(recordUpdate[0].mostRecentDay<dayI){
            recordUpdate[0].mostRecentDay = dayI;
            entities.push(recordUpdate[0]);
        }
        if(recordStep[0] === undefined){
            const step_record = {
                step: new Array(24).fill(0),
                step_sum: 0
            }
            step_record.step[hourI] = stepI;
            step_record.step_sum = stepI;
            const step_record_entity = {
                key:step_record_key,
                data:step_record,
            }
            entities.push(step_record_entity)
        }
        else{
            //console.log(records[1].step[hourI]);
            //console.log(recordStep[0].step);
            if(parseInt(recordStep[0].step[hourI])!=stepI) {
                console.log(recordStep[0]);
                recordStep[0].step_sum = recordStep[0].step_sum + stepI - recordStep[0].step[hourI];
                console.log(recordStep[0].step_sum);
                recordStep[0].step[hourI] = stepI;
                entities.push(recordStep[0]);
            }
        };
        //console.log(entities);
        await ds.upsert(entities);
    };
    res.send('succeeded');
});
// [End update]

// [Start Current Day Handler]
app.get('/current/:user',async (req, res, next)=>{
    const userID = req.params.user;
    //console.log(userID);
    const update_record_key = ds.key(['UpdateRecord', userID]);
    const recordUpdate = await ds.get(update_record_key);
    if(recordUpdate[0]===undefined){
        res.send("user "+userID+" not found");
        return
    }
    const dayI = recordUpdate[0].mostRecentDay;
    const step_record_key = ds.key(['StepRecord',userID+"#"+dayI.toString()]);
    const stepRecord = await ds.get(step_record_key);
    const step_sum = stepRecord[0].step_sum;
    res.send("Total step count on day " + dayI + " for " + userID + " is " + step_sum);
});
// [End Current Day Handler]

// [Start Single Day Handler]
app.get('/single/:user/:day',async (req, res, next)=>{
    const userID = req.params.user;
    const day = req.params.day;
    const unique_key = userID + "#" +  day;
    const step_record_key = ds.key(['StepRecord', unique_key]);
    const stepRecord = await ds.get(step_record_key);
    const step_sum = stepRecord[0].step_sum;
    if (stepRecord === undefined) {
        if (ds.key(['UpdateRecord', userID]) === undefined) {
            res.send("user" + userID + "not found");
        } else {
            res.send("no data for day"+ day + "for" + userID);
        }
    }
    res.send("Total step count on day" + day + "for" + userID + "is" + step_sum);
});
// [End Single Day Handler]

// [Start Range Day Handler]
app.get('/range/:user/:startDay/:numDays',async (req, res, next)=>{
    const userID = req.params.user;
    const update_record_key = ds.key(['UpdateRecord',userID]);
    const updateRecord = await ds.get(update_record_key);
    if(updateRecord[0] === undefined){
        res.send("user "+userID+" not found")
    }
    const startDayI = parseInt(req.params.startDay);
    const numDaysI =parseInt(req.params.numDays);
    const mostRecentDay = updateRecord[0].mostRecentDay;
    if(startDayI>mostRecentDay){
        res.send('start day is larger than most recent day')
    }
    const max_day = Math.min(mostRecentDay+1, startDayI+numDaysI);

    var batch  = new Object();

    for(let i=startDayI; i<max_day; i++){
        const unique_key = ds.key(["StepRecord",userID+ "#" + i.toString()]);
        const stepRecord = await ds.get(unique_key);
        if (stepRecord[0] === undefined) {
            batch[i] = 0;
        } else {
            batch[i] = stepRecord[0].step_sum;
        }
    }
    res.send(JSON.stringify(batch));
});
// [End Range Day Handler]

// [Start emptying the database]
app.delete('/delete', async (req, res, next) => {
    const queryStepRecord = ds.createQuery("StepRecord");
    //console.log(query);
    const [stepRecords] = await ds.runQuery(queryStepRecord);
    const queryUpdateRecord = ds.createQuery('UpdateRecord');
    const [updateRecords] = await ds.runQuery(queryUpdateRecord);
    const batch = [];
    //console.log(entities);

    for(let i in stepRecords){
        //console.log(entities[i]);
        batch.push(stepRecords[i][ds.KEY]);
    }
    for(let i in updateRecords){
        //console.log(entities[i]);
        batch.push(updateRecords[i][ds.KEY]);
    }
    //console.log(batch);
    await ds.delete(batch);
    res.send("delete succeed");
})
// [End emptying the database]
if (module === require.main) {
    // [START server]
    // Start the server
    const server = app.listen(process.env.PORT || 8080, () => {
        const port = server.address().port;
        console.log(`App listening on port ${port}`);
    });
    // [END server]
}

module.exports = app;