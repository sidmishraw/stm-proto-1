import states.StateManager;
import states.Variable;
import states.VariableOperation.Operations;
import stm.Memory;
import stm.STM;
import stm.Transaction;
import stm.Transaction.TransactionBuilder;
import utils.Poller;

/**
 * Project: stm
 * Package:
 * File: MainDriver.java
 * 
 * @author sidmishraw
 *         Last modified: Sep 15, 2017 5:24:42 PM
 */

/**
 * @author sidmishraw
 *
 *         Qualified Name: .MainDriver
 *
 *         The main driver of the project/application
 */
public class MainDriver {
    
    private static int[]                       tFailCounts = { 0, 0, 0 };
    
    // the STM manager
    private static final STM                   stm         = STM.getInstance();
    
    @SuppressWarnings("unchecked")
    private static final StateManager<Integer> manager     = (StateManager<Integer>) StateManager.getInstance();
    
    @SuppressWarnings({ "unchecked" })
    private static void simpleSTMDemo(int roundNbr) {
        
        // memory locations each with initial value of 10
        Memory<Integer> A = new Memory<Integer>(10);
        Memory<Integer> B = new Memory<Integer>(10);
        Memory<Integer> C = new Memory<Integer>(10);
        Memory<Integer> D = new Memory<Integer>(10);
        
        // Make 3 transactions
        
        // t1 does the following operations
        // A = A - 20
        // D = D + 20
        // this should fail if t3 takes ownership
        Transaction<Integer> t1 = TransactionBuilder.getInstance().buildNewTransaction().addWriteSetMembers(A, D)
                .addReadSetMembers(A, D).addExecutionLogic(() -> {
                    A.setVal(A.getVal() - 20);
                    D.setVal(D.getVal() + 20);
                }).getTransaction();
        
        // t2 does the following operations
        // B = B + 50
        // C = C - 50
        // this should fail if t3 takes ownership
        Transaction<Integer> t2 = TransactionBuilder.getInstance().buildNewTransaction().addWriteSetMembers(B, C)
                .addReadSetMembers(B, C).addExecutionLogic(() -> {
                    B.setVal(B.getVal() + 50);
                    C.setVal(C.getVal() - 50);
                }).getTransaction();
        
        // t3 does the following operations
        // A = A - 30
        // B = B - 20
        // C = C + 50
        // this should fail once t1 and t2 take ownership
        Transaction<Integer> t3 = TransactionBuilder.getInstance().buildNewTransaction().addWriteSetMembers(A, B, C)
                .addReadSetMembers(A, B, C).addExecutionLogic(() -> {
                    A.setVal(A.getVal() - 30);
                    B.setVal(B.getVal() - 20);
                    C.setVal(C.getVal() + 50);
                }).getTransaction();
        
        stm.addToJobQueue(t1, t2, t3);
        
        System.out.println("Intial state of memory locations round#" + roundNbr);
        System.out.println("A:: " + A.toString());
        System.out.println("B:: " + B.toString());
        System.out.println("C:: " + C.toString());
        System.out.println("D:: " + D.toString());
        
        Thread tt = new Thread(stm);
        Thread poller = new Thread(new Poller());
        
        tt.start();
        poller.start();
        
        try {
            
            // wait for STM to finish
            tt.join();
        } catch (InterruptedException e) {
            
            e.printStackTrace();
        }
        
        System.out.println("Final state of memory locations round#" + roundNbr);
        System.out.println("A:: " + A.toString());
        System.out.println("B:: " + B.toString());
        System.out.println("C:: " + C.toString());
        System.out.println("D:: " + D.toString());
        
        System.out.println("\n\n Transaction metrics");
        System.out.println("t1: Fail counts = " + t1.getFailedCount());
        System.out.println("t2: Fail counts = " + t2.getFailedCount());
        System.out.println("t3: Fail counts = " + t3.getFailedCount());
        
        // collect metrics
        tFailCounts[0] += t1.getFailedCount();
        tFailCounts[1] += t2.getFailedCount();
        tFailCounts[2] += t3.getFailedCount();
    }
    
