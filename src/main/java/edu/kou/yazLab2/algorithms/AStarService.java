package edu.kou.yazLab2.algorithms;

import edu.kou.yazLab2.model.Graph;

import java.util.*;

public class AStarService {

    private static class State {
        final int nodeId;
        final double gScore;   // start -> node gerçek maliyet
        final double fScore;   // g + h

        State(int nodeId, double gScore, double fScore) {
            this.nodeId = nodeId;
            this.gScore = gScore;
            this.fScore = fScore;
        }
    }

    public PathResult shortestPath(Graph graph, int startId, int targetId) {
        Map<Integer, List<Integer>> adj = graph.adjacencyList();

        if (!adj.containsKey(startId) || !adj.containsKey(targetId)) {
            return new PathResult(List.of(), Double.POSITIVE_INFINITY);
        }

        Map<Integer, Double> gScore = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();

        for (Integer id : adj.keySet()) {
            gScore.put(id, Double.POSITIVE_INFINITY);
        }
        gScore.put(startId, 0.0);

        PriorityQueue<State> open = new PriorityQueue<>(Comparator.comparingDouble(s -> s.fScore));
        open.add(new State(startId, 0.0, heuristic(graph, startId, targetId)));

        while (!open.isEmpty()) {
            State cur = open.poll();
            int u = cur.nodeId;

            // PQ içinde eski kayıtlar kalabilir: stale entry kontrolü
            if (cur.gScore > gScore.get(u)) continue;

            if (u == targetId) break;

            for (int v : adj.getOrDefault(u, List.of())) {
                double tentativeG = gScore.get(u) + graph.edgeCost(u, v);

                if (tentativeG < gScore.get(v)) {
                    gScore.put(v, tentativeG);
                    prev.put(v, u);

                    double f = tentativeG + heuristic(graph, v, targetId);
                    open.add(new State(v, tentativeG, f));
                }
            }
        }

        if (gScore.get(targetId) == Double.POSITIVE_INFINITY) {
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

        if (path.isEmpty() || path.get(0) != startId) {
            return new PathResult(List.of(), Double.POSITIVE_INFINITY);
        }

        return new PathResult(path, gScore.get(targetId));
    }

    private double heuristic(Graph graph, int fromId, int targetId) {
        // (Öklid (dinamik ağırlıkla uyumlu)
        var a = graph.requireNode(fromId);
        var b = graph.requireNode(targetId);

        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}