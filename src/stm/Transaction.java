/**
 * Project: stm
 * Package: stm
 * File: Transaction.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 15, 2017 2:21:56 PM
 */
package stm;

import java.util.Arrays;
import java.util.stream.Collectors;

import states.StateManager;
import states.Variable;
import stm.Operations.TransactionOperation;
import utils.Logger;

/**
 * @author sidmishraw
 *
 *         Qualified Name: stm.Transaction
 *
 */
public class Transaction<V> implements Runnable {
    
    /******** TRANSACTION METRICS - START ***********/
    private int failCount = 0;
    
    /**
     * Increments the fail counter of the transaction
     */
    public void incrementFailCount() {
        
        this.failCount++;
    }
    
    /**
     * Gives you the number of times the transaction failed
     * 
     * @return the failed count number
     */
    public int getFailedCount() {
        
        return this.failCount;
    }
    
    /********* TRANSACTION METRICS - END **********/
    
    /**
     * The metadata of the transaction. The `metadata` includes:
     * 
     * `version`: the version of the transaction, this also somewhat defines the
     * priority of the transaction
     * 
     * `writeSet`: set of memory locations the transaction intends to
     * modify(update)
     * 
     * `readSet`: set of memory locations the transaction intends to read from
     * 
     * `oldValues`: the oldValues of all the memory locations the transaction
     * intends to access, this is basically the backup used to restore from when
     * transaction fails and a rollback is performed.
     * 
     * `transaction's execution logic`: the execution logic of the transaction,
     * what it intends to do!
     */
    private Record<V> metadata = null;
    
    // the STM
    private STM       stm      = null;
    
    /**
     * Initializes the transaction with its metadata
     */
    private Transaction(Record<V> metadata) {
        
        this.metadata = metadata;
        this.stm = STM.getInstance();
    }
    
    /**
     * Fetches the metadata of the transaction
     * 
     * @return the metadata
     */
    public Record<V> getMetadata() {
        
        return this.metadata;
    }
    
    /**
     * The Builder of the transaction
     * 
     * @author sidmishraw
     *
     *         Qualified Name: stm.TransactionBuilder
     *
     */
    public static class TransactionBuilder<V> {
        
        @SuppressWarnings("rawtypes")
        private static TransactionBuilder tbuilder    = null;
        
        private Transaction<V>            transaction = null;
        
        private TransactionBuilder() {}
        
        /**
         * Gets you a TransactionBuilder instance
         * 
         * @return the TransactionBuilder instance
         */
        @SuppressWarnings("rawtypes")
        public static TransactionBuilder getInstance() {
            
            if (null == tbuilder) {
                
                tbuilder = new TransactionBuilder<>();
            }
            
            return tbuilder;
        }
        
        /**
         * Builds you a new transaction
         * 
         * @return the new transactionbuilder instance
         */
        public TransactionBuilder<V> buildNewTransaction() {
            
            this.transaction = new Transaction<V>(new Record<V>());
            
            return this;
        }
        
        /**
         * Adds write set memory locations to the transaction's writeSet
         * 
         * @param writeSetMembers
         *            the member memory locations
         * 
         * @return the updated transaction builder
         */
        @SuppressWarnings("unchecked")
        public TransactionBuilder<V> addWriteSetMembers(Memory<V>... writeSetMembers) {
            
            this.transaction.getMetadata().addToWriteSet((Object[]) writeSetMembers);
            
            return this;
        }
        
        /**
         * Adds write set memory locations to the transaction's writeSet
         * 
         * @param writeSetMembers
         *            the member memory locations
         * 
         * @return the updated transaction builder
         */
        @SuppressWarnings("unchecked")
        public TransactionBuilder<V> addWriteSetMembers(Variable... writeSetMembers) {
            
            StateManager<V> manager = (StateManager<V>) StateManager.getInstance();
            
            Object[] memories = Arrays.asList(writeSetMembers).stream().map(v -> manager.getCurrentMemory(v))
                    .collect(Collectors.toList()).toArray();
            
            this.transaction.getMetadata().addToWriteSet(memories);
            
            return this;
        }
        
        /**
         * Adds write set memory locations to the transaction's writeSet
         * 
         * @param writeSetMembers
         *            the member memory locations
         * 
         * @return the updated transaction builder
         */
        @SuppressWarnings("unchecked")
        public TransactionBuilder<V> addReadSetMembers(Variable... readSetMembers) {
            
            StateManager<V> manager = (StateManager<V>) StateManager.getInstance();
            
            Object[] memories = Arrays.asList(readSetMembers).stream().map(v -> manager.getCurrentMemory(v))
                    .collect(Collectors.toList()).toArray();
            
            this.transaction.getMetadata().addToReadSet(memories);
            
            return this;
        }
        
        /**
         * Adds the read set memory locations to the transaction's readSet
         * 
         * @param readSetMembers
         *            the member memory locations
         * @return the updated transaction
         */
        @SuppressWarnings("unchecked")
        public TransactionBuilder<V> addReadSetMembers(Memory<V>... readSetMembers) {
            
            this.transaction.getMetadata().addToReadSet((Object[]) readSetMembers);
            
            return this;
        }
        
