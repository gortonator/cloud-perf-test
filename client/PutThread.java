/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neu.wearableloadtester;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import  java.util.concurrent.ThreadLocalRandom;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread to issue PUT requests to /userN/dayNum/HourNum/steps endpoint.
 * Number of requests to issue and user key range to select randomly from are passed in to constructor
 * Accumulates all results (latenties, HTTP result) and writes them as a block to a queue for asychronous processing
 * Terminates when all requests sent, triggering  a latch to indicate completion
 * @author igortn
 */
public class PutThread  implements Runnable{
    
    private final BlockingQueue<ThreadRequestLatencies> resultsOutQ;
      
    ThreadRequestLatencies results;
    TestPhaseSpecification testInfo;
    CountDownLatch doneSignal;
    /*********************************************************************************
     * 
     * @param resultsOut: queue to write results to
     *      pre: not null
     * @param testInfo: contains specification of test for thread to execute
     *      pre: not null
     * @param doneSignal: latch for thread to decrement to indicate completion to caller
     *      pre: not null
     */
    public PutThread (BlockingQueue<ThreadRequestLatencies> resultsOut, TestPhaseSpecification testInfo, CountDownLatch doneSignal) {
        // check preconditions
        if (resultsOut == null)
            throw new IllegalArgumentException("Queue for writing resulkts cannot be null");
        if (testInfo == null)
            throw new IllegalArgumentException("TestPhaseSpecification object cannot be null");
        if (doneSignal == null)
            throw new IllegalArgumentException("CountDownLatch object cannot be null");
            
        this.resultsOutQ = resultsOut;
        this.testInfo = testInfo;
        this.doneSignal = doneSignal;
    }
    
    /*****************************************************************************
     * 
     */
    public void run() {
        
      
        for (int hour= testInfo.getStartPhase(); hour <= testInfo.getEndPhase(); hour++) {
            results = new ThreadRequestLatencies(Thread.currentThread().getId()); 
            CloseableHttpClient httpclient = HttpClients.createDefault();
            
            for (int request = 0; request < testInfo.getNumRequestsPerIteration(); request++) {
                
                
                
                int user = ThreadLocalRandom.current().nextInt(1, testInfo.getKeySpaceSize());
                int steps = ThreadLocalRandom.current().nextInt(0, testInfo.getStepRange());
                String requestURL = testInfo.getBaseURL() 
                            + "user" 
                            + Integer.toString(user) + "/" 
                            + Integer.toString(testInfo.getDayNum()) + "/" 
                            + Integer.toString(hour) + "/"
                            + Integer.toString(steps) ;
 
                HttpPost httpPost = new HttpPost(requestURL);

                CloseableHttpResponse response = null;
                try {
                    long startTime = System.currentTimeMillis();
                    response = httpclient.execute(httpPost);
                    long endTime = System.currentTimeMillis();
                    results.addEntry(startTime, "PUT", response.getStatusLine().getStatusCode(), endTime - startTime);
                    //System.out.println("Response code: " + response.getStatusLine());
                    HttpEntity entity2 = response.getEntity();
                    // do something useful with the response body
                    // and ensure it is fully consumed
                    EntityUtils.consume(entity2);
                   
                }  catch (IOException ex) {
                        Logger.getLogger(PutThread.class.getName()).log(Level.SEVERE, null, ex);
                }      
                finally {
                    try {
                        if (response != null) {
                            response.close();
                        }
                            
                        
                    } catch (IOException ex) {
                        Logger.getLogger(PutThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }    
             } // end inner for loop
            
            // send results for processing asynchrounously
            resultsOutQ.add(results);
            try {
                httpclient.close();
            } catch (IOException ex) {
                Logger.getLogger(PutThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } // end outer for loop
        
        
        doneSignal.countDown();

    
    }
    
}
