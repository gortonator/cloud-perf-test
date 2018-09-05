
package com.neu.wearableloadtester;

/**
 *
 * @author igortn
 */
import java.util.concurrent.BlockingQueue;
import  java.util.concurrent.ThreadLocalRandom;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread to issue GET requests to /current/userN endpoint.
 * Number of requests to issue and user key range to selext randomly from are passed in to constructor
 * Accumulates all results and writes them as a block to a queue for asychronous processing
 * @author igortn
 */
public class GetThread  implements Runnable{
    
    // queue to write results to
    private final BlockingQueue<ThreadRequestLatencies> resultsOutQ;
    // object to accumulate test results in
    private ThreadRequestLatencies results;
    // Used to specify number of requuests to sned
    private TestPhaseSpecification testInfo;
    
    /**************************************************************
     * Constructor
     * @param resultsOut: queue to write outputs to 
     *      pre: not null
     * @param testInfo: specifies behavior of thread - not null
     *      pre: not null
     */
    public GetThread (BlockingQueue<ThreadRequestLatencies> resultsOut, TestPhaseSpecification testInfo) {
        // check preconditions
        if (resultsOut == null)
            throw new IllegalArgumentException("Queue for writing resulkts cannot be null");
        if (testInfo == null)
            throw new IllegalArgumentException("TestPhaseSpecification object cannot be null");
        
        this.resultsOutQ = resultsOut;
        this.testInfo = testInfo;
        
    }
    /***************************************************************************
     * Main thread method:
     * -constructs and issues N GET requests to specified endpoint
     * -writes results (latencies, timestamp)  of all requests to a queue at end of execution for processing
     * -terminates when all requests sent
     */
    public void run() {
        
      
        // foreach interval in this test phase (from start to end)
        for (int hour= testInfo.getStartPhase(); hour <= testInfo.getEndPhase(); hour++) {
            results = new ThreadRequestLatencies(Thread.currentThread().getId()); 
            CloseableHttpClient httpclient = HttpClients.createDefault();
            
            // issue N GETs to endpoint and store latencies/etc in results object
            // TODO simplify method
            for (int request = 0; request < testInfo.getNumRequestsPerIteration(); request++) {
                
                // randomly generate user to query
                int user = ThreadLocalRandom.current().nextInt(1, testInfo.getKeySpaceSize());
                
                // construct URL for request
                String requestURL = testInfo.getBaseURL() 
                            + "current/" 
                            + Integer.toString(user) ;
                HttpGet httpGet = new HttpGet(requestURL);
                
                // send http request and prcoess results
                CloseableHttpResponse response = null;
                try {
                    long startTime = System.currentTimeMillis();
                    response = httpclient.execute(httpGet);
                    long endTime = System.currentTimeMillis();
                    results.addEntry(startTime, "GET", response.getStatusLine().getStatusCode(), endTime - startTime);

                    // ensure response is fully consumed
                    HttpEntity entity2 = response.getEntity();                
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
            
            // send results for processing asynchrounously and close connection
            resultsOutQ.add(results);
            try {
                httpclient.close();
            } catch (IOException ex) {
                Logger.getLogger(PutThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } // end outer for loop
           
    }
    
}
