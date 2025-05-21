import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Turtle ermöglicht das Erstellen einfacher Turtle-Grafiken als SVG-Datei.
 * Das Koordinatensystem ist kartesisch (0°=rechts, Winkel gegen den Uhrzeigersinn,
 * Y-Achse positiv nach oben). SVG verwendet hingegen eine Y-Achse, die nach unten zeigt.
 * Daher werden die Y-Koordinaten beim Export invertiert.
 * Die einzelnen graphischen Elemente werden durchnummeriert in der Reihenfolge ihrer Erzeugung.
 */
public class Turtle {
    private final double xFrom, yFrom, viewWidth, viewHeight;
    private final List<Line> lines = new ArrayList<>();
    private int elementCounter = 0;
    private State state;
    private final Deque<State> stack = new ArrayDeque<>();

    /**
     * @param xFrom      linke Begrenzung des Sichtbereichs
     * @param xTo        rechte Begrenzung des Sichtbereichs
     * @param yFrom      untere Begrenzung des Sichtbereichs
     * @param yTo        obere Begrenzung des Sichtbereichs
     * @param startX     Start-X-Koordinate der Turtle
     * @param startY     Start-Y-Koordinate der Turtle
     * @param startAngle Blickrichtung in Grad (0°=rechts, 90°=oben, gegen den Uhrzeigersinn)
     */
    public Turtle(double xFrom, double xTo, double yFrom, double yTo,
                     double startX, double startY, double startAngle) {
        this.xFrom = xFrom;
        this.yFrom = yFrom;
        this.viewWidth = xTo - xFrom;
        this.viewHeight = yTo - yFrom;
        this.state = new State(
            startX, startY, startAngle,
            new Color(0, 0, 0, 1.0), 1.0, true);
    }

    public Turtle penUp() {
        state = state.withPenDown(false);
        return this;
    }

    public Turtle penDown() {
        state = state.withPenDown(true);
        return this;
    }

    public Turtle forward(double distance) {
        double rad = Math.toRadians(state.angle());
        double dx = Math.cos(rad) * distance;
        double dy = Math.sin(rad) * distance;
        double newX = state.x() + dx;
        double newY = state.y() + dy;
        if (state.penDown()) {
            lines.add(new Line(++elementCounter,
                    state.x(), state.y(), newX, newY,
                    state.color(), state.width()));
        }
        state = state.withPosition(newX, newY);
        return this;
    }

    public Turtle backward(double distance) {
        forward(-distance);
        return this;
    }

    public Turtle right(double angle) {
        // Normalize angle to be in [0, 360)
        double newAngle = state.angle() - angle;
        state = state.withAngle((newAngle % 360 + 360) % 360);
        return this;
    }

    public Turtle left(double angle) {
        // Normalize angle to be in [0, 360)
        double newAngle = state.angle() + angle;
        state = state.withAngle((newAngle % 360 + 360) % 360);
        return this;
    }

    public Turtle color(int r, int g, int b, double a) {
        if (!(0 <= r && r <= 255 && 0 <= g && g <= 255 && 0 <= b && b <= 255 && 0 <= a && a <= 1))
            throw new IllegalArgumentException(
                String.format(Locale.US, "Invalid color values: r=%d, g=%d, b=%d, a=%.2f. " +
                                         "RGB must be [0,255], alpha must be [0.0,1.0].", r, g, b, a)
            );
        state = state.withColor(new Color(r, g, b, a));
        return this;
    }

    public Turtle color(int r, int g, int b) {
        return color(r, g, b, state.color().a());
    }

    public Turtle width(double w) {
        state = state.withWidth(w);
        return this;
    }

    public Turtle push() {
        stack.push(state);
        return this;
    }

    public Turtle pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Cannot pop from an empty turtle state stack.");
        }
        state = stack.pop();
        return this;
    }

    public void save(String filename) throws IOException {
        Path path = Path.of(filename);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(
                String.format(Locale.US,
                    """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="%.2f %.2f %.2f %.2f">
                    """,
                    xFrom, yFrom, viewWidth, viewHeight)
            );
            for (Line e : lines) {
                // Adapt coordinates for SVG
                double y1Svg = (viewHeight - (e.y1() - yFrom)) + yFrom;
                double y2Svg = (viewHeight - (e.y2() - yFrom)) + yFrom;
                writer.write(
                    String.format(Locale.US,
                        """
                          <line id="%d" x1="%.2f" y1="%.2f" x2="%.2f" y2="%.2f"
                                stroke="rgba(%d,%d,%d,%.2f)" stroke-width="%.2f" />
                        """,
                        e.id(), e.x1(), y1Svg, e.x2(), y2Svg,
                        e.color().r(), e.color().g(), e.color().b(), e.color().a(),
                        e.width()));
            }
            writer.write("</svg>\n");
        }
    }

    public void save() throws IOException { save("output.svg"); }

    private static record State(double x, double y, double angle, Color color, double width, boolean penDown) {
        public State withPosition(double newX, double newY) {
            return new State(newX, newY, angle, color, width, penDown);
        }
        public State withAngle(double newAngle) {
            return new State(x, y, newAngle, color, width, penDown);
        }
        public State withColor(Color newColor) {
            return new State(x, y, angle, newColor, width, penDown);
        }
        public State withWidth(double newWidth) {
            return new State(x, y, angle, color, newWidth, penDown);
        }
        public State withPenDown(boolean isDown) {
            return new State(x, y, angle, color, width, isDown);
        }
    }

    private static record Line(int id, double x1, double y1, double x2, double y2, Color color, double width) {}

    private static record Color(int r, int g, int b, double a) {}
}