        /**
         * Sets the transactions execution logic
         * 
         * @param executionLogic
         *            the execution logic of the transaction
         * @return the updated transaction
         */
        public TransactionBuilder<V> addExecutionLogic(TransactionOperation executionLogic) {
            
            this.transaction.getMetadata().setExecuteTransaction(executionLogic);
            
            return this;
        }
        
        /**
         * @return the constructed transaction
         */
        public Transaction<V> getTransaction() {
            return this.transaction;
        }
    }
    
    /**
     * Takes ownership of memory locations in writeSet
     * 
     * @return the success flag if the ownership was acquired for all write set
     *         members successfully
     */
    private boolean takeOwnership() {
        
        int ownerShipCount = this.metadata.getWriteSet().size();
        
        for (Memory<V> mem : this.metadata.getWriteSet()) {
            
            if (this.stm.isOwned(mem)) {
                
                return false;
            } else {
                
                // take ownership
                this.stm.addOwnership(mem, this.metadata);
                
                ownerShipCount--;
            }
        }
        
        if (ownerShipCount <= 0) {
            
            return true;
        } else {
            
            return false;
        }
    }
    
    /**
     * Rolls back everything
     * 
     * @param rollbackReasonMessage
     *            the message to be printed before rolling back (reason for
     *            rollback)
     */
    @SuppressWarnings("unchecked")
    private void rollback(String rollbackReasonMessage) {
        
        Logger.logMessage(rollbackReasonMessage);
        
        for (Memory<V> mem : this.metadata.getWriteSet()) {
            
            if (this.stm.isOwned(mem) && this.stm.isOwnedBy(mem, this.metadata)) {
                
                // release ownership
                this.stm.releaseOwnership(mem);
                
                // restore the old value of the memory location from backup
                if (this.metadata.getOldValues().containsKey(mem)) {
                    
                    mem.setVal((V) this.metadata.getOldValues().get(mem));
                }
            }
        }
        
        // update the fail count
        this.incrementFailCount();
        
        // adds itself to the job queue for retry
        this.stm.addToJobQueue(this);
    }
    
    /**
     * Takes a backup for all the values in the memory location and stores it in
     * the
     * oldvalues
     */
    private void takeBackup() {
        
        for (Memory<V> mem : this.metadata.getWriteSet()) {
            
            Logger.logMessage(" Backing up W:: " + mem);
            
            this.metadata.getOldValues().put(mem, mem.getVal());
            
            Logger.logMessage(" Backed up W:: " + mem);
        }
        
        for (Memory<V> mem : this.metadata.getReadSet()) {
            
            Logger.logMessage(" Backing up R:: " + mem);
            
            this.metadata.getOldValues().put(mem, mem.getVal());
            
            Logger.logMessage(" Backed up R:: " + mem);
        }
    }
    
    /**
     * Commit operation
     * 
     * @return the status of the commit operation
     */
    private boolean commit() {
        
        // release all the ownerships from write set
        for (Memory<V> mem : this.metadata.getWriteSet()) {
            
            if (this.stm.isOwned(mem) && this.stm.isOwnedBy(mem, this.metadata)) {
                
                // release ownership
                this.stm.releaseOwnership(mem);
            }
        }
        
        // verify that the readSet's values haven't been modified by other
        // transactions, if yes
        // fail commit
        for (Memory<V> mem : this.metadata.getReadSet()) {
            
            if (mem.getVal() != (Integer) this.metadata.getOldValues().get(mem)) {
                
                return false;
            }
        }
        
        return true;
    }
    
    /*
     * When a transaction runs
     */
    @Override
    public void run() {
        
        Logger.logMessage(" WriteSet size :: " + this.metadata.getWriteSet().size());
        Logger.logMessage(" ReadSet size :: " + this.metadata.getReadSet().size());
        
        Logger.logMessage(" Taking ownership for writeSet");
        
        boolean success = takeOwnership();
        
        if (!success) {
            
            rollback(" I failed to take ownership, hence rolling back");
            
            return;
        }
        
        Logger.logMessage(" Taking backup...");
        // take a backup in case transaction commit fails
        takeBackup();
        
        Logger.logMessage(" Executing transaction...");
        
        // execute the transaction
        this.metadata.getExecuteTransaction().execute();
        
        Logger.logMessage(" Initiating commit");
        
        boolean commitStatus = false;
        
        try {
            
            commitStatus = commit();
        } catch (Exception e) {
            
            Logger.logMessage(" Failed commit!!");
            
            e.printStackTrace();
        }
        
        if (!commitStatus) {
            
            Logger.logMessage(" Commit failed hence rolling back changes");
            
            rollback("I failed to commit, hence rolling back");
            
            Logger.logMessage(" Commit failed hence rolled back changes");
            
            return;
        } else {
            
            Logger.logMessage(" Committed changes successfully");
        }
    }
}
