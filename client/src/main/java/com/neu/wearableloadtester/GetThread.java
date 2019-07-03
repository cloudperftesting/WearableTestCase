
package com.neu.wearableloadtester;

/**
 *
 * @author igortn
 */
import java.util.concurrent.BlockingQueue;
import  java.util.concurrent.ThreadLocalRandom;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;

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
    
    private CloseableHttpClient httpClient;
    
    /**************************************************************
     * Constructor
     * @param resultsOut: queue to write outputs to 
     *      pre: not null
     * @param testInfo: specifies behavior of thread - not null
     *      pre: not null
     * @param client
     */
    public GetThread (BlockingQueue<ThreadRequestLatencies> resultsOut, TestPhaseSpecification testInfo, CloseableHttpClient client) {
        // check preconditions
        if (resultsOut == null)
            throw new IllegalArgumentException("Queue for writing resulkts cannot be null");
        if (testInfo == null)
            throw new IllegalArgumentException("TestPhaseSpecification object cannot be null");
        //TODO add more checks
        this.resultsOutQ = resultsOut;
        this.testInfo = testInfo;
        httpClient = client;
        
    }
    /***************************************************************************
     * Main thread method:
     * -constructs and issues N GET requests to specified endpoint
     * -writes results (latencies, timestamp)  of all requests to a queue at end of execution for processing
     * -terminates when all requests sent
     */
    public void run() {
        

        for (int hour= testInfo.getStartPhase(); hour < testInfo.getEndPhase(); hour++) {
            results = new ThreadRequestLatencies(Thread.currentThread().getId()); 
            // CloseableHttpClient httpclient = HttpClients.createDefault();
            
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
                    response = httpClient.execute(httpGet);
                    // get the response body as an array of bytes
                    long endTime = System.currentTimeMillis();
                    results.addEntry(startTime, "GET", response.getStatusLine().getStatusCode(), endTime - startTime);

                    // ensure response is fully consumed
                    HttpEntity entity2 = response.getEntity();                
                    EntityUtils.consume(entity2);
                    //System.out.println("Get response consumed");
                   
                }  catch ( IOException ex) {
                    
                        //Logger.getLogger(PostThread.class.getName()).log(Level.SEVERE, null, ex);
                        //long endTime = System.currentTimeMillis();
                        // results.addEntry(startTime, "GET", 503, endTime - startTime);
                        System.out.println("GET error: " + ex.getMessage());
               /* }  catch (ClientProtocolException ex) {
                    // TO DO Handle protocol errors */
                } 
            
                finally {
                    try {
                        if (response != null) {
                            response.close();
                        }  
                    } catch (IOException ex) {
                        Logger.getLogger(PostThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }    
             } // end inner for loop
            
            // send results for processing asynchrounously and close connection
            resultsOutQ.add(results);
            
                //httpClient.close();
    
            
        } // end outer for loop
       // System.out.println("+++GET Terminating+++");
    }
    
}
