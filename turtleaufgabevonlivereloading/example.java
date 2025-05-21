import java.io.IOException;

void drawPolygon(Turtle t, int sides, double length) {
    double angle = 360.0 / sides;
    for (int i = 0; i < sides; i++) {
        t.forward(length);
        t.right(angle);
    }
}

void recursivePattern(Turtle t, int depth, double length, int sides) {
    if (depth == 0) return;

    for (int i = 0; i < sides; i++) {
        t.push();
        t.color((depth * 40 + i * 15) % 255, (i * 20) % 255, (255 - depth * 30) % 255, 0.9);
        t.width(1.0 + depth * 0.2);
        drawPolygon(t, sides, length);
        t.forward(length / 2);
        t.right(360.0 / sides);
        recursivePattern(t, depth - 1, length * 0.6, sides);
        t.pop();
        t.right(360.0 / sides);
    }
}

void main() throws IOException {
    Turtle t = new Turtle(-400, 400, -400, 400, 0, 0, 0);
    recursivePattern(t, 4, 120, 6); // depth, size, sides
    t.save();
}
