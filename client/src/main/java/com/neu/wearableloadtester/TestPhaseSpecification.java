
package com.neu.wearableloadtester;

/**
 * Immutable class to hold all the information needed for a thread to execute a test phase
 * @author igortn
 */
public final class TestPhaseSpecification {
    
    // target URL
    private final String baseURL;
    private final String testPhaseName;
    private final int startPhase;
    private final int endPhase;
    private final int numRequestsPerIteration;
    private final int keySpaceSize;
    private final int stepRange;
    private final int dayNum;
    
    /**
     * Constructor
     * @param baseURL: base URL for server
     *      pre: not null
     * @param testPhaseName: phase name for output/reporting
     *      pre: not null
     * @param startPhase: value for 'hour' to begin phase
     *      pre: >= 0
     * @param endPhase: value for 'hour' for end of test phase
     *      pre: > startPhase
     * @param numRequestsPerIteration: number of requests for each thread to send per hour
     *      pre: > 0
     * @param keySpaceSize: range of values (starting from 0) to randomly select user keys
     *      pre: > 0
     * @param stepRange: max number for steps value to be randomly generated for URL
     *      pre: > 0
     * @param dayNum: value for day number to be used in URL
     *      pre: > 0
     */
    public TestPhaseSpecification (  String baseURL, String testPhaseName, int startPhase, int endPhase, int numRequestsPerIteration, int keySpaceSize, 
                                    int stepRange, int dayNum){
        // check preconditions
        if (baseURL == null) 
            throw new IllegalArgumentException ("Base URL for test cannot be null");
        if (testPhaseName == null)
            // TODO - maybe set to default value if null and not throw exception?
            throw new IllegalArgumentException ("Test phase name cannot be null");
        if (startPhase < 0)
            throw new IllegalArgumentException ("Start phase value for test not valid: " + startPhase);
        if ( endPhase <= startPhase)
            throw new IllegalArgumentException ("End phase value must be greater than start phase value");
        if (numRequestsPerIteration <= 0)
            throw new IllegalArgumentException ("Number of requests to issue per iteraltion must be greater than 0: " + numRequestsPerIteration);
        if (keySpaceSize <= 0)
            throw new IllegalArgumentException ("User key space size must be greater than 0: " + keySpaceSize);
        if (stepRange <= 0)
            throw new IllegalArgumentException ("Range to generate step counts from  must be greater than 0: " + stepRange);
        if (dayNum <= 0)
            throw new IllegalArgumentException ("Day number must be greater than 0: " + dayNum);
        
        this.baseURL = baseURL;
        this.testPhaseName = testPhaseName;
        this.startPhase = startPhase;
        this.endPhase = endPhase;
        this.numRequestsPerIteration = numRequestsPerIteration;
        this.keySpaceSize = keySpaceSize;
        this.stepRange = stepRange;
        this.dayNum = dayNum;
    }

    /**
     * @return the baseURL
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * @return the startPhase
     */
    public int getStartPhase() {
        return startPhase;
    }

    /**
     * @return the endPhase
     */
    public int getEndPhase() {
        return endPhase;
    }

    /**
     * @return the numRequestsPerIteration
     */
    public int getNumRequestsPerIteration() {
        return numRequestsPerIteration;
    }
    
        /**
     * @return the testPhaseName
     */
    public String getTestPhaseName() {
        return testPhaseName;
    }

    /**
     * @return the keySPaceSize
     */
    public int getKeySpaceSize() {
        return keySpaceSize;
    }

    /**
     * @return the stepRange
     */
    public int getStepRange() {
        return stepRange;
    }

    /**
     * @return the dayNum
     */
    public int getDayNum() {
        return dayNum;
    }
    
}
