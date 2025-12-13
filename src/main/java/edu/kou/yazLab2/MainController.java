package edu.kou.yazLab2;

import edu.kou.yazLab2.model.Node;
import edu.kou.yazLab2.ui.canvas.GraphCanvas;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML private Pane canvasHost;
    @FXML private Label nodeInfoLabel;

    private GraphCanvas graphCanvas;

    private final List<Node> nodes = new ArrayList<>();
    private Node selectedNode = null;
    private int nextNodeId = 1;

    @FXML
    private void initialize() {
        graphCanvas = new GraphCanvas(900, 600);
        canvasHost.getChildren().add(graphCanvas);

        graphCanvas.setOnMouseClicked(e -> {
            Node clicked = findNodeAt(e.getX(), e.getY());

            if (clicked != null) {
                selectedNode = clicked;
            } else {
                Node newNode = new Node(nextNodeId++, e.getX(), e.getY());
                nodes.add(newNode);
                selectedNode = newNode;
            }

            redraw();
        });

        redraw();
    }

    private void redraw() {
        graphCanvas.clear();

        for (Node n : nodes) {
            graphCanvas.drawNode(n.getX(), n.getY(), n == selectedNode);
        }

        if (nodeInfoLabel != null) {
            if (selectedNode != null) {
                nodeInfoLabel.setText(
                        "ID: " + selectedNode.getId() +
                                "\nX: " + (int) selectedNode.getX() +
                                "\nY: " + (int) selectedNode.getY()
                );
            } else {
                nodeInfoLabel.setText("Yok");
            }
        }
    }

    private Node findNodeAt(double x, double y) {

        double radius = 12;

        for (Node n : nodes) {
            double dx = n.getX() - x;
            double dy = n.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) <= radius) {
                return n;
            }
        }
        return null;
    }
}