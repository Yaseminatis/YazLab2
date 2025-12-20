package edu.kou.yazLab2.algorithms;

import java.util.List;

public class PathResult {

    private final List<Integer> path;
    private final double totalCost;

    public PathResult(List<Integer> path, double totalCost) {
        this.path = path;
        this.totalCost = totalCost;
    }

    public List<Integer> getPath() {
        return path;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public boolean found() {
        return path != null && !path.isEmpty();
    }
}