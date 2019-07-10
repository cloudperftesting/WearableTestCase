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
 * Thread to issue POST requests to /userN/dayNum/HourNum/steps endpoint.
 * Number of requests to issue and user key range to select randomly from are passed in to constructor
 * Accumulates all results (latenties, HTTP result) and writes them as a block to a queue for asychronous processing
 * Terminates when all requests sent, triggering  a latch to indicate completion
 * @author igortn
 */
public class PostThread  implements Runnable{
    
    // how many times to retry a request
    private static final int NUMRETRIES = 5;
   
    private final BlockingQueue<ThreadRequestLatencies> resultsOutQ;
    private CloseableHttpClient httpClient;
      
    private ThreadRequestLatencies results;
    private TestPhaseSpecification testInfo;
    private CountDownLatch doneSignal;
    
      /*********************************************************************************
     * 
     * @param resultsOut: queue to write results to
     *      pre: not null
     * @param testInfo: contains specification of test for thread to execute
     *      pre: not null
     * @param doneSignal: latch for thread to decrement to indicate completion to caller
     *      pre: not null
     * @param client
     */
    public PostThread (BlockingQueue<ThreadRequestLatencies> resultsOut, TestPhaseSpecification testInfo, CountDownLatch doneSignal, CloseableHttpClient client) {
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
        this.httpClient = client;
    }
    
    /*****************************************************************************
     * 
     */
    public void run() {
          //System.out.println("POST thread starting: Start phase: " + testInfo.getStartPhase() + " End: " + testInfo.getEndPhase() + " Iterations: " + testInfo.getNumRequestsPerIteration());
        long threadID = Thread.currentThread().getId();

        int retries = 0;
        for (int hour= testInfo.getStartPhase(); hour < testInfo.getEndPhase(); hour++) {
            results = new ThreadRequestLatencies(threadID); 
            
  
                        
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
                
              
                retries += sendRequestWithRetries (requestURL, NUMRETRIES) ;
            } // end for
            
            if (results.size() != testInfo.getNumRequestsPerIteration()) {
                 System.out.println("POST Invariant broken: only " + results.size() + " recorded");
            }
            results.setRetries (retries);
            resultsOutQ.add(results);

            
        } // end outer for loop
        

        
        doneSignal.countDown();
       // System.out.println("+++POST Terminating+++"+ "Start phase: " + testInfo.getStartPhase() + " End: " + testInfo.getEndPhase() + " Iterations: " + testInfo.getNumRequestsPerIteration());

    
    }
        
        
    /*
        send supplied URL and retry up to numRetries times if don't receive a 2xx response
        requestURL - valid URL, not null
        maxAttempts - > 0
    */
    private int sendRequestWithRetries(String requestURL, int maxAttempts) {
         
        
         int attempts =0; 
         boolean success = false;
         int backOff = 1 ;
         int HttpResponseCode = 0;
         long startTime = 0;
         
         // create request to send
         HttpPost httpPost = new HttpPost(requestURL);
         // start timer - request latency includes retries
         startTime = System.currentTimeMillis();
         while (attempts < maxAttempts && !success) {
             
              CloseableHttpResponse response = null;
                
              try {
                    
                    // send request 
                    response = httpClient.execute(httpPost);
                    long endTime = System.currentTimeMillis();
                    HttpResponseCode = response.getStatusLine().getStatusCode();
                    
                    // blunt error handling - we just want success!
                    if (HttpResponseCode >= 200 && HttpResponseCode < 300) {
                        results.addEntry(startTime, "POST", response.getStatusLine().getStatusCode(), endTime - startTime);
                        //System.out.println("Response code: " + response.getStatusLine());
                        HttpEntity entity2 = response.getEntity();
                        // do something useful with the response body
                        // and ensure it is fully consumed
                        EntityUtils.consume(entity2);
                        success = true;
                    } else {
                        // we sleep and retry 
                        attempts++;
                        Thread.sleep(backOff * attempts);
                       //  System.out.println("POST error: " + HttpResponseCode + " retry "  +  attempts);
                    }
                   
                }  catch (IOException ex) {
                        long endTime = System.currentTimeMillis();

                        System.out.println("POST error: "  + ex.getMessage());
                        // Logger.getLogger(PostThread.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {      
                        Logger.getLogger(PostThread.class.getName()).log(Level.SEVERE, null, ex);
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
             } // end while
             if (!success) {
                 System.out.println("POST error: Failed after max retries in test phase " + testInfo.getEndPhase() + " URL " + requestURL );
                 results.addEntry(startTime, "POST", HttpResponseCode, 0);
             }
             return attempts ;
         }
        
             
        
        
        
    
    
}
