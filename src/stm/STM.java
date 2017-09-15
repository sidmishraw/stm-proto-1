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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author sidmishraw
 *
 *         Qualified Name: stm.STM
 *
 */
public class STM {
	
	// ownerships
	private static Map<Memory, Record>			ownerships		= new HashMap<>();
	
	// global job queue
	private static LinkedBlockingQueue<Record>	jobQueue		= new LinkedBlockingQueue<>();
	
	private static boolean						continuePolling	= true;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// memory locations each with initial value of 10
		Memory A = new Memory(10);
		Memory B = new Memory(10);
		Memory C = new Memory(10);
		Memory D = new Memory(10);
		
		// Make 3 transactions
		Record t1 = new Record();
		// t1 does the following operations
		// A = A - 20
		// D = D + 20
		// this should fail if t3 takes ownership
		t1.addToWriteSet(A, D);
		
		t1.addToReadSet(A, D);
		
		t1.executeTransaction = () -> {
			A.val = A.val - 20;
			D.val = D.val + 20;
		};
		
		Record t2 = new Record();
		// t2 does the following operations
		// B = B + 50
		// C = C - 50
		// this should fail if t3 takes ownership
		t2.addToWriteSet(B, C);
		
		t2.addToReadSet(B, C);
		
		t2.executeTransaction = () -> {
			B.val = B.val + 50;
			C.val = C.val - 50;
		};
		
		Record t3 = new Record();
		// t3 does the following operations
		// A = A - 30
		// B = B - 20
		// C = C + 50
		// this should fail once t1 and t2 take ownership
		t3.addToWriteSet(A, B, C);
		
		t3.addToReadSet(A, B, C);
		
		t3.executeTransaction = () -> {
			A.val = A.val - 30;
			B.val = B.val - 20;
			C.val = C.val + 50;
		};
		
		Thread tt = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				ExecutorService threadPool = Executors.newFixedThreadPool(3);
				
				STM.jobQueue.add(t1);
				STM.jobQueue.add(t2);
				STM.jobQueue.add(t3);
				
				System.out.println("Intial state of memory locations");
				System.out.println("A:: " + A.toString());
				System.out.println("B:: " + B.toString());
				System.out.println("C:: " + C.toString());
				System.out.println("D:: " + D.toString());
				
				while (continuePolling) {
					
					Record transaction = null;
					
					if (null != (transaction = STM.jobQueue.poll())) {
						
						threadPool.execute(transaction);
					}
				}
				
				threadPool.shutdown();
				
