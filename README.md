# DAA Assignment 2 - In-Memory Workload Engine

Author: Taubakabyl Nurlybek

Three data structures written from scratch over `int[]` and nodes, with operation counters,
a benchmark over 4 workloads and generated plots.

## Layout

```
src/main/java/dsa/   DynamicArray, MyLinkedList, MinHeap, Metrics, Benchmark, Plots, Main
src/test/java/dsa/   JUnit 5 tests
results/results.csv  benchmark output
results/plots/       PNG charts
REPORT.md            complexity table, loop invariants, plots, discussion
```

## Requirements

JDK 21+ and Maven 3.8+.

## Build and test

```bash
mvn clean test
```

## Run the benchmark and generate the plots

```bash
mvn -q compile
java -cp target/classes dsa.Main
```

or with Maven only:

```bash
mvn -q compile exec:java
```

The run writes `results/results.csv` and the PNG charts in `results/plots/`.
To redraw the charts from an existing CSV without re-running the benchmark:

```bash
java -cp target/classes dsa.Main plots
```

## Benchmark parameters

- sizes: n = 100, 1 000, 10 000, 100 000
- data: `new Random(42)`, same values for every structure
- 3 warm-up runs + 5 measured runs per case, median time is reported
- W1: 10 000 `get(random index)`
- W2: 1 000 `contains(x)`, half present and half absent
- W3: 1 000 insertions + 1 000 removals at index 0 (`head`) and at index n/2 (`middle`)
- W4: n `insert(x)` + n `extractMin()` with a non-decreasing order check

## CSV format

```
workload,variant,structure,n,time_ms,steps,moves,comparisons
```

`variant` is `head` or `middle` for W3 and `-` for the other workloads.
Counters are incremented inside the operations: `steps` - array cell read or node hop,
`moves` - array element shift or pointer update, `comparisons` - comparison of two elements.
