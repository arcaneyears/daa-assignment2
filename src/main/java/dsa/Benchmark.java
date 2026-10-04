package dsa;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public final class Benchmark {

    private static final int[] SIZES = {100, 1000, 10000, 100000};
    private static final int SEED = 42;
    private static final int WARMUP_RUNS = 3;
    private static final int WARMUP_SIZE = 5000;
    private static final int MEASURED_RUNS = 5;
    private static final int ACCESS_QUERIES = 10000;
    private static final int SEARCH_QUERIES = 1000;
    private static final int EDIT_OPS = 1000;
    private static final int VALUE_BOUND = 500000;

    private static volatile long sink;

    private interface Job {
        Metrics setUp();

        void execute();
    }

    private static final class Result {
        double timeMs;
        long steps;
        long moves;
        long comparisons;
    }

    private Benchmark() {
    }

    public static Path run(Path output) throws IOException {
        warmUpJit();
        StringBuilder csv = new StringBuilder("workload,variant,structure,n,time_ms,steps,moves,comparisons\n");
        for (int n : SIZES) {
            int[] base = baseData(n);
            int[] indices = accessIndices(n);
            int[] queries = searchQueries(n, base);
            int[] values = editValues();
            for (int kind = 0; kind < 2; kind++) {
                String structure = name(kind);
                System.out.println("n=" + n + " " + structure);
                append(csv, "W1", "-", structure, n, measure(accessJob(kind, base, indices)));
                append(csv, "W2", "-", structure, n, measure(searchJob(kind, base, queries)));
                append(csv, "W3", "head", structure, n, measure(editJob(kind, base, values, 0)));
                append(csv, "W3", "middle", structure, n, measure(editJob(kind, base, values, n / 2)));
            }
            System.out.println("n=" + n + " MinHeap");
            append(csv, "W4", "-", "MinHeap", n, measure(heapJob(base)));
        }
        Files.createDirectories(output.getParent());
        Files.writeString(output, csv.toString(), StandardCharsets.UTF_8);
        return output;
    }

    private static void warmUpJit() {
        int[] base = baseData(WARMUP_SIZE);
        int[] indices = accessIndices(WARMUP_SIZE);
        int[] queries = searchQueries(WARMUP_SIZE, base);
        int[] values = editValues();
        for (int round = 0; round < 3; round++) {
            for (int kind = 0; kind < 2; kind++) {
                measure(accessJob(kind, base, indices));
                measure(searchJob(kind, base, queries));
                measure(editJob(kind, base, values, 0));
                measure(editJob(kind, base, values, WARMUP_SIZE / 2));
            }
            measure(heapJob(base));
        }
    }

    private static Result measure(Job job) {
        long[] times = new long[MEASURED_RUNS];
        Result result = new Result();
        for (int run = 0; run < WARMUP_RUNS + MEASURED_RUNS; run++) {
            Metrics metrics = job.setUp();
            metrics.reset();
            long start = System.nanoTime();
            job.execute();
            long elapsed = System.nanoTime() - start;
            if (run >= WARMUP_RUNS) {
                times[run - WARMUP_RUNS] = elapsed;
                result.steps = metrics.steps();
                result.moves = metrics.moves();
                result.comparisons = metrics.comparisons();
            }
        }
        result.timeMs = median(times) / 1_000_000.0;
        return result;
    }

    private static Job accessJob(int kind, int[] base, int[] indices) {
        return new Job() {
            private IntList list;

            @Override
            public Metrics setUp() {
                list = fill(kind, base);
                return list.metrics();
            }

            @Override
            public void execute() {
                long local = 0;
                for (int index : indices) {
                    local += list.get(index);
                }
                sink = local;
            }
        };
    }

    private static Job searchJob(int kind, int[] base, int[] queries) {
        return new Job() {
            private IntList list;

            @Override
            public Metrics setUp() {
                list = fill(kind, base);
                return list.metrics();
            }

            @Override
            public void execute() {
                long local = 0;
                for (int query : queries) {
                    if (list.contains(query)) {
                        local++;
                    }
                }
                sink = local;
            }
        };
    }

    private static Job editJob(int kind, int[] base, int[] values, int index) {
        return new Job() {
            private IntList list;

            @Override
            public Metrics setUp() {
                list = fill(kind, base);
                return list.metrics();
            }

            @Override
            public void execute() {
                long local = 0;
                for (int value : values) {
                    list.add(index, value);
                }
                for (int i = 0; i < EDIT_OPS; i++) {
                    local += list.remove(index);
                }
                sink = local;
            }
        };
    }

    private static Job heapJob(int[] base) {
        return new Job() {
            private MinHeap heap;

            @Override
            public Metrics setUp() {
                heap = new MinHeap();
                return heap.metrics();
            }

            @Override
            public void execute() {
                for (int value : base) {
                    heap.insert(value);
                }
                long previous = Integer.MIN_VALUE;
                long local = 0;
                while (!heap.isEmpty()) {
                    int min = heap.extractMin();
                    if (min < previous) {
                        throw new IllegalStateException("heap output is not sorted");
                    }
                    previous = min;
                    local += min;
                }
                sink = local;
            }
        };
    }

    private static IntList fill(int kind, int[] base) {
        IntList list = kind == 0 ? new DynamicArray() : new MyLinkedList();
        for (int value : base) {
            list.add(value);
        }
        return list;
    }

    private static String name(int kind) {
        return kind == 0 ? "DynamicArray" : "MyLinkedList";
    }

    private static int[] baseData(int n) {
        Random random = new Random(SEED);
        int[] base = new int[n];
        for (int i = 0; i < n; i++) {
            base[i] = random.nextInt(VALUE_BOUND) * 2;
        }
        return base;
    }

    private static int[] accessIndices(int n) {
        Random random = new Random(SEED);
        int[] indices = new int[ACCESS_QUERIES];
        for (int i = 0; i < ACCESS_QUERIES; i++) {
            indices[i] = random.nextInt(n);
        }
        return indices;
    }

    private static int[] searchQueries(int n, int[] base) {
        Random random = new Random(SEED);
        int[] queries = new int[SEARCH_QUERIES];
        for (int i = 0; i < SEARCH_QUERIES; i++) {
            if (i % 2 == 0) {
                queries[i] = base[random.nextInt(n)];
            } else {
                queries[i] = random.nextInt(VALUE_BOUND) * 2 + 1;
            }
        }
        return queries;
    }

    private static int[] editValues() {
        Random random = new Random(SEED);
        int[] values = new int[EDIT_OPS];
        for (int i = 0; i < EDIT_OPS; i++) {
            values[i] = random.nextInt(VALUE_BOUND) * 2;
        }
        return values;
    }

    private static long median(long[] values) {
        long[] copy = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            copy[i] = values[i];
        }
        for (int i = 1; i < copy.length; i++) {
            long current = copy[i];
            int j = i - 1;
            while (j >= 0 && copy[j] > current) {
                copy[j + 1] = copy[j];
                j--;
            }
            copy[j + 1] = current;
        }
        return copy[copy.length / 2];
    }

    private static void append(StringBuilder csv, String workload, String variant, String structure,
                               int n, Result result) {
        csv.append(workload).append(',')
                .append(variant).append(',')
                .append(structure).append(',')
                .append(n).append(',')
                .append(String.format(java.util.Locale.ROOT, "%.3f", result.timeMs)).append(',')
                .append(result.steps).append(',')
                .append(result.moves).append(',')
                .append(result.comparisons).append('\n');
    }
}
