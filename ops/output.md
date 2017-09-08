# Output analysis for STM prototype


## Run #1
```
Intial state of memory locations
A:: Memory [id=0, val=10]
B:: Memory [id=1, val=10]
C:: Memory [id=2, val=10]
D:: Memory [id=3, val=10]
pool-1-thread-1 WriteSet :: 2
pool-1-thread-1 ReadSet :: 2
pool-1-thread-1 Taking ownership for writeSet
pool-1-thread-2 WriteSet :: 2
pool-1-thread-2 ReadSet :: 2
pool-1-thread-2 Taking ownership for writeSet
pool-1-thread-3 WriteSet :: 3
pool-1-thread-3 ReadSet :: 3
pool-1-thread-3 Taking ownership for writeSet
pool-1-thread-1 Taking backup
pool-1-thread-2 Taking backup
pool-1-thread-2 Backing up W:: Memory [id=1, val=10]
pool-1-thread-1 Backing up W:: Memory [id=3, val=10]
pool-1-thread-2 Backed up W:: Memory [id=1, val=10]
pool-1-thread-2 Backing up W:: Memory [id=2, val=10]
pool-1-thread-2 Backed up W:: Memory [id=2, val=10]
pool-1-thread-2 Executing transaction
pool-1-thread-1 Backed up W:: Memory [id=3, val=10]
pool-1-thread-1 Backing up W:: Memory [id=0, val=10]
pool-1-thread-1 Backed up W:: Memory [id=0, val=10]
pool-1-thread-1 Executing transaction
pool-1-thread-2 Initiating commit
pool-1-thread-1 Initiating commit
pool-1-thread-2 Committed changes successfully
pool-1-thread-3 Taking backup
pool-1-thread-1 Committed changes successfully
pool-1-thread-3 Backing up W:: Memory [id=1, val=60]
pool-1-thread-3 Backed up W:: Memory [id=1, val=60]
pool-1-thread-3 Backing up W:: Memory [id=2, val=-40]
pool-1-thread-3 Backed up W:: Memory [id=2, val=-40]
pool-1-thread-3 Backing up W:: Memory [id=0, val=-10]
pool-1-thread-3 Backed up W:: Memory [id=0, val=-10]
pool-1-thread-3 Executing transaction
pool-1-thread-3 Initiating commit
pool-1-thread-3 Committed changes successfully
Final state of memory locations
A:: Memory [id=0, val=-40]
B:: Memory [id=1, val=40]
C:: Memory [id=2, val=10]
D:: Memory [id=3, val=30]
```

## First, Thread1 and Thread2 were able to get ownership:


### Thread1 -> W.S (D,A) = t1 (transaction #1)
```
L: 21 = pool-1-thread-1 Backed up W:: Memory [id=3, val=10]
L: 28 = pool-1-thread-1 Backed up W:: Memory [id=0, val=10]

```

### Thread2 -> W.S(B,C) = t2 (transaction #2)
```
L: 22 = pool-1-thread-2 Backed up W:: Memory [id=1, val=10]
L: 24 = pool-1-thread-2 Backed up W:: Memory [id=2, val=10]
```

After commit of t1 the values should be:
```
// A = A - 20 = 10 - 20 = -10 
// D = D + 20 = 10 + 20 = 30
```

After commit of t2 the values should be:
```
// B = B + 50 = 10 + 50 = 60
// C = C - 50 = 10 - 50 = -40
```

After commit of t3 the values should be: (if t3 is able to take ownership at all)
```
// A = A - 30 = -10 - 30 = -40
// B = B - 20 = 60 - 20 = 40
// C = C + 50 = -40 + 50 = 10
```

So final result should be:
```
A = -40
B = 40
C = 10
D = 30
```

The program gives the same output::
```
Final state of memory locations
A:: Memory [id=0, val=-40]
B:: Memory [id=1, val=40]
C:: Memory [id=2, val=10]
D:: Memory [id=3, val=30]
```

So, consistency is maintained in the above run. Trying out again for `Run#2`.


