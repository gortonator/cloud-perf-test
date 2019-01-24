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
       if (URLpath == null) {
            response.getWriter().println("No user/day information - get real dude");
            return;
        } 
       
        String[] URLparts = URLpath.split("/");
        if ( URLparts.length  == URL_LEN) {
            String user = URLparts[USER_POS];
            String day = URLparts [DAY_POS];
            String hour = URLparts [HOUR_POS] ;
            String steps = URLparts [STEP_POS];
            // User key is a string, rest must be valid integers
            if ( (Utils.isValidNum(day)) &&  (Utils.isValidNum(hour))  && (Utils.isValidNum(steps))){
                StepData newStepRecord = new StepData (user, day, hour, steps) ;
                //this.storeData(newStepRecord );
                StepDataOneTable.storeData(newStepRecord );
                response.getWriter().println("User=" + user + " day = " + day + " hour= " + hour + " steps= " + steps);
            } else {
                response.setStatus(400);
                response.getWriter().println("user/day/hour/steps  must be numeric");
            }
         } else {
            response.setStatus(400);
            response.getWriter().println("malformed URL - get real dude");
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
    
    private void storeData (StepData newStepRecord) {
        
        // connect to datastore
        DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
        
        //create datastore object
        String key = newStepRecord.getUser() + "#" + Integer.toString(newStepRecord.getDay()) + Integer.toString(newStepRecord.getHour());
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
        
        
        
    }

}
