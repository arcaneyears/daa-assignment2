package dsa;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public final class Plots {

    private static final int WIDTH = 980;
    private static final int HEIGHT = 620;
    private static final int LEFT = 95;
    private static final int RIGHT = 230;
    private static final int TOP = 60;
    private static final int BOTTOM = 75;

    private static final Color[] COLORS = {
            new Color(31, 119, 180),
            new Color(214, 39, 40),
            new Color(44, 160, 44),
            new Color(255, 127, 14),
            new Color(148, 103, 189),
            new Color(23, 190, 207)
    };

    private static String[] workload;
    private static String[] variant;
    private static String[] structure;
    private static int[] size;
    private static double[] timeMs;
    private static double[] steps;
    private static double[] moves;
    private static double[] comparisons;
    private static int[] sizes;

    private Plots() {
    }

    public static void run(Path csv, Path outputDir) throws IOException {
        load(csv);
        Files.createDirectories(outputDir);
        plotPair("W1", "-", "W1 Random Access (10 000 get calls)", outputDir, "w1");
        plotPair("W2", "-", "W2 Search (1 000 contains queries)", outputDir, "w2");
        plotPair("W3", "head", "W3 Insert & Remove at head (1 000 + 1 000)", outputDir, "w3_head");
        plotPair("W3", "middle", "W3 Insert & Remove at n/2 (1 000 + 1 000)", outputDir, "w3_middle");
        plotPair("W4", "-", "W4 Priority Processing (n insert + n extractMin)", outputDir, "w4");
    }

    private static void plotPair(String w, String v, String title, Path dir, String prefix) throws IOException {
        String[] structures = structuresOf(w, v);
        double[][] times = new double[structures.length][];
        String[] timeNames = new String[structures.length];
        for (int i = 0; i < structures.length; i++) {
            times[i] = series(w, v, structures[i], 0);
            timeNames[i] = structures[i];
        }
        chart(dir.resolve(prefix + "_time.png"), title, "time, ms (log scale)", timeNames, times);

        double[][] ops = new double[structures.length * 3][];
        String[] opNames = new String[structures.length * 3];
        for (int i = 0; i < structures.length; i++) {
            ops[i * 3] = series(w, v, structures[i], 1);
            ops[i * 3 + 1] = series(w, v, structures[i], 2);
            ops[i * 3 + 2] = series(w, v, structures[i], 3);
            opNames[i * 3] = label(structures[i] + " steps", ops[i * 3]);
            opNames[i * 3 + 1] = label(structures[i] + " moves", ops[i * 3 + 1]);
            opNames[i * 3 + 2] = label(structures[i] + " comparisons", ops[i * 3 + 2]);
        }
        chart(dir.resolve(prefix + "_ops.png"), title, "operations count (log scale)", opNames, ops);
    }

    private static String label(String name, double[] values) {
        for (double value : values) {
            if (value > 0) {
                return name;
            }
        }
        return name + " = 0";
    }

    private static String[] structuresOf(String w, String v) {
        String[] found = new String[4];
        int count = 0;
        for (int i = 0; i < workload.length; i++) {
            if (!workload[i].equals(w) || !variant[i].equals(v)) {
                continue;
            }
            boolean known = false;
            for (int j = 0; j < count; j++) {
                if (found[j].equals(structure[i])) {
                    known = true;
                }
            }
            if (!known) {
                found[count++] = structure[i];
            }
        }
        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            result[i] = found[i];
        }
        return result;
    }

    private static double[] series(String w, String v, String s, int metric) {
        double[] values = new double[sizes.length];
        for (int k = 0; k < sizes.length; k++) {
            values[k] = -1;
            for (int i = 0; i < workload.length; i++) {
                if (workload[i].equals(w) && variant[i].equals(v) && structure[i].equals(s) && size[i] == sizes[k]) {
                    values[k] = switch (metric) {
                        case 0 -> timeMs[i];
                        case 1 -> steps[i];
                        case 2 -> moves[i];
                        default -> comparisons[i];
                    };
                }
            }
        }
        return values;
    }

    private static void chart(Path file, String title, String yLabel, String[] names, double[][] values)
            throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString(title, LEFT, 32);

        double min = Double.MAX_VALUE;
        double max = 0;
        for (double[] row : values) {
            for (double value : row) {
                if (value > 0) {
                    min = Math.min(min, value);
                    max = Math.max(max, value);
                }
            }
        }
        if (max <= 0) {
            max = 1;
        }
        if (min == Double.MAX_VALUE) {
            min = max;
        }
        double low = Math.floor(Math.log10(min));
        double high = Math.ceil(Math.log10(max));
        if (high - low < 1) {
            high = low + 1;
        }

        int plotWidth = WIDTH - LEFT - RIGHT;
        int plotHeight = HEIGHT - TOP - BOTTOM;

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (int tick = (int) low; tick <= (int) high; tick++) {
            int y = yPixel(tick, low, high, plotHeight);
            g.setColor(new Color(225, 225, 225));
            g.drawLine(LEFT, y, LEFT + plotWidth, y);
            g.setColor(Color.DARK_GRAY);
            g.drawString(tickLabel(tick), 20, y + 4);
        }
        for (int k = 0; k < sizes.length; k++) {
            int x = xPixel(k, plotWidth);
            g.setColor(new Color(235, 235, 235));
            g.drawLine(x, TOP, x, TOP + plotHeight);
            g.setColor(Color.DARK_GRAY);
            String label = String.valueOf(sizes[k]);
            g.drawString(label, x - label.length() * 3, TOP + plotHeight + 20);
        }

        g.setColor(Color.BLACK);
        g.drawLine(LEFT, TOP, LEFT, TOP + plotHeight);
        g.drawLine(LEFT, TOP + plotHeight, LEFT + plotWidth, TOP + plotHeight);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.drawString("n (elements, log spacing)", LEFT + plotWidth / 2 - 70, HEIGHT - 25);
        drawVertical(g, yLabel, 16, TOP + plotHeight / 2 + 60);

        for (int s = 0; s < values.length; s++) {
            g.setColor(COLORS[s % COLORS.length]);
            g.setStroke(stroke(s, values.length));
            int marker = Math.max(4, 11 - 2 * s);
            int previousX = -1;
            int previousY = -1;
            for (int k = 0; k < sizes.length; k++) {
                double value = values[s][k];
                if (value <= 0) {
                    previousX = -1;
                    continue;
                }
                int x = xPixel(k, plotWidth);
                int y = yPixel(Math.log10(value), low, high, plotHeight);
                if (previousX >= 0) {
                    g.drawLine(previousX, previousY, x, y);
                }
                g.fillOval(x - marker / 2, y - marker / 2, marker, marker);
                previousX = x;
                previousY = y;
            }
        }

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        int legendY = TOP + 10;
        for (int s = 0; s < names.length; s++) {
            g.setColor(COLORS[s % COLORS.length]);
            g.fillRect(LEFT + plotWidth + 20, legendY - 10, 14, 10);
            g.setColor(Color.BLACK);
            g.drawString(names[s], LEFT + plotWidth + 40, legendY);
            legendY += 24;
        }

        g.dispose();
        ImageIO.write(image, "png", file.toFile());
    }

    private static BasicStroke stroke(int index, int total) {
        if (total <= 2) {
            return index == 0
                    ? new BasicStroke(2.2f)
                    : new BasicStroke(2.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                            new float[]{9f, 6f}, 0f);
        }
        float period = 5f * total;
        return new BasicStroke(2.6f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                new float[]{5f, period - 5f}, 5f * index);
    }

    private static void drawVertical(Graphics2D g, String text, int x, int y) {
        Graphics2D rotated = (Graphics2D) g.create();
        rotated.rotate(-Math.PI / 2, x, y);
        rotated.drawString(text, x, y);
        rotated.dispose();
    }

    private static int xPixel(int index, int plotWidth) {
        if (sizes.length == 1) {
            return LEFT + plotWidth / 2;
        }
        return LEFT + index * plotWidth / (sizes.length - 1);
    }

    private static int yPixel(double logValue, double low, double high, int plotHeight) {
        double ratio = (logValue - low) / (high - low);
        return TOP + plotHeight - (int) (ratio * plotHeight);
    }

    private static String tickLabel(int power) {
        if (power < 0) {
            return String.format(java.util.Locale.ROOT, "%.3f", Math.pow(10, power));
        }
        if (power <= 6) {
            return String.valueOf((long) Math.pow(10, power));
        }
        return "1e" + power;
    }

    private static void load(Path csv) throws IOException {
        String[] lines = Files.readString(csv, StandardCharsets.UTF_8).split("\n");
        int count = 0;
        for (int i = 1; i < lines.length; i++) {
            if (!lines[i].isBlank()) {
                count++;
            }
        }
        workload = new String[count];
        variant = new String[count];
        structure = new String[count];
        size = new int[count];
        timeMs = new double[count];
        steps = new double[count];
        moves = new double[count];
        comparisons = new double[count];
        int row = 0;
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isBlank()) {
                continue;
            }
            String[] parts = lines[i].trim().split(",");
            workload[row] = parts[0];
            variant[row] = parts[1];
            structure[row] = parts[2];
            size[row] = Integer.parseInt(parts[3]);
            timeMs[row] = Double.parseDouble(parts[4]);
            steps[row] = Double.parseDouble(parts[5]);
            moves[row] = Double.parseDouble(parts[6]);
            comparisons[row] = Double.parseDouble(parts[7]);
            row++;
        }
        int[] unique = new int[count];
        int uniqueCount = 0;
        for (int i = 0; i < count; i++) {
            boolean known = false;
            for (int j = 0; j < uniqueCount; j++) {
                if (unique[j] == size[i]) {
                    known = true;
                }
            }
            if (!known) {
                unique[uniqueCount++] = size[i];
            }
        }
        sizes = new int[uniqueCount];
        for (int i = 0; i < uniqueCount; i++) {
            sizes[i] = unique[i];
        }
        for (int i = 1; i < sizes.length; i++) {
            int current = sizes[i];
            int j = i - 1;
            while (j >= 0 && sizes[j] > current) {
                sizes[j + 1] = sizes[j];
                j--;
            }
            sizes[j + 1] = current;
        }
    }
}
