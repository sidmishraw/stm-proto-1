/**
 * Project: stm
 * Package: stm
 * File: Operations.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 15, 2017 1:58:02 PM
 */
package stm;

/**
 * @author sidmishraw
 *
 *         Qualified Name: stm.Operations
 *
 */
public interface Operations {
    
    @FunctionalInterface
    public interface TransactionOperation {
        
        public void execute();
    }
}
