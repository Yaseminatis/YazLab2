package edu.kou.yazLab2.algorithms;

import edu.kou.yazLab2.model.Graph;

public class ShortestPathService {

    private final DijkstraService dijkstra = new DijkstraService();
    private final AStarService aStar = new AStarService();

    public PathResult dijkstra(Graph graph, int startId, int targetId) {
        return dijkstra.shortestPath(graph, startId, targetId);
    }

    public PathResult aStar(Graph graph, int startId, int targetId) {
        return aStar.shortestPath(graph, startId, targetId);
    }
}