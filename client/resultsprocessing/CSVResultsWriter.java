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
    
    private File file;
    private FileWriter outputFile;
    private CSVWriter writer;
    // use a buffered file writer to (hopefully) increase write performance
    private BufferedWriter bf;

    
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
            file = new File (outFileName);
            // create (buffered) FileWriter object with file as parameter
            outputFile = new FileWriter(file);
            bf = new BufferedWriter(outputFile);
            
            // create CSVWriter object filewriter object as parameter
            writer = new CSVWriter(bf);

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
    public void writeResultsBlock(ThreadRequestLatencies results) {
        // check precondition
        if (results == null)
            throw new IllegalArgumentException("Input parameter cannot be null");
        
        // extract the array of results 
        long threadID = results.getThreadID();
        ArrayList<RequestData> latencies = results.getEntries();
        long total = 0;
        
        // format results from input parameter as a list of Strings for writing as a block to csv file
        List<String[]> lines = new ArrayList<>();
        for (int i = 0; i<latencies.size(); i++) {
            RequestData values = latencies.get(i);
            String time = Long.toString(values.getTimestamp());
            String latency = Long.toString(values.getLatency());
            total = total + values.getLatency();
            String httpResult = Integer.toString(values.getResult());
                     
            lines.add(new String[] {time, values.getRequestType(), latency, httpResult});
            
        }
        // write results to file
        writer.writeAll(lines);
        // output summary to screen
        // System.out.println("Thread: " + threadID + " Average thread latency: " + total/latencies.size()) ;
        
        
    }
    /**********************************************************
     * Called at end of test to close CSV file
     */
    @Override
    public void terminate(){
        
        try {
            bf.close();
            
        } catch (IOException ex) {
            Logger.getLogger(CSVResultsWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }


    
}
