package com.neu.wearableloadtester;

import java.io.Serializable;


/**
 * @author igortn
 * Immutable class to hold results from a single request sent to server
 */
public final class RequestData implements Serializable, Comparable<RequestData> {
    
    private final long timestamp;
    private final long latency;
    private final int result;
    private final String requestType;
    
    /***************************************************************************
     * 
     * @param timestamp: timestamp of request
     *      -pre: > 0
     * @param latency: request latency in millisecs
     *      -pre: >= 0
     * @param result: HTTP response status code
     *      -pre: >= 100 (lowest valid response code)
     *      TODO - improve validation to ensure valid HTTP result code. Kinda overkill?
     * @param requestType 
     *      - pre: not null
     *      TODO - improve validation to ensure valid HTTP verb
     */
    public  RequestData (long timeStamp, long latency, int result, String requestType ) {
        // check preconditions
        if (timeStamp <= 0)
            throw new IllegalArgumentException("Timestamp not valid: " + timeStamp);
        if (latency < 0)
            throw new IllegalArgumentException("Latency must be >= 0:" + latency);
        if (result< 100)
            throw new IllegalArgumentException("HTTP response staus code invalid: " + result);
        if (requestType == null)
            throw new IllegalArgumentException("RequestType object cannot be null");
        
        this.timestamp = timeStamp;
        this.latency = latency;
        this.result = result;
        this.requestType = requestType;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return the latency
     */
    public long getLatency() {
        return latency;
    }

    /**
     * @return the result
     */
    public int getResult() {
        return result;
    }

    /**
     * @return the requestType
     */
    public String getRequestType() {
        return requestType;
    }
    /*******************************************************************
     * Sorts a collection of RequestData in ascending order of timestamp
     * @param compareData
     * @return 
     */
    @Override
    public int compareTo(RequestData compareData) {
  
        
        Long thisTimestamp = this.timestamp; 
        Long paramTimestamp = ((RequestData) compareData).getTimestamp();
        // we want ascending order of timestamp
        
        return thisTimestamp.compareTo(paramTimestamp);
    }
    
}
