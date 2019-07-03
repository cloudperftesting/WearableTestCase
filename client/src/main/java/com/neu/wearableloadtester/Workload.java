
package com.neu.wearableloadtester;

import java.util.concurrent.BlockingQueue;

/**
 *
 * @author igortn
 * Interface to define a test workload class
 */
public interface Workload {
  
    // TO DO change all to throwimg an exception
    //public void Initialize (int maxLoad, int keySpace, String baseURL, int numRequestsPerHour, int dayNum);
    public void Initialize (TestConfiguration testConfig);
    
    public void Run();
    
    public void Terminate ();
    
}
