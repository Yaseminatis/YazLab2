package edu.kou.yazLab2.model;

import java.util.Objects;

/**
 * Graph düğümü.
 * - id: benzersiz kimlik
 * - x,y: konum (dinamik ağırlık hesaplarında kullanılır)
 */
public class Node {

    private final int id;
    private final double x;
    private final double y;

    public Node(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // Node eşitliği yalnızca id üzerinden
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node node)) return false;
        return id == node.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node{id=" + id + ", x=" + x + ", y=" + y + "}";
    }
}