
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
    
    // CONSTANTS 
    /** Default value for number of threads to use in test. Use property file to specify another value */
    private final static String DEFAULT_MAX_THREADS = "128";
    /** Default class name for test. Use properties file to specify another class to execute */
    private final static String DEFAULT_TEST_NAME = "WearableWorkload";
    private final static String WORKLOAD_PACKAGE_NAME = "com.neu.wearableloadtester.";
    private final static String DEFAULT_KEY_SPACE_SIZE = "10000";
    private final static String DEFAULT_REQUESTS_PER_ITERATION = "1000" ;

    /** Application root URL. TO DO make configurable through properties file */
 //   private final static String BASE_URL = "https://pyserver-208423.appspot.com/";
    private final static String BASE_URL = "https://pyserver2-neu.appspot.com/";
    // a workload class executes on a signle day, default is day 1. 
    // TODO Need to incorporate a way to specify multiple days in properties file        
    private final static String DEFAULT_DAYNUM = "1";
    

    // maximum number of concurrent teats threads duing peak workload phase. 
    private int maxThreads = 0;
    
    // key space of user records in database from which test threads randomly select 
    private int keySpaceSize = 0;
    
    // String representing clasName for test so that differnt tests can be invoked
    private String testClassName = null;
    
    // Number of PUT requests for each thread to send per hour
    private int numRequestsPerHour = 0;   
    
    // day number for URL construction. Option properties file entry
    private int dayNum;
    
    // test name - used to derive output file names
    private String testName;
    
    // terget URL for test
    private String baseURLProp;
    
    // location to write output files
    private String outputFilesDir;
    
    // Implements the specific test scenario defined by testName
    private Workload testScenario;
    
    // object to hold all test config parameters that specify the workload
    private TestConfiguration testConfig;
 
    
    
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
        testConfig = GetTestSpecification (args);
         
        System.out.println("Test Configuration is: " + testClassName + System.lineSeparator() +
                            "max threads: " + maxThreads + System.lineSeparator() +
                            "test URL " + baseURLProp + System.lineSeparator() +
                            "keySPaceSoze " + keySpaceSize + System.lineSeparator() +
                            "Iterations: " + numRequestsPerHour + System.lineSeparator() +
                            "Output Path: " + outputFilesDir) ;
        // instantiate the specified Workload object
        Object obj = this.CreateWorkloadInstance(testClassName);
        testScenario = (Workload) obj; 
        
        // run the test

        testScenario.Initialize(testConfig);
        testScenario.Run();
        testScenario.Terminate();
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
     private TestConfiguration GetTestSpecification (String[] args) {
         
         Properties configFile = new Properties();
	 try {
             // load a properties file for reading
             configFile.load(new FileInputStream(args[0]));
         } catch (IOException ex) {
             System.err.println("ERROR: Configuration file not found - " + args[0]);
         }    

	 try {	
            testClassName = configFile.getProperty("testClassName", DEFAULT_TEST_NAME);
            testName = configFile.getProperty("testName", DEFAULT_TEST_NAME);
            maxThreads = Integer.parseInt(configFile.getProperty("maxThreads", DEFAULT_MAX_THREADS));
            keySpaceSize = Integer.parseInt(configFile.getProperty("keySpace", DEFAULT_KEY_SPACE_SIZE));
            numRequestsPerHour = Integer.parseInt(configFile.getProperty("numRequestsPerIteration", DEFAULT_REQUESTS_PER_ITERATION));
            dayNum = Integer.parseInt(configFile.getProperty("dayNumber", DEFAULT_DAYNUM));
            baseURLProp = configFile.getProperty("baseURL", BASE_URL);
            // use current working directory if output path not specified
            outputFilesDir = configFile.getProperty("outputFileLocation", System.getProperty("user.dir"));
                 
         } catch (NumberFormatException ex) {
             System.err.println("Invalid property format - must be an integer");
         }

         return new TestConfiguration(testName, maxThreads,keySpaceSize, baseURLProp, numRequestsPerHour, dayNum, outputFilesDir );
         
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
