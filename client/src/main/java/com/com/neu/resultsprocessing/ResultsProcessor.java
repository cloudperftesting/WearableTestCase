/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neu.resultsprocessing;

/**
 * Interface to allow performance tests to provide specialized output formats for results.
 * A default CSV format writer is provided
 * @author igortn
 */
public interface ResultsProcessor {
    
  
    public void processResults(String resultsFileNameIn, String ResultsFileNameOut);
    public void processResultsUnsorted(String resultsFileNameIn, String resultsFileNameOut) ;
    
  
}
