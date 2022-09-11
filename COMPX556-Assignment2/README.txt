Alpha and Max Iterations are hardcoded and can be changes in the main method if needed.

This Project takes one argument which would be the path to the partial latin square text file.

The text file needs in the format of:

8                    // <-- this is the size of the latin square.
6 _ _ _ 3 _ _ _
_ _ _ 5 _ _ _ 3
_ _ _ _ _ 8 1 _
_ 8 _ 7 _ _ _ _     // <-- the matrix itself with ' _ ' as the symbol for a blank.
_ _ 5 _ _ _ 7 _
1 _ 7 _ _ _ _ _
_ _ _ _ _ _ 2 8
_ 3 _ _ _ _ _ 2