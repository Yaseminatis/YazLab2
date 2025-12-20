package edu.kou.yazLab2.algorithms;

import edu.kou.yazLab2.model.Graph;

import java.util.*;

public class BfsDfsService {

    public List<Integer> bfs(Graph graph, int startId) {
        // start node yoksa boş döndür
        if (graph.getNode(startId).isEmpty()) {
            return List.of();
        }

        List<Integer> order = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> q = new ArrayDeque<>();

        visited.add(startId);
        q.add(startId);

        while (!q.isEmpty()) {
            int u = q.poll();
            order.add(u);

            // Komşular Graph üzerinden (sorted)
            for (int v : graph.getNeighbors(u)) {
                if (visited.add(v)) {
                    q.add(v);
                }
            }
        }

        return order;
    }

    public List<Integer> dfs(Graph graph, int startId) {
        if (graph.getNode(startId).isEmpty()) {
            return List.of();
        }

        List<Integer> order = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        dfsRec(graph, startId, visited, order);

        return order;
    }

    private void dfsRec(Graph graph,
                        int u,
                        Set<Integer> visited,
                        List<Integer> order) {

        visited.add(u);
        order.add(u);

        for (int v : graph.getNeighbors(u)) {
            if (!visited.contains(v)) {
                dfsRec(graph, v, visited, order);
            }
        }
    }
}