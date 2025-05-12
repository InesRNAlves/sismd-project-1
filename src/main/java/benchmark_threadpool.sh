#!/bin/bash

# Configurations
GC_TYPES=("G1GC" "ZGC" "ParallelGC" "ShenandoahGC")
REPEATS=5
CSV_FILE="benchmark_threadpool_full_dataset_3_no_gc_tunning.csv"
JAVA_OPTS="-Xms512m -Xmx512m"
CLASS="ThreadPools.ThreadPools"

# Compile the program
javac ThreadPools/ThreadPools.java || { echo "Compilation failed"; exit 1; }

# Initialize CSV
echo "GC_TYPE,AVG_TIME_MS,AVG_MEMORY_MB,AVG_CPU_PERCENT" > "$CSV_FILE"

run_benchmark() {
  local gc=$1

  echo "Running GC=$gc | ($REPEATS runs)..."
  total_time=0
  total_mem=0
  total_cpu=0

  gc_flag=""
  case "$gc" in
    G1GC) gc_flag="-XX:+UseG1GC" ;;
    ZGC) gc_flag="-XX:+UseZGC" ;;
    ParallelGC) gc_flag="-XX:+UseParallelGC" ;;
    ShenandoahGC) gc_flag="-XX:+UseShenandoahGC" ;;
    *) echo "Unknown GC: $gc"; return ;;
  esac
  for ((i = 1; i <= REPEATS; i++)); do
    echo "  Run $i/$REPEATS..."

    # Capture output and CPU %
    output=$(gtime -f "%P" -o cpu.tmp java $gc_flag $CLASS | grep -E '^[0-9]+')
    cpu_usage=$(cat cpu.tmp | tr -d '%')

    IFS=',' read -r time memory <<< "$output"

    total_time=$((total_time + time))
    total_mem=$((total_mem + memory))
    total_cpu=$(echo "$total_cpu + $cpu_usage" | bc)
  done

  # Averages
  avg_time=$((total_time / REPEATS))
  avg_mem=$((total_mem / REPEATS))
  avg_cpu=$(echo "scale=2; $total_cpu / $REPEATS" | bc)

  echo "$gc,$avg_time,$avg_mem,$avg_cpu" >> "$CSV_FILE"
}

# Run all combinations
for gc in "${GC_TYPES[@]}"; do
  run_benchmark "$gc"
done

echo "Benchmark complete. Results saved to $CSV_FILE."