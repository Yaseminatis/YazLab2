package edu.kou.yazLab2.perf;

import edu.kou.yazLab2.algorithms.*;
import edu.kou.yazLab2.model.Graph;

import java.util.ArrayList;
import java.util.List;

public class PerformanceService {

    private final BfsDfsService bfsDfs = new BfsDfsService();
    private final ComponentsService comps = new ComponentsService();
    private final ShortestPathService sp = new ShortestPathService();
    private final ColoringService coloring = new ColoringService();

    public List<PerformanceResult> runAll(Graph g, int start, int target) {
        List<PerformanceResult> out = new ArrayList<>();

        int n = g.getNodes().size();
        int e = g.getEdges().size();

        out.add(measure("BFS", n, e, () -> bfsDfs.bfs(g, start)));
        out.add(measure("DFS", n, e, () -> bfsDfs.dfs(g, start)));
        out.add(measure("Components", n, e, () -> comps.findComponents(g)));
        out.add(measure("Dijkstra", n, e, () -> sp.dijkstra(g, start, target)));
        out.add(measure("A*", n, e, () -> sp.aStar(g, start, target)));
        out.add(measure("Welsh-Powell", n, e, () -> coloring.welshPowell(g)));

        return out;
    }

    private PerformanceResult measure(String name, int n, int e, RunnableWork work) {
        // 1 kez ısınma (JIT etkisini azaltsın)
        work.run();

        long t0 = System.nanoTime();
        work.run();
        long t1 = System.nanoTime();

        double ms = (t1 - t0) / 1_000_000.0;
        return new PerformanceResult(name, n, e, ms);
    }

    @FunctionalInterface
    private interface RunnableWork { void run(); }
}