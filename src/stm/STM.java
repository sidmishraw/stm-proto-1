/**
 * Project: stm
 * Package: stm
 * File: STM.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 7, 2017 8:02:22 PM
 */
package stm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author sidmishraw
 *
 *         Qualified Name: stm.STM
 *
 *         The Software Transactional Memory Manager/Scheduler
 */
public class STM implements Runnable {
    
    /********
     * CONSTANTS FOR STM -- START --
     ******************************************/
    
    private static final int                    MAX_POOL_THREAD_COUNT = 3;
    private static final int                    MAX_POOL_AWAIT_TIME   = 1000;
    
    /********
     * CONSTANTS FOR STM -- END --
     ********************************************/
    
    // ownerships
    private Map<Memory<?>, Record<?>>           ownerships            = null;
    
    // global job queue
    private LinkedBlockingQueue<Transaction<?>> jobQueue              = null;
    
    private Boolean                             continueManaging      = null;
    
    // singleton instance
    private static STM                          singleton             = null;
    
    private void initialize() {
        
        this.ownerships = new HashMap<>();
        this.jobQueue = new LinkedBlockingQueue<>();
        this.continueManaging = true;
    }
    
    /**
     * 
     */
    private STM() {
        
        initialize();
    }
    
    /**
     * Provides the invoker with a singleton STM instance
     * 
     * @return singleton STM instance
     */
    public static STM getInstance() {
        
        if (null == singleton) {
            
            singleton = new STM();
        }
        
        return singleton;
    }
    
    /**
     * Checks if a memory location is already owned by some other transaction
     * 
     * @param memory
     *            the memory location that is being checked for ownership
     * 
     * @return `true` if owned else `false`
     */
    public <V> boolean isOwned(Memory<V> memory) {
        
        return this.ownerships.containsKey(memory);
    }
    
    /**
     * Checks if the memory location is owned by the particular transaction
     * 
     * @param memory
     *            the memory location being checked for ownership
     * 
     * @param transaction
     *            the transaction who is checking for ownership
     * 
     * @return `true` if owned else `false`
     */
    @SuppressWarnings("unchecked")
    public <V> boolean isOwnedBy(Memory<V> memory, Record<V> transaction) {
        
        Record<V> owner = (Record<V>) this.ownerships.get(memory);
        
        if (null == owner) {
            
            return false;
        } else if (owner.equals(transaction)) {
            
            return true;
        } else {
            
            return false;
        }
    }
    
    /**
     * Transaction denoted by the Record `record` takes ownership of the
     * `record`
     * 
     * @param memory
     *            the memory location whose ownership is being taken
     * @param record
     *            the transaction who is taking the ownership
     */
    public <V> void addOwnership(Memory<V> memory, Record<V> record) {
        
        this.ownerships.put(memory, record);
    }
    
    /**
     * Releases the ownership of the particular memory location
     * 
     * @param memory
     *            the memory location that needs to be released from owership
     */
    public <V> void releaseOwnership(Memory<V> memory) {
        
        this.ownerships.remove(memory);
    }
    
    /**
     * Adds to the tail of the job queue for re-processing
     * 
     * @param transactions
     *            the transactions that need to be added to the job queue
     */
    public <V> void addToJobQueue(@SuppressWarnings("unchecked") Transaction<V>... transactions) {
        
        Arrays.asList(transactions).forEach(t -> this.jobQueue.add(t));
    }
    
    /**
     * The number of pending transactions in the job queue
     * 
     * @return the number of pending transactions
     */
    public int pendingTransactionCount() {
        
        return this.jobQueue.size();
    }
    
    /**
     * Stops the STM, now that everything is done
     */
    public final void halt() {
        
        this.continueManaging = false;
    }
    
    /**
     * Resets/Re-initializes the STM, making everything new
     */
    public final void reset() {
        
        System.out.println("Resetting stm");
        
        initialize();
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        
        System.out.println("Running STM " + this.continueManaging);
        
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_POOL_THREAD_COUNT);
        
        while (continueManaging) {
            
            Transaction<?> transaction = null;
            
            if (null != (transaction = this.jobQueue.poll())) {
                
                threadPool.execute(transaction);
            }
        }
        
        threadPool.shutdown();
        
        try {
            
            threadPool.awaitTermination(MAX_POOL_AWAIT_TIME, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            
            e.printStackTrace();
        }
    }
}
