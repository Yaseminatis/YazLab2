package edu.kou.yazLab2.ui.canvas;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GraphCanvas extends Canvas {

    public static final double NODE_RADIUS = 12;

    public GraphCanvas(double width, double height) {
        super(width, height);
        clear();
    }

    public void clear() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, getWidth(), getHeight());
    }

    /** Default node çizimi (renk default, selected destekli) */
    public void drawNode(double x, double y, boolean selected) {
        drawNode(x, y, null, selected);
    }

    /**
     * Node çizer (components renklendirme için fillColor destekli)
     * @param x node x
     * @param y node y
     * @param fillColor node iç rengi (null ise default mavi)
     * @param selected seçili mi (kenarlığı turuncu yapar)
     */
    public void drawNode(double x, double y, Color fillColor, boolean selected) {
        GraphicsContext gc = getGraphicsContext2D();

        Color baseFill = (fillColor != null) ? fillColor : Color.DODGERBLUE;
        gc.setFill(baseFill);
        gc.fillOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

        gc.setStroke(selected ? Color.ORANGE : Color.BLACK);
        gc.setLineWidth(selected ? 3 : 1);
        gc.strokeOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

        gc.setLineWidth(1);
    }

    public void drawEdge(double x1, double y1, double x2, double y2, boolean highlighted) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setStroke(highlighted ? Color.ORANGE : Color.GRAY);
        gc.setLineWidth(highlighted ? 3 : 2);
        gc.strokeLine(x1, y1, x2, y2);
        gc.setLineWidth(1);
    }
}