/**
 * Project: stm
 * Package: stm
 * File: Memory.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 15, 2017 1:56:51 PM
 */
package stm;

/**
 * @author sidmishraw
 *
 *         Qualified Name: stm.Memory
 *
 */
public class Memory<V> {
    
    private static int counter = 0;
    
    private int        id;
    private V          val;
    
    /**
     * @param val
     *            the value of the memory
     */
    public Memory(V val) {
        
        this.id = counter;
        this.val = val;
        
        counter++;
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
    @Override
    public boolean equals(Object obj) {
        
        if (this == obj) {
            return true;
        }
        
        if (obj == null) {
            return false;
        }
        
        if (!(obj instanceof Memory)) {
            return false;
        }
        
        @SuppressWarnings("unchecked")
        Memory<V> other = (Memory<V>) obj;
        
        if (this.id != other.id) {
            return false;
        }
        
        return true;
    }
    
    /**
     * @return the id
     */
    public int getId() {
        return this.id;
    }
    
    /**
     * @return the val
     */
    public V getVal() {
        return this.val;
    }
    
    /**
     * @param val
     *            the val to set
     */
    public void setVal(V val) {
        this.val = val;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        
        return "Memory [id=" + this.id + ", val=" + this.val.toString() + "]";
    }
}
