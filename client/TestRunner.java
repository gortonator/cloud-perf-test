
package com.neu.wearableloadtester;

import java.lang.IllegalArgumentException;
import java.util.concurrent.BlockingQueue;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
/**
 *
 * @author igortn
 */
public class TestRunner {
    
    // CONSTANTTS 
    /** Default value for number of threads to use in test. Use property file to specify another value */
    private final static String DEFAULT_MAX_THREADS = "200";
    /** Default class name for test. Use properties file to specify another class to execute */
    private final static String DEFAULT_TEST_NAME = "WearableWorkload";
    private final static String WORKLOAD_PACKAGE_NAME = "com.neu.wearableloadtester.";
    private final static String DEFAULT_KEY_SPACE_SIZE = "10000";
    private final static String DEFAULT_REQUESTS_PER_ITERATION = "1000" ;
    /** Application root URL. TO DO make configurable through properties file */
    private final static String BASE_URL = "https://pyserver-208423.appspot.com/";
    // a workload class executes on a signle day, default is day 1. 
    // TODO Need to incorporate a way to specify multiple days in propoerties file        
    private final static int DEFAULT_DAYNUM = 1;
    
    // Data members 
    // maximum number of concurrent teats threads duing peak workload phase. 
    private int maxThreads = 0;
    // key spce of user records in database from which test threads randomlu select 
    private int keySpaceSize = 0;
    // String representing clasName for test so that differnt tests can be invoked
    private String testClassName = null;
    // Number of PUT requests for each thread to send per hour
    private int numRequestsPerHour = 0;   
    // Implements the specific test scenario defined by testName
    private Workload testScenario;
 
    
    
    /*********************************************************
     * 
     * @return: usage instructions for the program command line options
     */
    public static String usage() {
        return ("Usage is: TestRunner \\path\\config_file_name");
    }
    
    /*****************************************************************************
     * Main performance test driver
     * @param args: location of properties file for test configuration
     *      pre: valid file location and properties file
     */
    public void runTest (String[] args) {
                     
        // Get test configuration parameters from properties file
        GetTestSpecification (args);
         
        System.out.println("Config is " + testClassName + " max threads " + maxThreads + " keySPaceSoze " + keySpaceSize + " Iterations: " + numRequestsPerHour) ;
        // instantiate the specified Workload object
        Object obj = this.CreateWorkloadInstance(testClassName);
        testScenario = (Workload) obj; 
        
        // run the test
        testScenario.Initialize(maxThreads, keySpaceSize, BASE_URL, numRequestsPerHour, DEFAULT_DAYNUM);
        testScenario.Run();
         //testScenario.Terminate();
    }
    
    /*******************************************************************
     * Creates a new test instance and start the test
     * @param args
     * [0] - name of property file to configure the test. requires absolute path name if not in local directory
     */
    public static void main(String[] args)  {
        if (args.length == 0){
             throw new IllegalArgumentException (usage());
        }
        TestRunner test = new TestRunner();
        test.runTest(args);
     }
    
     /************************************************************************
      * Process test parameters from properties file
      * @param args: location of properties file
      * TODO add validation for values from property file
      */
     private void GetTestSpecification (String[] args) {
         
         Properties configFile = new Properties();
	 try {
             // load a properties file for reading
             configFile.load(new FileInputStream(args[0]));
         } catch (IOException ex) {
             System.err.println("ERROR: Configuration file not found - " + args[0]);
             ex.printStackTrace();
         }    
         // get the properties
         // configFile.list(System.out);
	 try {	
            testClassName = configFile.getProperty("testName", DEFAULT_TEST_NAME);
            maxThreads = Integer.parseInt(configFile.getProperty("maxThreads", DEFAULT_MAX_THREADS));
            keySpaceSize = Integer.parseInt(configFile.getProperty("keySpace", DEFAULT_KEY_SPACE_SIZE));
            numRequestsPerHour = Integer.parseInt(configFile.getProperty("numRequestsPerIteration", DEFAULT_REQUESTS_PER_ITERATION));
                 
         } catch (NumberFormatException ex) {
             System.err.println("Invalid property format - must be an integer");
             ex.printStackTrace();
         }
     }
     
     /*********************************************************************
      * Instantiate test object based on class name that is passed in as input
      * Terminates if invalid class name is passed as input
      * @param testClassName: Name of Workload class specific in properties file
      *     pre: testClassName != null
      * @return Object: instance of WOrkload class created using reflection
      *     post: returned object references valid Workload object
      */
     private Object CreateWorkloadInstance (String testClassName) {
         
        Object obj = null; 
        try {
            Class classTemp = Class.forName(WORKLOAD_PACKAGE_NAME + testClassName);
            obj = classTemp.newInstance();        
        }    
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            System.out.println("Test workload class name not found");
            ex.printStackTrace();
        }
        
        return obj;
     }
     
             
}
