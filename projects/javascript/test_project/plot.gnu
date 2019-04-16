set terminal png size 1024,768
datafile = ARG1
set xrange [1:189000]
set yrange [0:1200]

set xlabel "Time in ms"
set ylabel "Rendertime in ms"

plot for [i=0:*] datafile index i using 1:2\
smooth csplines lw 2 title columnheader(1)
