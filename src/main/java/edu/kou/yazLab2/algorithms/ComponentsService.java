package edu.kou.yazLab2.algorithms;

import edu.kou.yazLab2.model.Graph;

import java.util.*;

public class ComponentsService {

    public List<List<Integer>> findComponents(Graph graph) {

        List<List<Integer>> components = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        // Başlangıç düğümlerini stabil sırada gez (UI/test için deterministik)
        for (Integer start : graph.sortedNodeIds()) {
            if (visited.contains(start)) continue;

            List<Integer> comp = new ArrayList<>();
            Deque<Integer> stack = new ArrayDeque<>();
            stack.push(start);
            visited.add(start);

            while (!stack.isEmpty()) {
                int u = stack.pop();
                comp.add(u);

                // Komşuları Graph üzerinden al (sorted)
                for (int v : graph.getNeighbors(u)) {
                    if (visited.add(v)) {
                        stack.push(v);
                    }
                }
            }

            Collections.sort(comp);
            components.add(comp);
        }

        // büyük component önce gelsin (mevcut davranışı koruyor)
        components.sort((a, b) -> Integer.compare(b.size(), a.size()));

        return components;
    }
}