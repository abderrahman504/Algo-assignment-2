# Progress Report

### Weighted Activity Selection
The weighted activity selection program is almost finished.  
It reads either a default input file (test.in) or a file from a specified path and solves the problem from the file.  
It then prints the solution to the console.
Later it will be made to write the solution to an output file.  
The algorithm runs in O(nlogn) time. It first sorts the activities in terms of starting time. Then it uses a top-down DP structure.  
Each problem _i_ compares the weights if activity _i_ is taken or not. If the activity is taken then _weight_ = _wi_ + subproblem _x_ where x is the next activity that doesn't overlap with _i_.  
If the activity isn't taken then the _weight_ = subproblem _i_+1  



### Huffman Code
No progress has been made yet.