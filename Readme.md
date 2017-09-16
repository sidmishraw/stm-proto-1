# STM Prototype


## Author: Sidharth Mishra
## Advisor: Dr. Jon Pearce 

This is a prototype that simulates the operating principle mentioned in the paper `Software Transactional Memory by Michel Weimerskirch`.

For detailed code/output walkthrough please refer to the [output.md](./ops/output.md) file.


After running the simulation 20 times, for the 3 transactions, the metrics are below::
```
Final metrics::
T1s failed:: 8
T2s failed:: 7
T3s failed:: 35
```

The count is the number of times a transaction had to retry before succeeding in all 20 runs.
These 20 runs were actual program runs.



Changelog:
9/15/2017: Refactored codebase and made it more generic
9/15/2017: Added Object-State pattern with the STM managing the state of the object