
package com.neu.resultsprocessing;

import com.neu.wearableloadtester.ThreadRequestLatencies;



/**
 * Interface to allow performance tests to provide specialized output formats for results.
 * A default CSV format writer is provided
 * @author igortn
 */
public interface ResultsWriter {
    
    public void initialize(String outFileName);
    
    public int writeResultsBlock(ThreadRequestLatencies results);
    
    public void terminate();
    
}
