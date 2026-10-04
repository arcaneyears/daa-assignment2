package dsa;

public final class Metrics {

    private long steps;
    private long moves;
    private long comparisons;

    public void step() {
        steps++;
    }

    public void step(long count) {
        steps += count;
    }

    public void move() {
        moves++;
    }

    public void move(long count) {
        moves += count;
    }

    public void compare() {
        comparisons++;
    }

    public long steps() {
        return steps;
    }

    public long moves() {
        return moves;
    }

    public long comparisons() {
        return comparisons;
    }

    public void reset() {
        steps = 0;
        moves = 0;
        comparisons = 0;
    }
}
