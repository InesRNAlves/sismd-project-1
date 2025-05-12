#!/bin/bash

THRESHOLD_VALUES=(10 100 200 300 500 1000 1500 2000 2500 3000 4000 5000)  # Various threshold values to test
GC_TYPES=("UseG1GC") #"UseParallelGC" "UseSerialGC" "UseZGC" "UseShenandoahGC"
CSV_FILE="benchmark_forkjoin_test_threshold.csv"
REPEATS=10

# Compile the program
javac ForkJoinPool/ForkJoinPoolApproach.java

# Header for CSV
echo "THRESHOLD,AVG_TIME_MS,AVG_MEMORY_MB" > $CSV_FILE

# Run benchmark
for GC in "${GC_TYPES[@]}"; do
  echo "Testing GC: -XX:+$GC"
  for THRESHOLD in "${THRESHOLD_VALUES[@]}"; do
    echo "Running threshold value $THRESHOLD ($REPEATS times)..."

    total_time=0
    total_memory=0

    for ((i=1; i<=REPEATS; i++)); do
      echo "  Run $i/$REPEATS..."
      output=$(java -XX:+$GC ThreadPools.ThreadPools $THRESHOLD | grep -E '^[0-9]+')

      # Expected output format: TIME_MS,MEMORY_MB,PAGES
      IFS=',' read -r time memory <<< "$output"

      total_time=$((total_time + time))
      total_memory=$((total_memory + memory))
    done

    avg_time=$((total_time / REPEATS))
    avg_memory=$((total_memory / REPEATS))

    echo "$GC,$THRESHOLD,$avg_time,$avg_memory" >> $CSV_FILE
  done
done

echo "Benchmark completed. Results written to $CSV_FILE."
