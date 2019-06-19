
package com.neu.wearableloadtester;

import java.util.ArrayList;

/**
 *
 * @author igortn
 * Collection to hold the latencies from a single thread 
 * Passed to results processing thread via a results queue
 */
public class ThreadRequestLatencies {
    
    /** thread id that generates results */ 
    private long threadID; 
    /** Array to hold individual request latencies */
    private ArrayList <RequestData> latencies = new ArrayList <> ();
    
    /*****************************************
     * Constructor
     * @param threadID: the id of the thread that generates these request latencies
     *
     */
    public ThreadRequestLatencies(long threadID) {
        
        // TODO - endOfTest value is used here as -1, making checking of precondition tricky
        // threadID should be > 0 .one to ponder
        
        this.threadID = threadID;
        
        
    }
    
    /*********************************************************************
     * Represents information about a request sent from client to server
     * @param timeStamp: timestamp of request 
     *      pre: > 0
     * @param requestType: PUT, GET, POST, DELETE, etc only -
     *      pre: ensure valid HTTP verb
     * @param result: HTTP response code 
     *      pre: > 0
     * @param latency : time in milliseconds for request 
     *      pre: >=0
     * 
     */
    public void addEntry (long timeStamp, String requestType, int result, long latency) {
        
        if (timeStamp <= 0)
            throw new IllegalArgumentException("Timestamp must be positive: " + timeStamp);
        //TO DO check request type
        if (result <= 0)
            throw new IllegalArgumentException("HTTP Result code invalied: " + result);
        if (latency < 0 )
            throw new IllegalArgumentException("Latency must ve >=0: " + latency);
        if (requestType == null)
            // TODO improve validation to check for HTTP verbs
            throw new IllegalArgumentException("Request Type must be a HTTP verb ");
        
        RequestData entry = new RequestData (timeStamp, latency, result, requestType );
        latencies.add(entry);
        
    }
    /**
     * 
     * @return threadID
     */
    public long getThreadID() {
        return threadID;
    }
    
    /**
     * 
     * @return ArrayList of request results
     */
    public ArrayList<RequestData> getEntries(){
        return latencies;
    }
    
}
