package edu.kou.yazLab2.ui.canvas;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

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
        drawNode(x, y, null, selected, null);
    }

    /** (Mevcut imza ile uyumlu) components / coloring için fillColor destekli */
    public void drawNode(double x, double y, Color fillColor, boolean selected) {
        drawNode(x, y, fillColor, selected, null);
    }

    /**
     * Node çizer:
     * - fillColor ile renklendirme (components / welsh-powell)
     * - selected kenarlık
     * - opsiyonel nodeId yazdırma (demo/test için çok faydalı)
     */
    public void drawNode(double x, double y, Color fillColor, boolean selected, Integer nodeId) {
        GraphicsContext gc = getGraphicsContext2D();

        // fill
        Color baseFill = (fillColor != null) ? fillColor : Color.DODGERBLUE;
        gc.setFill(baseFill);
        gc.fillOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

        // border
        gc.setStroke(selected ? Color.ORANGE : Color.BLACK);
        gc.setLineWidth(selected ? 3 : 1.5);
        gc.strokeOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

        // node id text (opsiyonel)
        if (nodeId != null) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(String.valueOf(nodeId), x, y);
        }

        gc.setLineWidth(1);
    }

    /**
     * Edge çizer:
     * - highlighted=true ise (shortest path) daha kalın ve turuncu
     * - normalde gri
     */
    public void drawEdge(double x1, double y1, double x2, double y2, boolean highlighted) {
        GraphicsContext gc = getGraphicsContext2D();

        if (highlighted) {
            // önce “glow” gibi bir alt katman (daha görünür path)
            gc.setStroke(Color.web("#FFD08A")); // açık turuncu
            gc.setLineWidth(6);
            gc.strokeLine(x1, y1, x2, y2);

            // üstüne asıl path çizgisi
            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(3.5);
            gc.strokeLine(x1, y1, x2, y2);
        } else {
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(2);
            gc.strokeLine(x1, y1, x2, y2);
        }

        gc.setLineWidth(1);
    }
}