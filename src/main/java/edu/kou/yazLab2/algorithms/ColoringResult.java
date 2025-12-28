package edu.kou.yazLab2.algorithms;

public class ColoringResult {
    private final int nodeId;
    private final int color; // 1,2,3...

    public ColoringResult(int nodeId, int color) {
        this.nodeId = nodeId;
        this.color = color;
    }

    public int getNodeId() { return nodeId; }
    public int getColor() { return color; }
}