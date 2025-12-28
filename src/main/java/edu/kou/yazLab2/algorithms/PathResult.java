package edu.kou.yazLab2.algorithms;

import java.util.List;

public record PathResult(List<Integer> path, double totalCost) {

    public boolean found() {
        return path != null && !path.isEmpty() && Double.isFinite(totalCost);
    }
}