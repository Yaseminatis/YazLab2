package edu.kou.yazLab2.model.weight;

import edu.kou.yazLab2.model.Node;

public class DynamicWeightCalculator {


    public double weight(Node a, Node b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}