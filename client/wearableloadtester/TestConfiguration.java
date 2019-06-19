package com.neu.wearableloadtester;

/**
 * Immutable class to hold configuration parameter for the test from the
 * supplied properties file
 * @author igortn
 */
public class TestConfiguration {
    private String testName;
    private int maxThreads;
    private int keySpaceSize;
    private String targetURL;
    private int numRequestsPerHour;
    private int dayNum; 

    /**
     * Constructor - TODO add docs
     * @param testName
     * @param maxThreads
     * @param keySpaceSize
     * @param url
     * @param numResquests
     * @param dayNum 
     */
    public TestConfiguration(String testName, int maxThreads, int keySpaceSize, String url, int numRequests, int dayNum){
        // TODO add precondition checks
        this.testName = testName;
        this.maxThreads = maxThreads;
        this.keySpaceSize = keySpaceSize;
        this.targetURL = url;
        this.numRequestsPerHour = numRequests;
        this.dayNum = dayNum;
        
    }
    /**
     * @return the testName
     */
    public String getTestName() {
        return testName;
    }

    /**
     * @return the maxThreads
     */
    public int getMaxThreads() {
        return maxThreads;
    }

    /**
     * @return the keySpaceSize
     */
    public int getKeySpaceSize() {
        return keySpaceSize;
    }

    /**
     * @return the targetURL
     */
    public String getTargetURL() {
        return targetURL;
    }

    /**
     * @return the numRequestsPerHour
     */
    public int getNumRequestsPerHour() {
        return numRequestsPerHour;
    }

    /**
     * @return the dayNum
     */
    public int getDayNum() {
        return dayNum;
    }

}