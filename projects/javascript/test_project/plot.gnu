datafile = ARG1
set xrange [1:189000]
set yrange [0:20]

set xlabel "Time in ms"
set ylabel "Rendertime in ms"

plot for [i=0:*] datafile index i using 1:2\
smooth csplines title columnheader(1)
