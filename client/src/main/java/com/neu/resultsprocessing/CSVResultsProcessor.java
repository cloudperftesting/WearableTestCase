
package com.neu.resultsprocessing;

import com.neu.wearableloadtester.TestInterval;
import java.io.*;
import java.util.*;
import com.opencsv.CSVWriter;
import com.opencsv.CSVReader;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads and processes a results file written by CSVResultsWriter class
 * sorts the file if needed
 * @author igortn
 */
public class CSVResultsProcessor implements ResultsProcessor {
    

    private FileWriter outputFile;
    private FileReader inputFile;
    private CSVWriter writer;
    private CSVReader reader;
    // use a buffered file writer to (hopefully) increase write performance
    private BufferedWriter bf;
    List<String[]> lines = new ArrayList<>();
    
 
   
    @Override
    public void processResults(String resultsFileNameIn, String resultsFileNameOut) {
        
        int totalLines = 0 ;
        int resultsThisInterval = 0;
        int totalThisInterval = 0;
        int totalIntervals = 0;
        
        // open the input and output files
        try {
            reader = new CSVReader(new FileReader(resultsFileNameIn));
            writer = new CSVWriter(new FileWriter(resultsFileNameOut)) ;
        } catch (IOException ex) {
            Logger.getLogger(CSVResultsProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            boolean moreResults = true;
            String [] resultsLine;
            // check file not empty
            if ((resultsLine = reader.readNext())== null){
                moreResults = false ;
            }
            totalLines = 1;
            while (moreResults) {
                // new time interval
                // timestamp is in milliseconds, so calculate a one second interval to buclet results in to 
                long oneSecondIntervalBucketExtent = Long.valueOf(resultsLine[0]) + 1000; 
                resultsThisInterval = 1 ;
                totalThisInterval = Integer.valueOf(resultsLine[2]); 
                // System.out.println(resultsLine[0] + resultsLine[1] + resultsLine[2] + resultsLine[3]);
                while (  ((resultsLine = reader.readNext())!= null) ) {
                    // calculate stas for this interval 
                    totalLines++;
                    if ( (Long.valueOf(resultsLine[0]) < oneSecondIntervalBucketExtent)) {
                        totalThisInterval += Long.valueOf(resultsLine[2]);
                        resultsThisInterval++;      
                    } else {
                        break;  // end of interval
                    }
                }
                long mean = (totalThisInterval/resultsThisInterval);
                writer.writeNext(new String[] {Integer.toString(totalIntervals), Integer.toString(resultsThisInterval), Long.toString(mean) });
                totalIntervals ++;

                if (resultsLine == null){
                    moreResults = false;
                } 
            }
           reader.close();
           writer.close();  

            System.out.println("Total lines processed = " + totalLines);
                             
            
        } catch (IOException ex) {
            Logger.getLogger(CSVResultsProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    public void processResultsUnsorted(String resultsFileNameIn, String resultsFileNameOut) {
        
        int totalLines = 0 ;
        int resultsThisInterval = 0;
        int totalThisInterval = 0;
        int totalIntervals = 0;
        
        // open the input and output files
        try {
            reader = new CSVReader(new FileReader(resultsFileNameIn));
            writer = new CSVWriter(new FileWriter(resultsFileNameOut)) ;
        } catch (IOException ex) {
            Logger.getLogger(CSVResultsProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        Map<Long,TestInterval> testIntervals = new TreeMap<>();
        try {
            boolean moreResults = true;
            String [] resultsLine;
            // check file not empty
            if ((resultsLine = reader.readNext())== null){
                moreResults = false ;
            }
            totalLines = 1;
            long oneSecIntervalBucketKey;
            while (moreResults) {
                // create the onse second interval bucket for this timestamp
                oneSecIntervalBucketKey = Long.valueOf(resultsLine[0])/1000 ;
               
                
                //add to hashmap, and if present increment count of requests for that second
                if (testIntervals.containsKey(oneSecIntervalBucketKey)){ 
                    TestInterval temp = testIntervals.get(oneSecIntervalBucketKey);
                    temp.addToLatencyTotal(Long.valueOf(resultsLine[2]));
                    temp.incrementRequestCount();
                    testIntervals.put(oneSecIntervalBucketKey, temp);
                } else {
                    // new one second interval - set count to 1
                    TestInterval request = new TestInterval ( (new Long (1)), Long.valueOf(resultsLine[2])  );
                    testIntervals.put (oneSecIntervalBucketKey, request) ;
                }
                    
                
                // System.out.println(resultsLine[0] + resultsLine[1] + resultsLine[2] + resultsLine[3]);
                if  (  ((resultsLine = reader.readNext())!= null) ) {
                    // calculate stas for this interval 
                    totalLines++;
                    

                } else {
                
                    moreResults = false;
                } 
            }
            
            
            /*Iterator it = testIntervals.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                System.out.println(pair.getKey() + " = " + pair.getValue());
                it.remove(); // avoids a ConcurrentModificationException
            } */
            
            testIntervals.entrySet().forEach((Map.Entry<Long, TestInterval> mapData) -> {
                TestInterval tmp = mapData.getValue();
                //System.out.println("Key : " +mapData.getKey()+ 
                  //      "  Value : " + tmp.getRequestCount() + 
                   //     " Average: " + tmp.getMean());
                 writer.writeNext(new String[] {Long.toString(mapData.getKey()), 
                         Long.toString(tmp.getRequestCount()), Long.toString(tmp.getMean()) });
              
            });
            
           reader.close();
           writer.close();  
           //long mean = (totalThisInterval/resultsThisInterval);
               // writer.writeNext(new String[] {Integer.toString(totalIntervals), Integer.toString(resultsThisInterval), Long.toString(mean) });

            System.out.println("Total lines processed = " + totalLines);
                             
            
        } catch (IOException ex) {
            Logger.getLogger(CSVResultsProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

    
    private void sort() {
 //      reader.readAll(lines);
        
        
        
    }
    
}
