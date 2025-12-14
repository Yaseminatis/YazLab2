package edu.kou.yazLab2.model;

public class Node {
    private final int id;
    private final double x;
    private final double y;

    public Node(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
}