## Run #2 (Without retrial)
```
Intial state of memory locations
A:: Memory [id=0, val=10]
B:: Memory [id=1, val=10]
C:: Memory [id=2, val=10]
D:: Memory [id=3, val=10]
pool-1-thread-1 WriteSet :: 2
pool-1-thread-1 ReadSet :: 2
pool-1-thread-2 WriteSet :: 2
pool-1-thread-2 ReadSet :: 2
pool-1-thread-2 Taking ownership for writeSet
pool-1-thread-1 Taking ownership for writeSet
pool-1-thread-2 Taking backup
pool-1-thread-3 WriteSet :: 3
pool-1-thread-3 ReadSet :: 3
pool-1-thread-3 Taking ownership for writeSet
pool-1-thread-3 I failed to take ownership, hence rolling back
pool-1-thread-2 Backing up W:: Memory [id=1, val=10]
pool-1-thread-2 Backed up W:: Memory [id=1, val=10]
pool-1-thread-2 Backing up W:: Memory [id=2, val=10]
pool-1-thread-2 Backed up W:: Memory [id=2, val=10]
pool-1-thread-2 Executing transaction
pool-1-thread-1 Taking backup
pool-1-thread-2 Initiating commit
pool-1-thread-1 Backing up W:: Memory [id=3, val=10]
pool-1-thread-1 Backed up W:: Memory [id=3, val=10]
pool-1-thread-1 Backing up W:: Memory [id=0, val=10]
pool-1-thread-1 Backed up W:: Memory [id=0, val=10]
pool-1-thread-1 Executing transaction
pool-1-thread-2 Committed changes successfully
pool-1-thread-1 Initiating commit
pool-1-thread-1 Committed changes successfully
Final state of memory locations
A:: Memory [id=0, val=-10]
B:: Memory [id=1, val=60]
C:: Memory [id=2, val=-40]
D:: Memory [id=3, val=30]
```

In the above run, t3 was unable to take ownership for the Memory locations in it's write set so it rolled back and didn't execute.

So, the final State will be without t3 being executed:
```
A = -10
B = 60
C = -40
D = 30
```

Adding a retry mechanism
## Run #3 (With retry for failed transactions)
```
Intial state of memory locations
A:: Memory [id=0, val=10]
B:: Memory [id=1, val=10]
C:: Memory [id=2, val=10]
D:: Memory [id=3, val=10]
pool-1-thread-1 WriteSet :: 2
pool-1-thread-2 WriteSet :: 2
pool-1-thread-2 ReadSet :: 2
pool-1-thread-1 ReadSet :: 2
pool-1-thread-1 Taking ownership for writeSet
pool-1-thread-2 Taking ownership for writeSet
pool-1-thread-3 WriteSet :: 3
pool-1-thread-3 ReadSet :: 3
pool-1-thread-3 Taking ownership for writeSet
pool-1-thread-3 Taking backup
pool-1-thread-1 I failed to take ownership, hence rolling back
pool-1-thread-3 Backing up W:: Memory [id=1, val=10]
pool-1-thread-1 WriteSet :: 2
pool-1-thread-2 I failed to take ownership, hence rolling back
pool-1-thread-1 ReadSet :: 2
pool-1-thread-1 Taking ownership for writeSet
pool-1-thread-1 I failed to take ownership, hence rolling back
pool-1-thread-3 Backed up W:: Memory [id=1, val=10]
pool-1-thread-1 WriteSet :: 2
pool-1-thread-1 ReadSet :: 2
pool-1-thread-1 Taking ownership for writeSet
pool-1-thread-3 Backing up W:: Memory [id=2, val=10]
pool-1-thread-2 WriteSet :: 2
pool-1-thread-2 ReadSet :: 2
pool-1-thread-2 Taking ownership for writeSet
pool-1-thread-3 Backed up W:: Memory [id=2, val=10]
pool-1-thread-3 Backing up W:: Memory [id=0, val=10]
pool-1-thread-1 I failed to take ownership, hence rolling back
pool-1-thread-3 Backed up W:: Memory [id=0, val=10]
pool-1-thread-3 Executing transaction
pool-1-thread-2 I failed to take ownership, hence rolling back
pool-1-thread-3 Initiating commit
pool-1-thread-1 WriteSet :: 2
pool-1-thread-1 ReadSet :: 2
pool-1-thread-1 Taking ownership for writeSet
pool-1-thread-1 Taking backup
pool-1-thread-1 Backing up W:: Memory [id=3, val=10]
pool-1-thread-1 Backed up W:: Memory [id=3, val=10]
pool-1-thread-1 Backing up W:: Memory [id=0, val=-20]
pool-1-thread-1 Backed up W:: Memory [id=0, val=-20]
pool-1-thread-1 Executing transaction
pool-1-thread-1 Initiating commit
pool-1-thread-1 Committed changes successfully
pool-1-thread-3 Committed changes successfully
pool-1-thread-2 WriteSet :: 2
pool-1-thread-2 ReadSet :: 2
pool-1-thread-2 Taking ownership for writeSet
pool-1-thread-2 Taking backup
pool-1-thread-2 Backing up W:: Memory [id=1, val=-10]
pool-1-thread-2 Backed up W:: Memory [id=1, val=-10]
pool-1-thread-2 Backing up W:: Memory [id=2, val=60]
pool-1-thread-2 Backed up W:: Memory [id=2, val=60]
pool-1-thread-2 Executing transaction
pool-1-thread-2 Initiating commit
pool-1-thread-2 Committed changes successfully
Final state of memory locations
A:: Memory [id=0, val=-40]
B:: Memory [id=1, val=40]
C:: Memory [id=2, val=10]
D:: Memory [id=3, val=30]
```

Consistency is maintained!

- Sid