/**
 * Project: stm
 * Package: states
 * File: StateManager.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 15, 2017 6:08:16 PM
 */
package states;

import java.util.HashMap;
import java.util.Map;

import stm.Memory;
import utils.Logger;

/**
 * @author sidmishraw
 *
 *         Qualified Name: states.StateManager
 *
 * @description Manages the objects and their respective states, taking care of
 *              all the updates
 */
public class StateManager<V> {
    
    /********* SETUP -START *********/
    private static StateManager<?> manager = null;
    
    private void initialize() {
        
        this.variableStates = new HashMap<>();
    }
    
    /**
     * 
     */
    private StateManager() {
        
        initialize();
    }
    
    /**
     * Gets up a manager
     * 
     * @return the singleton manager instance
     */
    public static final StateManager<?> getInstance() {
        
        if (null == manager) {
            
            manager = new StateManager<>();
        }
        
        return manager;
    }
    
    /**
     * Resets the StateManager
     */
    public void reset() {
        
        initialize();
    }
    
    /*********** SETUP - END *******/
    
    /**
     * The StateManager is going to hold the mapping between the Variable and
     * it's State.
     * 
     * This will be held inside the variableState mapping.
     * 
     * The twist is that, the State is actually held inside the STM, so the
     * mapping is actually between a Variable and the memory location.
     */
    private Map<Variable, Memory<State<V>>> variableStates = null;
    
    /**
     * Gets you a brand new shiny variable!
     * 
     * Note: The memory location where the variable's state is stored is also
     * made during this process
     * 
     * @return an instance of a new Variable
     */
    public final Variable makeVariable() {
        
        Variable variable = new Variable();
        
        Memory<State<V>> memoryLocation = new Memory<State<V>>(new State<V>(null));
        
        this.variableStates.put(variable, memoryLocation);
        
        return variable;
    }
    
    /**
     * Gets you an instance of a new state
     * 
     * @param value
     *            the value of the state
     * @return a new shiny state
     */
    private final State<V> makeState(V value) {
        
        State<V> state = new State<>(value);
        
        return state;
    }
    
    /**
     * Gets you the current state
     * 
     * @param variable
     *            the variable
     * @return the current state of the variable
     */
    public final State<V> getCurrentState(Variable variable) {
        
        return ((Memory<State<V>>) this.variableStates.get(variable)).getVal();
    }
    
    /**
     * Gets you the current memory location
     * 
     * @param variable
     *            the variable
     * 
     * @return the memory location of the variable holding the state
     */
    public final Memory<State<V>> getCurrentMemory(Variable variable) {
        
        return (Memory<State<V>>) this.variableStates.get(variable);
    }
    
    /**
     * Operates on the variable depending on the operation
     * 
     * @param variable
     *            the variable
     * @param operation
     *            the operation
     * @param value
     *            the incoming value
     */
    @SuppressWarnings("unchecked")
    public final void operateOnVariable(Variable variable, VariableOperation.Operations operation, V value) {
        
        Memory<State<V>> memoryLocation = variableStates.get(variable);
        
        State<V> currentState = memoryLocation.getVal();
        
        V newValue = null;
        
        if (null != currentState) {
            
            newValue = (V) VariableOperation.operate(currentState.getValue(), operation, value);
        }
        
        State<V> newState = makeState(newValue);
        
        memoryLocation.setVal(newState);
        
        Logger.logMessage("State changed from: " + currentState + " to: " + newState);
    }
}