    /**
     * object - state demo
     */
    @SuppressWarnings("unchecked")
    private static void objectDemo(int roundNbr) {
        
        Variable A = manager.makeVariable();
        Variable B = manager.makeVariable();
        Variable C = manager.makeVariable();
        Variable D = manager.makeVariable();
        
        // Make 3 transactions
        
        // t1 does the following operations
        // A = A - 20
        // D = D + 20
        // this should fail if t3 takes ownership
        Transaction<Integer> t1 = TransactionBuilder.getInstance().buildNewTransaction().addWriteSetMembers(A, D)
                .addReadSetMembers(A, D).addExecutionLogic(() -> {
                    manager.operateOnVariable(A, Operations.SUBTRACT, 20);
                    manager.operateOnVariable(D, Operations.ADD, 20);
                }).getTransaction();
        
        // t2 does the following operations
        // B = B + 50
        // C = C - 50
        // this should fail if t3 takes ownership
        Transaction<Integer> t2 = TransactionBuilder.getInstance().buildNewTransaction().addWriteSetMembers(B, C)
                .addReadSetMembers(B, C).addExecutionLogic(() -> {
                    manager.operateOnVariable(B, Operations.ADD, 50);
                    manager.operateOnVariable(C, Operations.SUBTRACT, 50);
                }).getTransaction();
        
        // t3 does the following operations
        // A = A - 30
        // B = B - 20
        // C = C + 50
        // this should fail once t1 and t2 take ownership
        Transaction<Integer> t3 = TransactionBuilder.getInstance().buildNewTransaction().addWriteSetMembers(A, B, C)
                .addReadSetMembers(A, B, C).addExecutionLogic(() -> {
                    manager.operateOnVariable(A, Operations.SUBTRACT, 30);
                    manager.operateOnVariable(B, Operations.SUBTRACT, 20);
                    manager.operateOnVariable(C, Operations.ADD, 50);
                }).getTransaction();
        
        stm.addToJobQueue(t1, t2, t3);
        
        System.out.println("Intial state of memory locations for round#" + roundNbr);
        System.out.println("A:: " + manager.getCurrentState(A));
        System.out.println("B:: " + manager.getCurrentState(B));
        System.out.println("C:: " + manager.getCurrentState(C));
        System.out.println("D:: " + manager.getCurrentState(D));
        
        Thread tt = new Thread(stm);
        Thread poller = new Thread(new Poller());
        
        tt.start();
        poller.start();
        
        try {
            
            // wait for STM to finish
            tt.join();
        } catch (InterruptedException e) {
            
            e.printStackTrace();
        }
        
        System.out.println("Final state of memory locations for round#" + roundNbr);
        System.out.println("A:: " + manager.getCurrentState(A));
        System.out.println("B:: " + manager.getCurrentState(B));
        System.out.println("C:: " + manager.getCurrentState(C));
        System.out.println("D:: " + manager.getCurrentState(D));
        
        System.out.println("\n\n Transaction metrics");
        System.out.println("t1: Fail counts = " + t1.getFailedCount());
        System.out.println("t2: Fail counts = " + t2.getFailedCount());
        System.out.println("t3: Fail counts = " + t3.getFailedCount());
        
        // collect metrics
        tFailCounts[0] += t1.getFailedCount();
        tFailCounts[1] += t2.getFailedCount();
        tFailCounts[2] += t3.getFailedCount();
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        
        int i = 20;
        
        while (i > 0) {
            
            // simple STM mechanism demo
            // simpleSTMDemo(i);
            
            // Object - state separation demo
            objectDemo(i);
            
            i--;
            
            stm.reset();
            manager.reset();
        }
        
        System.out.println("\n\nFinal metrics::");
        System.out.println("T1s failed:: " + tFailCounts[0]);
        System.out.println("T2s failed:: " + tFailCounts[1]);
        System.out.println("T3s failed:: " + tFailCounts[2]);
    }
}
