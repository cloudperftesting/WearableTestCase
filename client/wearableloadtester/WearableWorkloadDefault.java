
package com.neu.wearableloadtester;


import com.neu.resultsprocessing.CSVResultsProcessor;
import com.neu.resultsprocessing.ResultsProcessor;
import com.neu.resultsprocessing.ResultsProcessingThread;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;



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
    /** Maximum number os steps per hour to use to generate random value     */
    private final static int MAX_STEPS_PER_HOUR = 5000;
    /** Default file names for results output files
     * TODO refactor to make paths configurable through properties file
     */
    private final static String DEFAULT_PATH = "c:\\Users\\Public\\nodeServer-RO\\";
    //private final static String DEFAULT_PATH = "c:\\Users\\Public\\PyServer\\";
    //private final static String DEFAULT_PATH = "c:\\Users\\Public\\GoServer\\";
    // start and end values for each phase TODO improve by combining with testPhases map
    private final int[] testIntervals = {1, 2, 5, 19, 23, 25};
    //private final int[] testIntervals = {1, 4, 7, 10, 13, 16};
    /** Names for 5 test phases */
    private enum TESTPHASE { WARMUP, GROWTH, PEAK, SHRINK, COOLDOWN;} 
    private final Map<TESTPHASE, Integer> testPhases = new EnumMap<>(TESTPHASE.class);
    private int keySpaceSize; 
    private int numRequestsPerHour;
    /** Queue used to send results from Workload to ResultsWriter from POST operations */
    private BlockingQueue<ThreadRequestLatencies> resultsQPOST;
    /** Queue used to send results from Workload to ResultsWriter from GET operations */
    private BlockingQueue<ThreadRequestLatencies> resultsQGET;
    
    /** Each WearableWorkload test operates on a single day. Days are simply ascending ints in the database */
    private int dayNum; 
    /** Base URL for test site */
    private String baseURL;
    /** Test name for constructing output files */
    private String testName;
    /** reusable connection to pass to client threads */
    CloseableHttpClient httpClient;
    /** number of phases for the test */
    int numPhases; 
    
    
    /***************************************************************
     *  Setup test structure ready for execution
     * 
     * @param testConfig : object hold test configuration settings from properties file
     *      -pre not null
     */
    @Override
   // public void Initialize (int maxThreads, int keySpace, String baseURL, int numRequestsPerHour, int dayNum){ 
      public void Initialize (TestConfiguration testConfig)          {
        System.out.println("Enter WearableWorkLoadDefault");
        
        // pre condition checks 
        if (testConfig == null)
            throw new IllegalArgumentException("Test Configuration object cannot be null") ;   
        
        // instantiate unbounded blockingqueues for asynchronoud results preocessing
        //from PUT and GET threads
        resultsQPOST = new LinkedBlockingQueue<>();
        resultsQGET = new LinkedBlockingQueue<>();
        
        int maxThreads = testConfig.getMaxThreads();
        // check maxThreads and exit if too low
        if (maxThreads < 10) {
            System.out.println("Max threads must be 10 or more for this workload. Setting value to 10");
            maxThreads =10;
        }
        
        // calculate the number of threads per test phase for this workload and store values.
        // TO DO get rid of magic numbers
        testPhases.put(TESTPHASE.WARMUP, maxThreads/10 );
        testPhases.put(TESTPHASE.GROWTH, maxThreads/2 );
        testPhases.put(TESTPHASE.PEAK, maxThreads );
        testPhases.put(TESTPHASE.SHRINK, maxThreads/3 );
        testPhases.put(TESTPHASE.COOLDOWN, maxThreads/10 ); 
        
        this.baseURL = testConfig.getTargetURL() ;
                
        System.out.println("WARMUP = " + testPhases.get(TESTPHASE.WARMUP)); //.intValue());
        System.out.println("Base URL is " + this.baseURL);
        
        this.keySpaceSize = testConfig.getKeySpaceSize();
        this.numRequestsPerHour = testConfig.getNumRequestsPerHour();
        this.dayNum = testConfig.getDayNum();
        this.testName = testConfig.getTestName();
        
        numPhases = testPhases.size();
   
        
    }
    /********************************************
     * TODO comment
     */
    public void Run(){
        //TODO refactor this method
        Iterator<TESTPHASE> enumKeySet = testPhases.keySet().iterator();
        // TODO make a constant
        int endOfTestMarker = -1;
        
        //start the results processing threads. One for GETs and one for POSTSs with unique output file names
        String putFileName = DEFAULT_PATH + testName + "-POSTraw.csv";
        String getFileName = DEFAULT_PATH + testName + "-GETraw.csv";
        Thread resultsProcessingThreadPUT = new Thread (new ResultsProcessingThread( resultsQPOST , endOfTestMarker, putFileName));
        resultsProcessingThreadPUT.start();
        Thread resultsProcessingThreadGET = new Thread (new ResultsProcessingThread( resultsQGET , endOfTestMarker, getFileName));
        resultsProcessingThreadGET.start();
        
        // create multithreaded http connection pool
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        httpClient = HttpClients.custom()
        .setConnectionManager(cm)
        .build();
        
        // Increase max total connection
        cm.setMaxTotal(2000);
        // Increase default max connection per route
        cm.setDefaultMaxPerRoute(1000);
        int currentTestPhase = 0; 
        while(enumKeySet.hasNext()){
            TESTPHASE currentPhase = enumKeySet.next(); 
            int numThreads = testPhases.get(currentPhase);
            //TO DO fix hardcoded values - horrible
            TestPhaseSpecification thisPhase = 
                    new TestPhaseSpecification (baseURL, 
                                                currentPhase.name(), 
                                                testIntervals[currentTestPhase], 
                                                testIntervals[currentTestPhase+1], 
                                                numRequestsPerHour, 
                                                keySpaceSize, 
                                                MAX_STEPS_PER_HOUR, dayNum);
            try {
                // pass true if this is the last test phase
                this.executeTestPhase(thisPhase, numThreads, !(enumKeySet.hasNext()));
            } catch (InterruptedException ex) {
                System.out.println( "Test Phase interrupted:" + currentPhase.name());
                ex.printStackTrace();
                System.exit(1);
                        
            }
            
            currentTestPhase++;
        }
        System.out.println("Test Completed - terminating resultsProcessingThread");
        try {
            //Termintae the results processing threads
            resultsQPOST.put(new ThreadRequestLatencies (endOfTestMarker));
            resultsQGET.put(new ThreadRequestLatencies (endOfTestMarker));
        } catch (InterruptedException ex) {
            Logger.getLogger(WearableWorkloadDefault.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Sorting and creating results files");
        
       // String sortedPUTFile = this.SortOutputFile(putFileName, "-POST-SORTED.csv");
        // String sortedGETFile = this.SortOutputFile(getFileName, "-GET-SORTED.csv");
        String resultsFilePOST = DEFAULT_PATH + testName + "POST-RESULTS.csv";
        String resultsFileGET = DEFAULT_PATH + testName + "GET-RESULTS.csv";
        ResultsProcessor results = new CSVResultsProcessor();
        // results.processResults(sortedPUTFile, resultsFilePUT);
        // results.processResults(sortedGETFile, resultsFileGET);
        results.processResultsUnsorted(getFileName, resultsFileGET);
        results.processResultsUnsorted(putFileName, resultsFilePOST);
        System.out.println ("+++Test Complete+++");
        
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
        
        System.out.println(" New test Phase. Threads =  " + numThreads);
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
            Thread tmpThread = new Thread(new PostThread(resultsQPOST, testPhaseData, nextPhaseSignal, httpClient));
            tmpThread.start();
        }

        // execute GetThreads asychronously to completion, no latch required
        for (int i = 0; i < NUM_GET_THREADS; i++) {
            Thread tmpThread = new Thread(new GetThread(resultsQGET, testPhaseData, httpClient));
            tmpThread.start();
        }
        
        nextPhaseSignal.await(); // triggered when any thread finished so we move on the next phase whiel other threads complete
        System.out.println("Latch triggered");
        
        
        
    }
    
    private String SortOutputFile (String file, String fileNameExtension) {
        
        CSVReader reader;
        CSVWriter writer;
        // create file name with appropriate name to distinguish from raw test output
        String sortedFileName = DEFAULT_PATH + testName + fileNameExtension;;
        
        List<String[]> lines = new ArrayList<>();
        List <RequestData> results = new ArrayList<>();
         try {
            reader = new CSVReader(new FileReader(file));
            writer = new CSVWriter(new FileWriter(sortedFileName)) ;
            
            lines = reader.readAll();
            for (int i = 0; i < lines.size(); i++) {
                String[] line = lines.get(i);
                results.add(new RequestData( Long.parseLong(line[0]),  // timestamp
                                             Long.parseLong(line[2]),  // latency 
                                             Integer.parseInt(line[3]), // HTTP response
                                             line[1]  )                  // operation            
                );
            }
            Collections.sort(results);
            
            lines.clear(); // empty the unsorted collection so we can refill with sorted
            for (int i = 0; i<results.size(); i++) {
                RequestData values = results.get(i);
                String time = Long.toString(values.getTimestamp());
                String latency = Long.toString(values.getLatency());
                String httpResult = Integer.toString(values.getResult());

                lines.add(new String[] {time, values.getRequestType(), latency, httpResult});
            }
            
            // write results to file
            writer.writeAll(lines);
            writer.close();
            
            
            
       
        } catch (IOException  ex) {
            Logger.getLogger(CSVResultsProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return sortedFileName;
         
    }
    
}

