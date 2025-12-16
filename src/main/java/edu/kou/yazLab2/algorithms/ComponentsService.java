package edu.kou.yazLab2.algorithms;

import edu.kou.yazLab2.model.Graph;

import java.util.*;

public class ComponentsService {

    public List<List<Integer>> findComponents(Graph graph) {
        Map<Integer, List<Integer>> adj = graph.adjacencyList();

        List<List<Integer>> components = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        for (Integer start : adj.keySet()) {
            if (visited.contains(start)) continue;

            List<Integer> comp = new ArrayList<>();
            Deque<Integer> stack = new ArrayDeque<>();
            stack.push(start);
            visited.add(start);

            while (!stack.isEmpty()) {
                int u = stack.pop();
                comp.add(u);

                for (int v : adj.getOrDefault(u, List.of())) {
                    if (!visited.contains(v)) {
                        visited.add(v);
                        stack.push(v);
                    }
                }
            }

            Collections.sort(comp);
            components.add(comp);
        }

        // büyük component önce gelsin
        components.sort((a, b) -> Integer.compare(b.size(), a.size()));

        return components;
    }
}