set xrange [1:600000]
set yrange [0:100]

set xlabel "Time in ms"
set ylabel "Rendertime in ms"

plot "plot.dat" using 1:2 smooth acsplines
