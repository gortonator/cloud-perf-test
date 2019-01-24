/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neu.javaserver;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

/**
 *
 * @author igortn
 */
public class StepDataOneTable {
    
      public static void storeData (StepData newStepRecord) {
        
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
    
      
    public static long readStepCount(String userID, int day) {
      
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
