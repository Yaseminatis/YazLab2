package edu.kou.yazLab2.algorithms;

import edu.kou.yazLab2.model.Graph;

import java.util.*;

public class ColoringService {

    
    public List<ColoringResult> welshPowell(Graph graph) {
        Map<Integer, List<Integer>> adj = graph.adjacencyList();

        // node'ları degree'a göre büyükten küçüğe sırala
        List<Integer> nodes = new ArrayList<>(adj.keySet());
        nodes.sort((a, b) -> {
            int da = adj.getOrDefault(a, List.of()).size();
            int db = adj.getOrDefault(b, List.of()).size();
            if (db != da) return Integer.compare(db, da);
            return Integer.compare(a, b);
        });

        Map<Integer, Integer> colorOf = new HashMap<>();
        int color = 0;

        for (int node : nodes) {
            if (colorOf.containsKey(node)) continue;

            color++;
            colorOf.put(node, color);

            for (int other : nodes) {
                if (colorOf.containsKey(other)) continue;

                boolean conflict = false;
                for (int neigh : adj.getOrDefault(other, List.of())) {
                    if (colorOf.getOrDefault(neigh, -1) == color) {
                        conflict = true;
                        break;
                    }
                }
                if (!conflict) {
                    colorOf.put(other, color);
                }
            }
        }

        List<ColoringResult> results = new ArrayList<>();
        for (var e : colorOf.entrySet()) {
            results.add(new ColoringResult(e.getKey(), e.getValue()));
        }
        results.sort(Comparator.comparingInt(ColoringResult::getNodeId));
        return results;
    }


    public List<ColoringResult> color(Graph graph) {
        return welshPowell(graph);
    }
}