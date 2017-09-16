/**
 * Project: stm
 * Package: states
 * File: VariableOperation.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 15, 2017 6:28:06 PM
 */
package states;

/**
 * @author sidmishraw
 *
 *         Qualified Name: states.VariableOperation
 *
 */
public interface VariableOperation {
    
    /**
     * The operations
     * 
     * @author sidmishraw
     *
     *         Qualified Name: states.Operations
     *
     */
    public static enum Operations {
        
        ADD, SUBTRACT, MULTIPLY, DIVIDE;
    }
    
    /**
     * The operation currently is an integral operation
     * 
     * @param currentValue
     *            the currentValue
     * @param operation
     *            the operation
     * @param incomingValue
     *            the incoming value
     * @return the new computed value
     */
    public static Object operate(Object currentValue, Operations operation, Object incomingValue) {
        
        if (null == currentValue) {
            
            currentValue = 0;
        }
        
        if (currentValue instanceof Integer && incomingValue instanceof Integer) {
            
            switch (operation) {
                
                case ADD:
                    return (int) currentValue + (int) incomingValue;
                
                case SUBTRACT:
                    return (int) currentValue - (int) incomingValue;
                
                case MULTIPLY:
                    return (int) currentValue * (int) incomingValue;
                
                case DIVIDE:
                    return (int) currentValue / (int) incomingValue;
            }
        }
        
        return null;
    }
}
