# This is a single-order Markov model used in BlueJam for
# selecting notes from a scaled set.
#
# Extra spaces don't matter, as long as there is one 
# between each value. The parser will expect the column
# headers to mirror the rows, as shown below.
#
# All rows should total 1.0
# The pitch starting the row should be in the pitch Enum.
#
# Scaled sets don't have to use these models,
# but they might get better results if they do.
#
#
#    Gb    A    B    C     Db    E
Gb 0.10  0.25 0.175 0.05 0.175 0.25 
A  0.25  0.10 0.25  0.05 0.175 0.175
B  0.175 0.20 0.10  0.20 0.15  0.175 
C  0.05  0.05 0.425 0    0.425 0.05
Db 0.175 0.175 0.15 0.20 0.10  0.20 
E  0.25 0.175 0.175 0.05 0.25  0.10