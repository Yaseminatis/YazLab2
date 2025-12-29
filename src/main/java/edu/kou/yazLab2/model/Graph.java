package edu.kou.yazLab2.model;

import edu.kou.yazLab2.model.validation.GraphValidationException;
import edu.kou.yazLab2.model.weight.DynamicWeightCalculator;

import java.util.*;

public class Graph {

    private final Map<Integer, Node> nodes = new LinkedHashMap<>();
    private final Set<Edge> edges = new LinkedHashSet<>();

    // Dinamik ağırlık HESAPLAMA – tek merkez
    private final DynamicWeightCalculator weightCalculator = new DynamicWeightCalculator();

    // ---------------- NODE ----------------
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
        recalcConnectionCounts();

    }

    public Optional<Node> getNode(int id) {
        return Optional.ofNullable(nodes.get(id));
    }

    public Node requireNode(int id) {
        return getNode(id).orElseThrow(() ->
                new GraphValidationException("Node bulunamadı: " + id)
        );
    }

    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    // ---------------- EDGE ----------------
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
        recalcConnectionCounts();
    }

    public boolean removeEdge(int fromId, int toId) {
        boolean removed = edges.remove(new Edge(fromId, toId));
        if (removed) recalcConnectionCounts();
        return removed;


    }

    public Set<Edge> getEdges() {
        return Collections.unmodifiableSet(edges);
    }

    // ---------------- ADJACENCY ----------------

    /** Komşu listesi (undirected) */
    public List<Integer> getNeighbors(int nodeId) {
        List<Integer> neighbors = new ArrayList<>();
        for (Edge e : edges) {
            if (e.getFromId() == nodeId) neighbors.add(e.getToId());
            else if (e.getToId() == nodeId) neighbors.add(e.getFromId());
        }
        Collections.sort(neighbors);
        return neighbors;
    }

    /** BFS / DFS / Components için adjacency list */
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

    // ---------------- WEIGHT ----------------

    /**
     * Dinamik edge maliyeti / ağırlığı (TEK NOKTA)
     * Dijkstra ve A* burayı kullanacak.
     */
    public double edgeCost(int fromId, int toId) {
        Node a = requireNode(fromId);
        Node b = requireNode(toId);
        return weightCalculator.weight(a, b);
    }

    /**
     * Geriye dönük uyumluluk: eski isim kullanıldıysa kırılmasın diye.
     * (İstersen tamamen kaldırabilirsin ama şimdilik güvenli.)
     */
    public double getEdgeWeight(int fromId, int toId) {
        return edgeCost(fromId, toId);
    }

    /** Weighted adjacency matrix (Dijkstra / A*) */
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

            double w = edgeCost(e.getFromId(), e.getToId());
            m[i][j] = w;
            m[j][i] = w;
        }

        return m;
    }

    // ---------------- HELPERS ----------------

    /** Eklenme sırasına göre */
    public List<Integer> nodeIdsInOrder() {
        return new ArrayList<>(nodes.keySet());
    }

    /** Sıralı id listesi */
    public List<Integer> sortedNodeIds() {
        List<Integer> ids = new ArrayList<>(nodes.keySet());
        Collections.sort(ids);
        return ids;
    }
    private void recalcConnectionCounts() {
        // önce sıfırla
        for (Node n : nodes.values()) {
            n.setBaglantiSayisi(0);
        }
        // her edge iki node'un derecesini artırır (undirected)
        for (Edge e : edges) {
            Node a = nodes.get(e.getFromId());
            Node b = nodes.get(e.getToId());
            if (a != null) a.setBaglantiSayisi(a.getBaglantiSayisi() + 1);
            if (b != null) b.setBaglantiSayisi(b.getBaglantiSayisi() + 1);
        }
    }
    public void refreshDerivedFields() {
        recalcConnectionCounts();
    }
}