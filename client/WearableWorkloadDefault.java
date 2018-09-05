
package com.neu.wearableloadtester;


import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Implements a write-heavy workload on the Wearables server HTTP interface
 * Test is configured by value supplied in a properties file
 * @author igortn
 */
public class WearableWorkloadDefault implements Workload{
    
    /** This default test scenario uses 5 test phase to ramp up and down load.
     *  Number  of threads per phase is calculated from maxThreads in the properties file
     */
    private final static int NUM_TEST_PHASES = 5;
    /** This test scenario uses 10 threads to issue GET requests. */
    private final static int NUM_GET_THREADS = 10;
    /** Default file names for results output files
     * TODO refactor to make paths configurable through properties file
     */
    private final static String DEFAULT_PUTFILE_PATH = "c:\\Users\\Public\\putresults.csv";
    private final static String DEFAULT_GETFILE_PATH = "c:\\Users\\Public\\getresults.csv";
    /** Names for 5 test phases */
    private enum TESTPHASE { WARMUP, GROWTH, PEAK, SHRINK, COOLDOWN;} 
    private Map<TESTPHASE, Integer> testPhases = new EnumMap<>(TESTPHASE.class);
    private int keySpaceSize; 
    private int numRequestsPerHour;
    /** Queue used to send results from Workload to ResultsWriter from PUT operations */
    private BlockingQueue<ThreadRequestLatencies> resultsQPUT;
    /** Queue used to send results from Workload to ResultsWriter from PUT operations */
    private BlockingQueue<ThreadRequestLatencies> resultsQGET;
    
    /** Each WearableWorkload test operates on a single day. Days are simply ascending ints in the database */
    private int dayNum; 
    /** Base URL for test site */
    private String baseURL;
    
    
    /***************************************************************
     *  Setup test structure ready for execution
     * 
     * @param maxThreads: peak threads to run in test
     *      -pre: > 0
     * @param keySpace: range of user ID to select from
     *      -pre > 0
     * @param baseURL: base URL for test server
     *      -pre: not null
     * @param numRequestsPerHour : number of requests for each thread to issue for hour value  of test URL
     *      -pre: >0
     * @param dayNum: each test iteration designed to run on a single day
     *      -pre: >0
     */
    @Override
    public void Initialize (int maxThreads, int keySpace, String baseURL, int numRequestsPerHour, int dayNum){ 
        System.out.println("Enter WearableWorkLoadDefault");
        // TODO add pre condition checks 
                
        // instantiate unbounded blockingqueues for asynchronoud results preocessing
        //from PUT and GET threads
        resultsQPUT = new LinkedBlockingQueue<>();
        resultsQGET = new LinkedBlockingQueue<>();
        

        
        // calculate the number of threads per test phase for this workload and store values.
        // TO DO get rid of magic numbers
        testPhases.put(TESTPHASE.WARMUP, maxThreads/10 );
        testPhases.put(TESTPHASE.GROWTH, maxThreads/2 );
        testPhases.put(TESTPHASE.PEAK, maxThreads );
        testPhases.put(TESTPHASE.SHRINK, maxThreads/3 );
        testPhases.put(TESTPHASE.COOLDOWN, maxThreads/10 ); 
        
        // add dayNum to the URL for this load test
        this.baseURL = baseURL ;
                
        System.out.println("WARMUP = " + testPhases.get(TESTPHASE.WARMUP)); //.intValue());
        System.out.println("Base URL is " + this.baseURL);
        
        this.keySpaceSize = keySpace;
        this.numRequestsPerHour = numRequestsPerHour;
        this.dayNum = dayNum;
   
        
    }
    
    public void Run(){
        
        Iterator<TESTPHASE> enumKeySet = testPhases.keySet().iterator();
        int endOfTestMarker = -1;
        
        //start the results processing threads. One for GETs and one for PUTs
        Thread resultsProcessingThreadPUT = new Thread (new ResultsProcessingThread( resultsQPUT , endOfTestMarker, DEFAULT_PUTFILE_PATH));
        resultsProcessingThreadPUT.start();
        Thread resultsProcessingThreadGET = new Thread (new ResultsProcessingThread( resultsQGET , endOfTestMarker, DEFAULT_GETFILE_PATH));
        resultsProcessingThreadGET.start();
        
        while(enumKeySet.hasNext()){
            TESTPHASE currentPhase = enumKeySet.next(); 
            int numThreads = testPhases.get(currentPhase);
            //TO DO fix hardcoded values - horrible
            TestPhaseSpecification thisPhase = new TestPhaseSpecification (baseURL, currentPhase.name(), 1, 3, numRequestsPerHour, keySpaceSize, 5000, dayNum);
            try {
                // pass true if this is the last test phase
                this.executeTestPhase(thisPhase, numThreads, !(enumKeySet.hasNext()));
            } catch (InterruptedException ex) {
                System.out.println( "Test Phase interrupted:" + currentPhase.name());
                ex.printStackTrace();
                System.exit(1);
                        
            }
            
            
        }
        System.out.println("Test Completed - terminating resultsProcessingThread");
        try {
            //Termintae the results processing threads
            resultsQPUT.put(new ThreadRequestLatencies (endOfTestMarker));
            resultsQGET.put(new ThreadRequestLatencies (endOfTestMarker));
        } catch (InterruptedException ex) {
            Logger.getLogger(WearableWorkloadDefault.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void Terminate (){
        
    }
    
    /************************************************************
     * 
     * @param testPhaseData
     * @param numThreads
     * @param lastPhase
     * @throws InterruptedException 
     */
    private void executeTestPhase(TestPhaseSpecification testPhaseData, int numThreads, boolean lastPhase) 
            throws InterruptedException {
        
        CountDownLatch nextPhaseSignal;
        // if last phase when we want all threads to cleanly terminate
        if (lastPhase) {
            nextPhaseSignal = new CountDownLatch(numThreads);
        } else {
            // We only want to wait for a single thread to complete before starting next phase 
            // transition between phases isn't 'lockstepped'
            nextPhaseSignal = new CountDownLatch(1);
        }
        
        for (int i = 0; i < numThreads; i++) {
            Thread tmpThread = new Thread(new PutThread(resultsQPUT, testPhaseData, nextPhaseSignal));
            tmpThread.start();
        }
        // execute GetThreads asychronously to completion
        for (int i = 0; i < NUM_GET_THREADS; i++) {
            Thread tmpThread = new Thread(new GetThread(resultsQGET, testPhaseData));
            tmpThread.start();
        }
        
        nextPhaseSignal.await(); // triggered when any thread finished so we move on the next phase whiel other threads complete
        System.out.println("Latch triggered");
        
        
        
    }
    
}

