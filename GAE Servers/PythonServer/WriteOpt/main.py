# https://cloud.google.com/endpoints/docs/frameworks/python/get-started-frameworks-python
import json

import webapp2
from google.appengine.ext import ndb


class StepRecord(ndb.Model):
    steps = ndb.IntegerProperty(default=0, indexed=False)
    day = ndb.IntegerProperty(default=0, indexed=True)
    userID = ndb.StringProperty(required=True, indexed=True)


class UpdateHandler(webapp2.RequestHandler):
    def post(self, userID, day, hour, step):
        self.response.headers['Content-Type'] = 'text/plain'
        step = int(step)
        if int(day) < 0 or int(hour) < 0 or step < 0 or int(hour) > 23 or step > 5000:
            self.response.write('invalid number')
            self.response.set_status(400)

        unique_key = userID + '#' + day + '#' + hour
        step_record_key = ndb.Key(StepRecord, unique_key)
        step_record = StepRecord(key=step_record_key)
        step_record.steps = step
        step_record.day = int(day)
        step_record.userID = userID
        step_record.put()


class CurrentDayHandler(webapp2.RequestHandler):
    def get(self, userID):
        self.response.headers['Content-Type'] = 'text/plain'
        records = StepRecord.query(StepRecord.userID == userID).fetch()
        #import pdb
        #pdb.set_trace()
        maxDay = 0
        for r in records:
            maxDay = max(maxDay, r.day)
        sum_steps = 0
        for r in records:
            if r.day == maxDay:
                sum_steps += r.steps
        self.response.write('Total step count on day {} for {} is {}'.format(
            maxDay, userID, sum_steps))


class SingleDayHandler(webapp2.RequestHandler):
    def get(self, userID, day):
        day = int(day)
        self.response.headers['Content-Type'] = 'text/plain'
        records = StepRecord.query(
            StepRecord.userID == userID, StepRecord.day == day).fetch()
        if len(records) == 0:
            self.response.write('no data for day {} of {}'.format(day, userID))
            return
        sum_steps = 0
        for r in records:
            sum_steps += r.steps
        self.response.write('Total step count on day {} for {} is {}'.format(
            day, userID, sum_steps))


class RangeDayHandler(webapp2.RequestHandler):
    def get(self, userID, startDay, numDays):
        startDay = int(startDay)
        numDays = int(numDays)
        self.response.headers['Content-Type'] = 'text/plain'
        records = StepRecord.query(
            StepRecord.userID == userID,
            StepRecord.day >= startDay,
            StepRecord.day < startDay + numDays).fetch()
        if len(records) == 0:
            self.response.write('user {} not found'.format(userID))
            return
        days_dict = {}
        for r in records:
            if r.day in days_dict:
                days_dict[r.day] += r.steps
            else:
                days_dict[r.day] = r.steps

        self.response.write(json.dumps(days_dict))


class DeleteHandler(webapp2.RequestHandler):
    def delete(self):
        self.response.headers['Content-Type'] = 'text/plain'
        ndb.delete_multi(
            StepRecord.query().fetch(keys_only=True)
        )
        self.response.write('Emptied the data store')


class MainPage(webapp2.RequestHandler):
    def get(self):
        self.response.write(
            '<html><body>'
            '<h1>Usage:</h1>'
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