				try {
					
					threadPool.awaitTermination(1000, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
			}
		});
		
		Thread poller = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				int retries = 1;
				
				while (true) {
					
					if (retries <= 0) {
						
						STM.continuePolling = false;
						return;
					}
					
					if (STM.jobQueue.size() == 0) {
						
						try {
							
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							
							System.out.println("Error in polling thread");
							e.printStackTrace();
						}
						
						retries--;
					}
					
				}
			}
		});
		
		tt.start();
		poller.start();
		
		try {
			
			tt.join();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
		System.out.println("Final state of memory locations");
		System.out.println("A:: " + A.toString());
		System.out.println("B:: " + B.toString());
		System.out.println("C:: " + C.toString());
		System.out.println("D:: " + D.toString());
	}
	
	static class Memory {
		
		private static int	counter	= 0;
		
		private int			id;
		public int			val;
		
		/**
		 * @param val
		 *            the value of the memory
		 */
		public Memory(int val) {
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
			Memory other = (Memory) obj;
			if (this.id != other.id) {
				return false;
			}
			return true;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			
			return "Memory [id=" + this.id + ", val=" + this.val + "]";
		}
	}
	
	@FunctionalInterface
	interface TransactionOperation {
		
		public void execute();
	}
	
	static class Record implements Runnable {
		
		static int	counter	= 0;
		
		private int	id;
		
		/**
		 * 
		 */
		public Record() {
			this.id = counter;
			counter++;
		}
		
		public Set<Memory>			writeSet			= new HashSet<>();
		public Set<Memory>			readSet				= new HashSet<>();
		public Map<Memory, Integer>	oldValues			= new HashMap<>();
		
		public TransactionOperation	executeTransaction	= () -> {};
		
		/**
		 * Adds the memory to readSet
		 * 
		 * @param memories
		 *            the memories that need to be written/read (updated)
		 */
		public void addToWriteSet(Memory... memories) {
			
			Arrays.asList(memories).forEach(memory -> {
				
				this.writeSet.add(memory);
			});
		}
		
		/**
		 * Adds the memory to readSet
		 * 
		 * @param memories
		 *            the memories that need to be written/read (updated)
		 */
		public void addToReadSet(Memory... memories) {
			
			Arrays.asList(memories).forEach(memory -> {
				
				if (!this.writeSet.contains(memory)) {
					
					this.readSet.add(memory);
				}
			});
		}
		
		/**
		 * Takes ownership of memory locations in writeSet
		 * 
		 * @return the success flag if the ownership was acquired for all write
		 *         set members successfully
		 */
		private boolean takeOwnership() {
			
			int ownerShipCount = this.writeSet.size();
			
			for (Memory mem : this.writeSet) {
				
				if (STM.ownerships.containsKey(mem)) {
					
					return false;
				} else {
					
					// take ownership
					STM.ownerships.put(mem, this);
					
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
		 */
		private void rollback(String message) {
			
			printMessage(message);
			
			for (Memory mem : this.writeSet) {
				
				if (STM.ownerships.containsKey(mem) && STM.ownerships.get(mem).equals(this)) {
					
					// release ownership
					STM.ownerships.remove(mem);
					
					// restore the old value of the memory location from backup
					if (this.oldValues.containsKey(mem)) {
						
						mem.val = (Integer) this.oldValues.get(mem);
					}
				}
			}
			
			// adds itself to the job queue for retry
			STM.jobQueue.add(this);
		}
		
		/**
		 * Takes a backup for all the values in the memory location and stores
		 * it in the oldvalues
		 */
		private void takeBackup() {
			
			for (Memory mem : this.writeSet) {
				
				printMessage(" Backing up W:: " + mem);
				
				this.oldValues.put(mem, mem.val);
				
				printMessage(" Backed up W:: " + mem);
			}
			
			for (Memory mem : this.readSet) {
				
				printMessage(" Backing up R:: " + mem);
				
				this.oldValues.put(mem, mem.val);
				
				printMessage(" Backed up R:: " + mem);
			}
		}
		
		/**
		 * Commit operation
		 * 
		 * @return the status of the commit operation
		 */
		private boolean commit() {
			
			// release all the ownerships from write set
			for (Memory mem : this.writeSet) {
				
				if (STM.ownerships.containsKey(mem) && STM.ownerships.get(mem).equals(this)) {
					
					// release ownership
					STM.ownerships.remove(mem);
				}
			}
			
			// verify that the readSet's values haven't been modified by other
			// transactions, if yes
			// fail commit
			for (Memory mem : this.readSet) {
				
				if (mem.val != (Integer) this.oldValues.get(mem)) {
					
					return false;
				}
			}
			
			return true;
		}
		
		/**
		 * Prints the message to STDOUT
		 * 
		 * @param message
		 *            the message
		 */
		private static void printMessage(String message) {
			
			System.out.println(Thread.currentThread().getName() + message);
		}
		/*
		 * When a transaction runs
		 */
		@Override
		public void run() {
			
			printMessage(" WriteSet :: " + this.writeSet.size());
			printMessage(" ReadSet :: " + this.writeSet.size());
			
			printMessage(" Taking ownership for writeSet");
			
			boolean success = takeOwnership();
			
			if (!success) {
				
				rollback(" I failed to take ownership, hence rolling back");
				
				return;
			}
			
			printMessage(" Taking backup");
			// take a backup in case transaction commit fails
			takeBackup();
			
			printMessage(" Executing transaction");
			
			// execute the transaction
			this.executeTransaction.execute();
			
			printMessage(" Initiating commit");
			
			boolean commitStatus = false;
			
			try {
				
				commitStatus = commit();
			} catch (Exception e) {
				
				printMessage(" Failed commit!!");
				
				e.printStackTrace();
			}
			
			if (!commitStatus) {
				
				printMessage(" Commit failed hence rolling back changes");
				
				rollback("I failed to commit, hence rolling back");
				
				printMessage(" Commit failed hence rolled back changes");
				
				return;
			} else {
				
				printMessage(" Committed changes successfully");
			}
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
			if (!(obj instanceof Record)) {
				return false;
			}
			Record other = (Record) obj;
			if (this.id != other.id) {
				return false;
			}
			return true;
		}
	}
}
