/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import com.neu.resultsprocessing.CSVResultsProcessor;
import com.neu.wearableloadtester.*;


/**
 *
 * @author igortn
 */
public class ResultsProcessorTest {
    
   private static final CSVResultsProcessor test = new CSVResultsProcessor();
        
   public static void main(String[] args) {

        test.processResultsUnsorted("c:\\Users\\Public\\PyServer\\GETraw.csv", "c:\\Users\\Public\\PyServer\\GETresults.csv");
    }
    
}
