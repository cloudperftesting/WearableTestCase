/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neu.javaserver;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 *
 * @author igortn
 */
public class PostStepCount extends HttpServlet {
    
    private static final int USER_POS = 1;
    private static final int DAY_POS =2;
    private static final int HOUR_POS =3;
    private static final int STEP_POS =4;
    private static final int URL_LEN = 5 ;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings("serial")
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       
       String URLpath = request.getPathInfo(); 
       response.setContentType("text/plain");
       // check we have a URL!
       if (URLpath == null) {
            response.getWriter().println("No user/day information - get real dude");
            return;
       } 
       // check we have the right URL components
        String[] URLparts = URLpath.split("/");
        if ( URLparts.length  != URL_LEN) {
            response.setStatus(400);
            response.getWriter().println("malformed URL - /userN/dayID/hour/count - eg /user234/4/23/1234");
            return;
        }
        
        // get the component parts of the URL         
        // User key is a string, rest must be valid integers
        String user = URLparts[USER_POS];
        String day = URLparts [DAY_POS];
        String hour = URLparts [HOUR_POS] ;
        String steps = URLparts [STEP_POS];

        //check user key starts with "user" and rest is numeric
        if (user.length() < 5) {
            response.setStatus(400);
            response.getWriter().println("User must be in format userN" );
            return;
        }
        String temp1 = user.substring(0, 4); 
        String temp2 = user.substring(4, user.length()) ;
        if ((temp1.equals("user")) && Utils.isValidNum(temp2)) {
            // we have a valid userID, do nothing. TO DO should invert conditio
            
        } else { 
            response.setStatus(400);
            response.getWriter().println("User must be in format userN" );
            return;   
        }
            
        // check rest of URL is numeric                
        if ( (( !Utils.isValidNum(day)) || !Utils.isValidNum(hour))  || !Utils.isValidNum(steps)){
            response.setStatus(400);
            response.getWriter().println("/{userN}/day/hour/steps  must be numeric");
        } else {
            // yeah - store in database!
            StepData newStepRecord = new StepData (user, day, hour, steps) ;
            //this.storeData(newStepRecord );
            String key = StepDataWriteOptimized.storeData(newStepRecord );
            
            // return results - should return a 204 but just return 200 for testing
            // response.setStatus(204);
            response.getWriter().println("User=" + key + " day = " + day + " hour= " + hour + " steps= " + steps);
         }

        
    }



    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processPostRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    
    private String storeData (StepData newStepRecord) {
        
        // connect to datastore
        DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
        
        //create datastore object
        String key;
        key = "user" + newStepRecord.getUser() + "#" + Integer.toString(newStepRecord.getDay()) + Integer.toString(newStepRecord.getHour());
        //Key entityKey = KeyFactory.createKey("StepData", key);
        Entity stepCountEntry = new Entity ("StepData", key);
        //Entity stepCountEntry = new Entity ("StepData");
             
        // set datastore object values
        stepCountEntry.setProperty("uid", newStepRecord.getUser() );
        stepCountEntry.setProperty("day", newStepRecord.getDay());
        stepCountEntry.setUnindexedProperty("hour", newStepRecord.getHour());
        stepCountEntry.setUnindexedProperty("count", newStepRecord.getStepCount()   );
        
        // write to datastore
        dataStore.put(stepCountEntry);
        
        return key;
        
    }

}
