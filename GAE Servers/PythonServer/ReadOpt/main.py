# https://cloud.google.com/endpoints/docs/frameworks/python/get-started-frameworks-python
import json
import logging

import webapp2
from google.appengine.ext import ndb


class StepRecord(ndb.Model):
    h0 = ndb.IntegerProperty(default=0, indexed=False)
    h1 = ndb.IntegerProperty(default=0, indexed=False)
    h2 = ndb.IntegerProperty(default=0, indexed=False)
    h3 = ndb.IntegerProperty(default=0, indexed=False)
    h4 = ndb.IntegerProperty(default=0, indexed=False)
    h5 = ndb.IntegerProperty(default=0, indexed=False)
    h6 = ndb.IntegerProperty(default=0, indexed=False)
    h7 = ndb.IntegerProperty(default=0, indexed=False)
    h8 = ndb.IntegerProperty(default=0, indexed=False)
    h9 = ndb.IntegerProperty(default=0, indexed=False)
    h10 = ndb.IntegerProperty(default=0, indexed=False)
    h11 = ndb.IntegerProperty(default=0, indexed=False)
    h12 = ndb.IntegerProperty(default=0, indexed=False)
    h13 = ndb.IntegerProperty(default=0, indexed=False)
    h14 = ndb.IntegerProperty(default=0, indexed=False)
    h15 = ndb.IntegerProperty(default=0, indexed=False)
    h16 = ndb.IntegerProperty(default=0, indexed=False)
    h17 = ndb.IntegerProperty(default=0, indexed=False)
    h18 = ndb.IntegerProperty(default=0, indexed=False)
    h19 = ndb.IntegerProperty(default=0, indexed=False)
    h20 = ndb.IntegerProperty(default=0, indexed=False)
    h21 = ndb.IntegerProperty(default=0, indexed=False)
    h22 = ndb.IntegerProperty(default=0, indexed=False)
    h23 = ndb.IntegerProperty(default=0, indexed=False)
    total = ndb.IntegerProperty(default=0, indexed=False)

    def sum(self):
        return sum([getattr(self, name) for name in self._properties])


class UpdateRecord(ndb.Model):
    mostRecentDay = ndb.StringProperty(required=True, indexed=False)


class UpdateHandler(webapp2.RequestHandler):
    def post(self, userID, day, hour, step):
        self.response.headers['Content-Type'] = 'text/plain'
        step = int(step)
        if int(day) < 0 or int(hour) < 0 or step < 0 or int(hour) > 23 or step > 5000:
            self.response.write('invalid number')
            self.response.set_status(400)

        update_record_key = ndb.Key(UpdateRecord, userID)
        hour_property = 'h' + hour
        step_total = 'total'
        unique_key = userID + '#' + day
        step_record_key = ndb.Key(StepRecord, unique_key)
        update_record, step_record = ndb.get_multi([update_record_key, step_record_key])
        try:
            previous_step_count = getattr(step_record, hour_property, step)
            if previous_step_count and previous_step_count > 0:
                setattr(step_record, step_total, step_record.total - previous_step_count)
        except:
            previous_step_count = 0
        batch = []
        most_recent_day = int(update_record.mostRecentDay) if update_record else -1
        if most_recent_day < int(day):
            update_record = UpdateRecord(key=update_record_key, mostRecentDay=day)
            batch.append(update_record)
        if step_record is None:
            step_record = StepRecord(key=step_record_key)
        setattr(step_record, hour_property, step)
        setattr(step_record, step_total, step_record.total + step)
        batch.append(step_record)
        ndb.put_multi(batch)


class CurrentDayHandler(webapp2.RequestHandler):
    def get(self, userID):
        self.response.headers['Content-Type'] = 'text/plain'
        update_record_key = ndb.Key(UpdateRecord, userID)
        update_record = update_record_key.get()
        if update_record is None:
            self.response.write('user {} not found'.format(userID))
            return
        day = update_record.mostRecentDay
        step_record_key = ndb.Key(StepRecord, userID + '#' + day)
        step_record = step_record_key.get()
        self.response.write('Total step count on day {} for {} is {}'.format(day, userID, step_record.total))


