package com.neu.resultsprocessing;


import com.neu.wearableloadtester.RequestData;
import com.neu.wearableloadtester.ThreadRequestLatencies;
import java.io.*;
import java.util.*;
import com.opencsv.CSVWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 
 * Writes performance results to a CSV file for further processing
 * Uses BufferedWritr to reduce IO overheads
 * @author igortn
 */


public class CSVResultsWriter implements ResultsWriter{
    
    private File file, errFile;
    private FileWriter outputFile, errFileOut;
    private CSVWriter writer, errWriter;
    // use a buffered file writer to (hopefully) increase write performance
    private BufferedWriter bf, errbf;

    
    /***********************************************************************
     * 
     * @param outFileName - file to write to. 
     *      pre:Cannot be null
     */
    @Override
    public void initialize(String outFileName){
        // check preconditions
        if (outFileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }
        
        try {
            // create name for file to write HTTP error responses to
            String errFileName = outFileName.replaceFirst("raw", "err");
            file = new File (outFileName);
            errFile = new File (errFileName);
            // create (buffered) FileWriter object with file as parameter
            outputFile = new FileWriter(file);
            errFileOut = new FileWriter (errFile); 
            bf = new BufferedWriter(outputFile);
            errbf = new BufferedWriter (errFileOut);
            
            // create CSVWriter object filewriter object as parameter
            writer = new CSVWriter(bf);
            errWriter = new CSVWriter (errbf);

        } catch (IOException ex) {
            System.out.println ("Failed to create results file");
            Logger.getLogger(CSVResultsWriter.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }
    
    /*******************************************************
     * Takes an array of results from performance test and writes the array contents to a CSV file
     * @param results: contains latencies for operations from a test thread
     *      pre: not null
     */
    @Override
    public int  writeResultsBlock(ThreadRequestLatencies results) {
        // check precondition
        if (results == null)
            throw new IllegalArgumentException("Input parameter cannot be null");
         int lineCount = 0;
        // extract the array of results 
        long threadID = results.getThreadID();
        ArrayList<RequestData> latencies = results.getEntries();
        long total = 0;
        
        // format results from input parameter as a list of Strings for writing as a block to csv file
        // any HTTP errors written to errline. Should be few to none of these in a good test
        List<String[]> lines = new ArrayList<>();
        List<String[]> errLines = new ArrayList<>();
        for (int i = 0; i<latencies.size(); i++) {
            RequestData values = latencies.get(i);
            String time = Long.toString(values.getTimestamp());
            String latency = Long.toString(values.getLatency());
            total = total + values.getLatency();
            String httpResult = Integer.toString(values.getResult());
            // check for a HTTP 2XX response code
            if (values.getResult() >= 200 && values.getResult() <= 299 ) {
                lines.add(new String[] {time, values.getRequestType(), latency, httpResult});
            } else {
                errLines.add(new String[] {time, values.getRequestType(), latency, httpResult});
            }
                               
        }
        // write results to file
        if (lines.size() > 0) {
            writer.writeAll(lines);
        }
        if (errLines.size() > 0) {
            errWriter.writeAll(errLines);        }
        
        return latencies.size();
        
    }
    

    /**********************************************************
     * Called at end of test to close CSV file
     */
    @Override
    public void terminate(){
        
        try {
            System.out.println("=============================Closing raw results Files ==========================================");
            bf.close();
            errbf.close();


            
        } catch (IOException ex) {
            Logger.getLogger(CSVResultsWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }


    
}
