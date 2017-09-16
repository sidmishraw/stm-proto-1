/**
 * Project: stm
 * Package: utils
 * File: Logger.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 15, 2017 2:52:38 PM
 */
package utils;

import java.util.Calendar;

/**
 * @author sidmishraw
 *
 *         Qualified Name: utils.Logger
 *
 */
public interface Logger {
    
    Calendar calendar = Calendar.getInstance();
    
    /**
     * Prints the message to STDOUT
     * 
     * @param message
     *            the message
     */
    public static void logMessage(String message) {
        
        System.out.println(calendar.getTime().toString() + ": " + Thread.currentThread().getName() + " " + message);
    }
}