class SingleDayHandler(webapp2.RequestHandler):
    def get(self, userID, day):
        self.response.headers['Content-Type'] = 'text/plain'
        step_record_key = ndb.Key(StepRecord, userID + '#' + day)
        step_record = step_record_key.get()
        if step_record is None:
            update_record_key = ndb.Key(UpdateRecord, userID)
            record = update_record_key.get()
            if record is None:
                self.response.write('user {} not found'.format(userID))
            else:
                self.response.write('no data for day {} of {}'.format(day, userID))
        else:
            self.response.write('Total step count on day {} for {} is {}'.format(day, userID, step_record.total))


class RangeDayHandler(webapp2.RequestHandler):
    def get(self, userID, startDay, numDays):
        self.response.headers['Content-Type'] = 'text/plain'
        update_record_key = ndb.Key(UpdateRecord, userID)
        update_record = update_record_key.get()
        if update_record is None:
            self.response.write('user {} not found'.format(userID))
            return

        start_day, num_days, most_recent_day = int(startDay), int(numDays), int(update_record.mostRecentDay)
        if start_day > most_recent_day:
            self.response.write(
                'start day {} is larger than most recent day {} for {}'.format(startDay, most_recent_day, userID))
            return

        max_day = min(most_recent_day + 1, start_day + num_days)
        day_key_dict = {day: ndb.Key(StepRecord, userID + '#' + str(day)) for day in range(start_day, max_day)}
        # calculate sum for each day
        sums = [stepRecord.sum() if stepRecord else 0 for stepRecord in ndb.get_multi(day_key_dict.values())]
        self.response.write(json.dumps(dict(zip(day_key_dict.keys(), sums))))


class DeleteHandler(webapp2.RequestHandler):
    def delete(self):
        self.response.headers['Content-Type'] = 'text/plain'
        step_records = StepRecord.query().fetch_async()
        update_records = UpdateRecord.query().fetch_async()
        batch = step_records.get_result() + update_records.get_result()
        ndb.delete_multi([entity.key for entity in batch])
        self.response.write('Emptied the data store')


class MainPage(webapp2.RequestHandler):
    def get(self):
        self.response.write(
            '<html><body>'
            '<h1>Usage</h1>'
            '<p>To create/update a step count. Endpoint:/{userID}/{day}/{hour}/{step} Method:POST</p>'
            'e.g. /user1/1/0/42'
            '<p>To retrieve the step count sum for the latest day in the database. Endpoint:/current/{userID} Method:GET</p>'
            'e.g. /current/user1'
            '<p>To retrieve the step count sum for a single day. Endpoint:/single/{userID}/{day} Method:GET</p>'
            'e.g. /single/user1/1'
            '<p>To retrieve the step count sum for a range of days. Endpoint:/range/{userID}/{start_day}/{day_number} Method:GET</p>'
            'e.g. /range/user1/1/3, start day must be earier than the latest day in the database'
            '<p>To empty the database. Endpoint:/delete Method:DELETE</p>'
            '<br />'
            '<p>userID: string e.g. user1, user2</p>'
            '<p>day, start_day, day_number: positive integer number</p>'
            '<p>hour: select an integer from 0 to 23</p>'
            '<p>step: non-negative integer number</p>'
            '<br />'
            '<a href="https://docs.google.com/document/d/1y4u422Btu3qJbLFZcJbS9jrbiVUJHNPL_3fDjUH72rA">Document</a>'
            '</body></html>')


app = webapp2.WSGIApplication([
    ('/', MainPage),
    ('/single/(.*)/([1-9][0-9]*)', SingleDayHandler),
    ('/current/(.*)', CurrentDayHandler),
    ('/range/(.*)/([1-9][0-9]*)/(\d+)', RangeDayHandler),
    ('/(.*)/([1-9][0-9]*)/(\d+)/(\d+)', UpdateHandler),
    ('/delete', DeleteHandler)
], debug=True)
