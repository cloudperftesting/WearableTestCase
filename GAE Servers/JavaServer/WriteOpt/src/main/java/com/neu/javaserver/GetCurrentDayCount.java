package com.neu.javaserver;

import com.google.appengine.api.utils.SystemProperty;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// datastore imports
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.util.ArrayList;

@WebServlet(name = "GetCurrentDayCount", value = "/current/*")

public class GetCurrentDayCount extends HttpServlet {
    private static final int USER_POS = 1;
    private static final int URL_LEN =2 ;
    
    // default value for current day - will not be needed for multiday tests
    private static final int DEFAULT_DAY = 1;
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

        String userID; 
        String URLpath = request.getPathInfo();
        response.setContentType("text/plain");
        if ( URLpath != null ) {
            if ((userID = this.getUserID(URLpath, URL_LEN, USER_POS)) != null) {
                    // TODO add exception handler for database reads
                    //long total = this.readStepCount(userID, DEFAULT_DAY);
                    long currentDay = StepDataWriteOptimized.getMostRecentDay(userID) ;
                    if (currentDay != Utils.NO_RESULTS) {
                        long total = StepDataWriteOptimized.readStepCount(userID, currentDay);
                        response.setStatus(200);
                        response.getWriter().println("User=" + userID);
                        response.getWriter().println("URLpath=" + URLpath);
                        response.getWriter().println("Count: " + total);
                    } else {
                        response.setStatus(204);
                        response.getWriter().println("No data for this user - yet");
                    }
            } else {
                response.setStatus(400);
                response.getWriter().println("URL format invalid: expects /current/userN");
            }
            
        } else {
            response.setStatus(400);
            response.getWriter().println("No user - get real dude");
            
        }
        
  }
  /*
  * extract "userN" from URL or return null if invalid
  */
  private  String getUserID(String URLpath, int pathLen, int userPos) {
      String[] userID = URLpath.split("/");
      if (userID.length < pathLen) {
          return null;
      }
      //check user key starts with "user"
      if (userID[userPos].length() < 5)
          return null;
      String temp = userID[userPos].substring(0, 4); 
      if (temp.equals("user"))
          return userID[userPos];
      else 
          return null ;
  }
  
  private long readStepCount(String userID, int day) {
      
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
