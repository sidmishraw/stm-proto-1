/**
 * Project: stm
 * Package: states
 * File: Value.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 15, 2017 5:19:21 PM
 */
package states;

/**
 * @author sidmishraw
 *
 *         Qualified Name: states.Variable
 *
 *         Value is the object whose state changes
 */
public class Variable {
    
    private static int variableCounter = 0;
    
    private Integer    id;
    
    /**
     * 
     */
    public Variable() {
        
        this.id = variableCounter;
        variableCounter++;
    }
    
    /**
     * @return the id
     */
    public Integer getId() {
        return this.id;
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
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
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
        if (!(obj instanceof Variable)) {
            return false;
        }
        Variable other = (Variable) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
