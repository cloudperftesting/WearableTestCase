/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neu.javaserver;

/**
 *
 * @author igortn
 */



public class Utils {
    
    public final static long NO_RESULTS = -1;
    public static boolean isValidNum(String user) {
        try {
            long d = Long.parseLong(user);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
        
    }
    
}
