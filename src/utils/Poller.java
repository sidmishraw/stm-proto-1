/**
 * Project: stm
 * Package: utils
 * File: Poller.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 15, 2017 5:46:41 PM
 */
package utils;

import stm.STM;

/**
 * @author sidmishraw
 *
 *         Qualified Name: utils.Poller
 *
 */
public class Poller implements Runnable {
    
    private static final int MAX_RETRIES = 1;
    
    private STM              stm         = STM.getInstance();
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        
        int retries = MAX_RETRIES;
        
        while (true) {
            
            if (retries <= 0) {
                
                stm.halt();
                
                return;
            }
            
            if (stm.pendingTransactionCount() == 0) {
                
                try {
                    
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    
                    System.out.println("Error in polling thread");
                    e.printStackTrace();
                }
                
                retries--;
            }
        }
    }
    
}
