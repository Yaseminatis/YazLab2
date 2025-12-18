package edu.kou.yazLab2.model;

import java.util.Objects;

public class Edge {
    private final int fromId;
    private final int toId;

    public Edge(int fromId, int toId) {
        this.fromId = fromId;
        this.toId = toId;
    }

    public int getFromId() { return fromId; }
    public int getToId() { return toId; }

    // Undirected duplicate kontrolü için normalize ediyoruz
    private int a() { return Math.min(fromId, toId); }
    private int b() { return Math.max(fromId, toId); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge edge)) return false;
        return a() == edge.a() && b() == edge.b();
    }

    @Override
    public int hashCode() {
        return Objects.hash(a(), b());
    }

    @Override
    public String toString() {
        return "Edge{" + fromId + " <-> " + toId + "}";
    }
}