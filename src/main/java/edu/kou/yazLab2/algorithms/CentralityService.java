package edu.kou.yazLab2.algorithms;

import edu.kou.yazLab2.model.Graph;

import java.util.*;

public class CentralityService {


    public List<CentralityResult> top5DegreeCentrality(Graph graph) {
        return topDegree(graph, 5);
    }


    public List<CentralityResult> topDegree(Graph graph, int topN) {
        Map<Integer, List<Integer>> adj = graph.adjacencyList();

        List<CentralityResult> all = new ArrayList<>();
        for (var entry : adj.entrySet()) {
            int nodeId = entry.getKey();
            int degree = entry.getValue().size();
            all.add(new CentralityResult(nodeId, degree));
        }

        all.sort(
                Comparator.comparingInt(CentralityResult::getDegree).reversed()
                        .thenComparingInt(CentralityResult::getNodeId)
        );

        return all.subList(0, Math.min(topN, all.size()));
    }


    public List<CentralityResult> calculate(Graph graph) {
        return topDegree(graph, 5);
    }
}