package edu.kou.yazLab2.ui.canvas;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GraphCanvas extends Canvas {

    private static final double NODE_RADIUS = 12;

    public GraphCanvas(double width, double height) {
        super(width, height);
        clear();
    }

    /** Canvas'ı temizler */
    public void clear() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Node çizer
     * @param x node x
     * @param y node y
     * @param selected seçili mi
     */
    public void drawNode(double x, double y, boolean selected) {
        GraphicsContext gc = getGraphicsContext2D();

        // Seçiliyse farklı renkte çiz
        gc.setFill(selected ? Color.ORANGE : Color.DODGERBLUE);
        gc.fillOval(
                x - NODE_RADIUS,
                y - NODE_RADIUS,
                NODE_RADIUS * 2,
                NODE_RADIUS * 2
        );

        // Kenarlık
        gc.setStroke(Color.BLACK);
        gc.strokeOval(
                x - NODE_RADIUS,
                y - NODE_RADIUS,
                NODE_RADIUS * 2,
                NODE_RADIUS * 2
        );
    }
}