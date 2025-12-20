package edu.kou.yazLab2.algorithms;

import edu.kou.yazLab2.model.Graph;

import java.util.*;

public class DijkstraService {

    private static class State {
        final int nodeId;
        final double dist;

        State(int nodeId, double dist) {
            this.nodeId = nodeId;
            this.dist = dist;
        }
    }

    public PathResult shortestPath(Graph graph, int startId, int targetId) {
        Map<Integer, List<Integer>> adj = graph.adjacencyList();

        // start/target yoksa
        if (!adj.containsKey(startId) || !adj.containsKey(targetId)) {
            return new PathResult(List.of(), Double.POSITIVE_INFINITY);
        }

        // Dijkstra: dist + prev
        Map<Integer, Double> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();

        for (Integer id : adj.keySet()) {
            dist.put(id, Double.POSITIVE_INFINITY);
        }
        dist.put(startId, 0.0);

        PriorityQueue<State> pq = new PriorityQueue<>(Comparator.comparingDouble(s -> s.dist));
        pq.add(new State(startId, 0.0));

        while (!pq.isEmpty()) {
            State cur = pq.poll();
            int u = cur.nodeId;

            // PQ içinde eski kayıtlar kalabilir
            if (cur.dist > dist.get(u)) continue;

            // hedefe ulaştıysak bitir
            if (u == targetId) break;

            for (int v : adj.getOrDefault(u, List.of())) {
                double w = graph.edgeCost(u, v);   // ✅ tek noktadan maliyet
                double alt = dist.get(u) + w;

                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.add(new State(v, alt));
                }
            }
        }

        // yol yoksa
        if (dist.get(targetId) == Double.POSITIVE_INFINITY) {
            return new PathResult(List.of(), Double.POSITIVE_INFINITY);
        }

        // path'i geri kur
        List<Integer> path = new ArrayList<>();
        Integer cur = targetId;
        while (cur != null) {
            path.add(cur);
            if (cur == startId) break;
            cur = prev.get(cur);
        }
        Collections.reverse(path);

        // güvenlik: start'a ulaşamadıysa
        if (path.isEmpty() || path.get(0) != startId) {
            return new PathResult(List.of(), Double.POSITIVE_INFINITY);
        }

        return new PathResult(path, dist.get(targetId));
    }
}