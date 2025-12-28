package edu.kou.yazLab2.algorithms;

public class CentralityResult {
    private final int nodeId;
    private final int degree;

    public CentralityResult(int nodeId, int degree) {
        this.nodeId = nodeId;
        this.degree = degree;
    }

    public int getNodeId() { return nodeId; }
    public int getDegree() { return degree; }
}