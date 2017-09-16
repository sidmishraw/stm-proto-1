/**
 * Project: stm
 * Package: states
 * File: State.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 15, 2017 5:18:46 PM
 */
package states;

/**
 * @author sidmishraw
 *
 *         Qualified Name: states.State
 *
 *         Description: The state of an object
 */
public class State<V> {
    
    private static int stateCounter = 0;
    
    private int        id;
    private V          value;
    
    /**
     * @param value
     *            of the state
     */
    public State(V value) {
        
        this.id = stateCounter;
        
        stateCounter++;
        
        this.value = value;
    }
    
    /**
     * @return the value
     */
    public V getValue() {
        
        return this.value;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        
        return "State [id= " + this.id + " value=" + this.value + "]";
    }
}
