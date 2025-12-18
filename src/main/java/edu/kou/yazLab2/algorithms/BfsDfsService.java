package edu.kou.yazLab2.algorithms;

import edu.kou.yazLab2.model.Graph;

import java.util.*;

public class BfsDfsService {

    public List<Integer> bfs(Graph graph, int startId) {
        Map<Integer, List<Integer>> adj = graph.adjacencyList();

        if (!adj.containsKey(startId)) {
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

            for (int v : adj.getOrDefault(u, List.of())) {
                if (!visited.contains(v)) {
                    visited.add(v);
                    q.add(v);
                }
            }
        }

        return order;
    }

    public List<Integer> dfs(Graph graph, int startId) {
        Map<Integer, List<Integer>> adj = graph.adjacencyList();

        if (!adj.containsKey(startId)) {
            return List.of();
        }

        List<Integer> order = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        dfsRec(startId, adj, visited, order);

        return order;
    }

    private void dfsRec(int u,
                        Map<Integer, List<Integer>> adj,
                        Set<Integer> visited,
                        List<Integer> order) {

        visited.add(u);
        order.add(u);

        for (int v : adj.getOrDefault(u, List.of())) {
            if (!visited.contains(v)) {
                dfsRec(v, adj, visited, order);
            }
        }
    }
}