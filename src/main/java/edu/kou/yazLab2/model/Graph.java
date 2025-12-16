package edu.kou.yazLab2.model;

import edu.kou.yazLab2.model.validation.GraphValidationException;
import edu.kou.yazLab2.model.weight.DynamicWeightCalculator;

import java.util.*;

public class Graph {

    private final Map<Integer, Node> nodes = new LinkedHashMap<>();
    private final Set<Edge> edges = new LinkedHashSet<>();
    private final DynamicWeightCalculator weightCalculator = new DynamicWeightCalculator();

    // ---- NODE ----
    public void addNode(Node node) {
        if (nodes.containsKey(node.getId())) {
            throw new GraphValidationException("Aynı node id olamaz: " + node.getId());
        }
        nodes.put(node.getId(), node);
    }

    public void removeNode(int nodeId) {
        if (!nodes.containsKey(nodeId)) return;
        nodes.remove(nodeId);
        edges.removeIf(e -> e.getFromId() == nodeId || e.getToId() == nodeId);
    }

    public Optional<Node> getNode(int id) {
        return Optional.ofNullable(nodes.get(id));
    }

    // İSTEĞE BAĞLI: Optional ile uğraşmamak için
    public Node requireNode(int id) {
        return getNode(id).orElseThrow(() ->
                new GraphValidationException("Node bulunamadı: " + id)
        );
    }

    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    // ---- EDGE ----
    public void addEdge(int fromId, int toId) {
        if (fromId == toId) {
            throw new GraphValidationException("Self-loop yasak: " + fromId);
        }
        if (!nodes.containsKey(fromId) || !nodes.containsKey(toId)) {
            throw new GraphValidationException("Edge için node bulunamadı: " + fromId + " - " + toId);
        }

        Edge edge = new Edge(fromId, toId);
        if (edges.contains(edge)) {
            throw new GraphValidationException("Duplicate edge yasak: " + fromId + " - " + toId);
        }

        edges.add(edge);
    }

    public void removeEdge(int fromId, int toId) {
        edges.remove(new Edge(fromId, toId));
    }

    public Set<Edge> getEdges() {
        return Collections.unmodifiableSet(edges);
    }

    // ---- ADJACENCY LIST ----
    public Map<Integer, List<Integer>> adjacencyList() {
        Map<Integer, List<Integer>> adj = new LinkedHashMap<>();
        for (int id : nodes.keySet()) adj.put(id, new ArrayList<>());

        for (Edge e : edges) {
            adj.get(e.getFromId()).add(e.getToId());
            adj.get(e.getToId()).add(e.getFromId());
        }

        for (List<Integer> list : adj.values()) Collections.sort(list);
        return adj;
    }

    // ---- ADJACENCY MATRIX (WEIGHTED) ----
    public double[][] adjacencyMatrix() {
        List<Integer> ids = new ArrayList<>(nodes.keySet());
        int n = ids.size();

        Map<Integer, Integer> index = new HashMap<>();
        for (int i = 0; i < n; i++) index.put(ids.get(i), i);

        double[][] m = new double[n][n];
        for (int i = 0; i < n; i++) Arrays.fill(m[i], Double.POSITIVE_INFINITY);
        for (int i = 0; i < n; i++) m[i][i] = 0.0;

        for (Edge e : edges) {
            int i = index.get(e.getFromId());
            int j = index.get(e.getToId());

            Node a = nodes.get(e.getFromId());
            Node b = nodes.get(e.getToId());
            double w = weightCalculator.weight(a, b);

            m[i][j] = w;
            m[j][i] = w;
        }

        return m;
    }

    // Var olan: eklenme sırası
    public List<Integer> nodeIdsInOrder() {
        return new ArrayList<>(nodes.keySet());
    }

    // EKLENDİ: sıralı id listesi (MainController hatasını çözer)
    public List<Integer> sortedNodeIds() {
        List<Integer> ids = new ArrayList<>(nodes.keySet());
        Collections.sort(ids);
        return ids;
    }
}