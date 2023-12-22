# Assignment 2


## Weighted Activity Selection
The algorithm runs in O(nlogn) time. It first sorts the activities in terms of starting time. Then it uses a top-down DP structure.  
Each problem _i_ compares the weights if activity _i_ is taken or not. If the activity is taken then _weight_ = _wi_ + subproblem _x_ where x is the next activity that doesn't overlap with _i_.  
If the activity isn't taken then the _weight_ = subproblem _i_+1.  
The two weights are compared and the max is taken.

## Huffman Code
