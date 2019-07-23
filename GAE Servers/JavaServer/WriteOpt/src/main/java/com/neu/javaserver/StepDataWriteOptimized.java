/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neu.javaserver;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author igortn
 * This class implements Google datastore operations for our WriteOptimized model
 */
public class StepDataWriteOptimized {
    
      public static String storeData (StepData newStepRecord) {
        
        // connect to datastore
        DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
        
        //create datastore object
        String key =  newStepRecord.getUser() + "#" + Integer.toString(newStepRecord.getDay()) + Integer.toString(newStepRecord.getHour());
        //Key entityKey = KeyFactory.createKey("StepData", key);
        Entity stepCountEntry = new Entity ("StepData", key);
        //Entity stepCountEntry = new Entity ("StepData");
             

        
          // set datastore object values for new step record
        stepCountEntry.setProperty("uid", newStepRecord.getUser() );
        stepCountEntry.setProperty("day", newStepRecord.getDay());
        stepCountEntry.setUnindexedProperty("hour", newStepRecord.getHour());
        stepCountEntry.setUnindexedProperty("count", newStepRecord.getStepCount()   );
        
        // check and if needed update most recent day fo ruser
        Key k = KeyFactory.createKey("RecentDay", newStepRecord.getUser());
        Entity recentDay; 
        boolean updateRecentDay = false;
        try {
            // get the most recent day value for the user
            recentDay = dataStore.get(k); 
        } catch (EntityNotFoundException ex) {
            // new user so store day from request in new entity
            recentDay = new Entity ("RecentDay", newStepRecord.getUser());
            recentDay.setUnindexedProperty("day", newStepRecord.getDay()   );
            updateRecentDay = true;
        }
        
        if (!updateRecentDay) {
            long storedDay = (long) recentDay.getProperty ("day");
            // if new day value > stored day value, overwrite it
            if (storedDay < newStepRecord.getDay() ) {
                 recentDay.setUnindexedProperty("day", newStepRecord.getDay()   );
                 updateRecentDay = true; 
            }
        }
        
        // write to datastore   
        if (updateRecentDay) {
            // let's do a batch insert 
            List<Entity> updates = Arrays.asList ( recentDay, stepCountEntry);
            dataStore.put(updates);
        } else {

            dataStore.put(stepCountEntry);
        }
      
        return key;
        
    }
    
    /*
      Get the most recent day fo rthe user in a /current request
      */
    public static long getMostRecentDay(String userID) {
        
        // connect to datastore
        DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
        long mostRecentDay = 0;
        // get the most recent day value from the database
        Key k = KeyFactory.createKey("RecentDay", userID);

        boolean exists = true;
        try {
            // get the most recent day value for the user
            Entity recentDay = dataStore.get(k); 
            mostRecentDay = (long) recentDay.getProperty ("day");
            
        } catch (EntityNotFoundException ex) {
            // no user record 
            exists = false;
        }
        
        if (exists) {
            return mostRecentDay;
        } else {
            return Utils.NO_RESULTS;
        }
        
        
    }   
      
    public static long readStepCount(String userID, long day) {
      
      // TODO add exception handlers I assume!!
      
      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
      
      Query q = new Query ("StepData");      
      
      
      // retrieve all user records for specific day
      q.setFilter(Query.CompositeFilterOperator.and(
              Query.FilterOperator.EQUAL.of("uid", userID),                
              Query.FilterOperator.EQUAL.of( "day", day)));
      PreparedQuery pq = ds.prepare(q);
      
      Iterable<Entity> results = pq.asIterable();
      
      // process results to calculate step count for the day
      long total = 0;
      for (Entity result : results) {  
          
          total += (long) result.getProperty("count");

      }
      return total;
      
      
  }  
}
