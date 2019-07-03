/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neu.wearableloadtester;

/**
 *
 * @author igortn
 */
    public class TestInterval {
        private Long requestCount;
        private Long latencyTotal;
        
    public TestInterval(Long requestCount, Long latencyTotal) {
        this.requestCount = requestCount;
        this.latencyTotal = latencyTotal;
    }

        
    public Long getMean(){
        return (latencyTotal/requestCount) ;
    }    
    /**
     * @return the requestCount
     */
    public Long getRequestCount() {
        return requestCount;
    }

    /**
     * @param requestCount the requestCount to set
     */
    public void incrementRequestCount() {
        this.requestCount++;
    }



    /**
     * @param latencyTotal the latencyTotal to set
     */
    public void addToLatencyTotal(long latencyTotal) {
        this.latencyTotal += latencyTotal;
    }
 }
