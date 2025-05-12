
# Optimizing Large-Scale Data Processing on Multicore Systems

This project processes Wikipedia XML dumps to count word occurrences in the text of pages. It demonstrates various multithreading approaches in Java to efficiently handle large datasets.

## Features

- **Approaches**:
    - **Sequential Approach**
    - **Simple Threads**
    - **Thread Pools**
    - **Fork/Join Framework**
    - **CompletableFuture**
- **Thread-Safe Word Counting**: Uses `ConcurrentHashMap` to store word counts safely across threads.
- **Metrics**: Outputs processing time, memory usage, cpu usage, and the top N most frequent words.
- **CSV-like Output**: Provides results in a format suitable for further analysis.

## Prerequisites

- **Java**: Ensure Java 21 is installed.
- **Maven**: Build and manage dependencies with Maven.

## Setup

1. **Download Wikipedia Dumps**:
    - [Download File 1](https://dumps.wikimedia.org/enwiki/20250201/enwiki-20250201-pages-articles-multistream1.xml-p1p41242.bz2)
    - [Download File 2](https://dumps.wikimedia.org/enwiki/20250201/enwiki-20250201-pages-articles-multistream3.xml-p151574p311329.bz2)

2. **Prepare Resources**:
    - Unpack the downloaded files.
    - Place `enwiki-20250201-pages-articles-multistream1.xml-p1p41242` in the `resources` directory as an XML file and rename it to `enwiki.xml`.
    - Place `enwiki-20250201-pages-articles-multistream3.xml-p151574p311329` in the `resources` directory as an XML file and rename it to `enwiki3.xml`.

3. **Build the Project**:
   ```bash
   mvn clean install