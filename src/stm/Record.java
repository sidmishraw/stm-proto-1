/**
 * Project: stm
 * Package: stm
 * File: Record.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 15, 2017 1:58:48 PM
 */
package stm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import stm.Operations.TransactionOperation;

/**
 * @author sidmishraw
 *
 *         Qualified Name: stm.Record
 *
 *         A record is the transaction metadata
 */
public class Record<V> {
    
    static int                   counter            = 0;
    
    private int                  id;
    
    private Set<Memory<V>>       writeSet           = new HashSet<>();
    private Set<Memory<V>>       readSet            = new HashSet<>();
    private Map<Memory<V>, V>    oldValues          = new HashMap<>();
    
    private TransactionOperation executeTransaction = () -> {};
    
    /**
     * 
     */
    public Record() {
        
        this.id = counter;
        counter++;
    }
    
    /**
     * Adds the memory to readSet
     * 
     * @param memories
     *            the memories that need to be written/read (updated)
     */
    @SuppressWarnings("unchecked")
    public void addToWriteSet(Object... memories) {
        
        Arrays.asList(memories).forEach(memory -> {
            
            this.writeSet.add((Memory<V>) memory);
        });
    }
    
    /**
     * Adds the memory to readSet
     * 
     * @param memories
     *            the memories that need to be written/read (updated)
     */
    @SuppressWarnings("unchecked")
    public void addToReadSet(Object... memories) {
        
        Arrays.asList(memories).forEach(memory -> {
            
            if (!this.writeSet.contains(memory)) {
                
                this.readSet.add((Memory<V>) memory);
            }
        });
    }
    
    /**
     * @return the executeTransaction
     */
    public TransactionOperation getExecuteTransaction() {
        return this.executeTransaction;
    }
    
    /**
     * @param executeTransaction
     *            the executeTransaction to set
     */
    public void setExecuteTransaction(TransactionOperation executeTransaction) {
        this.executeTransaction = executeTransaction;
    }
    
    /**
     * @return the id
     */
    public int getId() {
        return this.id;
    }
    
    /**
     * @return the writeSet
     */
    public Set<Memory<V>> getWriteSet() {
        return this.writeSet;
    }
    
    /**
     * @return the readSet
     */
    public Set<Memory<V>> getReadSet() {
        return this.readSet;
    }
    
    /**
     * @return the oldValues
     */
    public Map<Memory<V>, V> getOldValues() {
        return this.oldValues;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.id;
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Record)) {
            return false;
        }
        Record<V> other = (Record<V>) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
}
