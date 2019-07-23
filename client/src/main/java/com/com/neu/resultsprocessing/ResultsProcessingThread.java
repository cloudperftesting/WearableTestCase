
package com.neu.resultsprocessing;

import com.neu.wearableloadtester.ThreadRequestLatencies;
import java.util.concurrent.BlockingQueue;

/**
 * Thread to pull performance test results from queue and write to file 
 * @author igortn
 */
public class ResultsProcessingThread implements Runnable {
    
    // Queue receives results in block from each testing thread
    private final BlockingQueue<ThreadRequestLatencies> resultsQIn;
    // write results received on queue to an output format for post-processing
    private final ResultsWriter writeOutput;
    int endOfTest;
    private final String outFileName;
    
    /***************************************************************************
     * 
     * @param resultsQIn: blockingqueue to pull results from
     *      pre: not null
     * @param endOfTest: marker value to indicate endOfTest and shutdown thread
     * @param outFileName : file to write result to
     *      pre: not null
     */
    public ResultsProcessingThread(BlockingQueue resultsQIn, int endOfTest, String outFileName) {
        
        // check preconditions
        if (resultsQIn == null)
            throw new IllegalArgumentException("Queue object cannot be null");
        if (outFileName == null)
            throw new IllegalArgumentException("Filename cannot be null");
        
        this.resultsQIn = resultsQIn;
        this.endOfTest = endOfTest;
        this.outFileName = outFileName;
        
        // TO DO refactor to make the output format configuarble at run time
        // Just using csv writer as default for now
        writeOutput = new CSVResultsWriter();
        writeOutput.initialize(outFileName); 
        
    }
    /*****************************************************************************
     * Pulls results from blocking queue and  persists them 
     * Terminates when it receives an endOfTest marker
     */
    public void run() {
        ThreadRequestLatencies latencies;
        int retriesTotal = 0;
        int totalLines = 0;
        try {   
                latencies = resultsQIn.take();
                while (latencies.getThreadID() != endOfTest) {
                    retriesTotal += latencies.getRetries();
                    int lines = writeOutput.writeResultsBlock(latencies);
                    totalLines += lines ; 
                    latencies = resultsQIn.take();
                }    
            } 
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Results Processing thread terminating: lines processes = " + totalLines+ " - retries = " + retriesTotal);
        writeOutput.terminate();
    }
            
        
    
}
    

