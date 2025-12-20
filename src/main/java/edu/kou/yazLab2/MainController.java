package edu.kou.yazLab2;

import edu.kou.yazLab2.algorithms.*;
import edu.kou.yazLab2.io.GraphIO;
import edu.kou.yazLab2.io.JsonGraphIO;
import edu.kou.yazLab2.model.Edge;
import edu.kou.yazLab2.model.Graph;
import edu.kou.yazLab2.model.Node;
import edu.kou.yazLab2.ui.canvas.GraphCanvas;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {

    @FXML private Pane canvasHost;

    // SOL PANEL
    @FXML private ComboBox<Integer> startNodeCombo;
    @FXML private TextArea traversalArea;
    @FXML private TextArea componentsArea;

    // Shortest path (SOL PANEL)
    @FXML private ComboBox<Integer> pathStartCombo;
    @FXML private ComboBox<Integer> pathTargetCombo;
    @FXML private TextArea pathOutputArea;

    // SAĞ PANEL
    @FXML private Label nodeInfoLabel;
    @FXML private ListView<String> resultListView;
    @FXML private TextArea logArea;

    // SAĞ PANEL - Top5 Centrality
    @FXML private TableView<CentralityRow> centralityTable;
    @FXML private TableColumn<CentralityRow, Number> centralityNodeCol;
    @FXML private TableColumn<CentralityRow, Number> centralityDegreeCol;

    // SAĞ PANEL - Welsh–Powell
    @FXML private TableView<ColoringRow> coloringTable;
    @FXML private TableColumn<ColoringRow, Number> colorNodeCol;
    @FXML private TableColumn<ColoringRow, Number> colorCol;

    private GraphCanvas graphCanvas;

    private Graph graph = new Graph();

    private Integer selectedNodeId = null;
    private int nextNodeId = 1;

    private final BfsDfsService bfsDfsService = new BfsDfsService();
    private final ComponentsService componentsService = new ComponentsService();

    private final DijkstraService dijkstraService = new DijkstraService();
    private final AStarService aStarService = new AStarService();

    private final GraphIO graphIO = new JsonGraphIO();

    private final Map<Integer, Color> nodeColors = new HashMap<>();

    // Path highlight için: "minId-maxId"
    private final Set<String> highlightedEdges = new HashSet<>();

    private final Color[] palette = new Color[] {
            Color.DODGERBLUE, Color.MEDIUMSEAGREEN, Color.DARKORCHID, Color.CORAL,
            Color.GOLDENROD, Color.DEEPSKYBLUE, Color.TOMATO, Color.SLATEBLUE,
            Color.DARKCYAN, Color.DARKSALMON, Color.OLIVEDRAB
    };

    // ---------------- INIT ----------------
    @FXML
    private void initialize() {
        graphCanvas = new GraphCanvas(900, 600);
        canvasHost.getChildren().add(graphCanvas);

        // TableView column bindings
        if (centralityNodeCol != null) centralityNodeCol.setCellValueFactory(c -> c.getValue().nodeIdProperty());
        if (centralityDegreeCol != null) centralityDegreeCol.setCellValueFactory(c -> c.getValue().degreeProperty());

        if (colorNodeCol != null) colorNodeCol.setCellValueFactory(c -> c.getValue().nodeIdProperty());
        if (colorCol != null) colorCol.setCellValueFactory(c -> c.getValue().colorProperty());

        graphCanvas.setOnMouseClicked(e -> {
            Integer clickedId = findNodeIdAt(e.getX(), e.getY());

            if (clickedId != null) {
                // SHIFT + tık → EDGE EKLE
                if (e.isShiftDown() && selectedNodeId != null && !selectedNodeId.equals(clickedId)) {
                    try {
                        graph.addEdge(selectedNodeId, clickedId);
                        log("Edge eklendi: " + selectedNodeId + " - " + clickedId);
                    } catch (Exception ex) {
                        log("Edge eklenemedi: " + ex.getMessage());
                    }
                } else {
                    selectedNodeId = clickedId;
                    log("Node seçildi: " + clickedId);
                }
            } else {
                int id = nextNodeId++;
                graph.addNode(new Node(id, e.getX(), e.getY()));
                selectedNodeId = id;

                nodeColors.putIfAbsent(id, Color.DODGERBLUE);
                log("Node eklendi: " + id);
            }

            refreshCombos();
            redraw();
        });

        refreshCombos();
        redraw();
    }

    // ---------------- UI HELPERS ----------------
    private void refreshCombos() {
        List<Integer> ids = graph.nodeIdsInOrder();

        // Mevcut seçimleri koru
        Integer currentStart = (startNodeCombo != null) ? startNodeCombo.getValue() : null;
        Integer currentPathS = (pathStartCombo != null) ? pathStartCombo.getValue() : null;
        Integer currentPathT = (pathTargetCombo != null) ? pathTargetCombo.getValue() : null;

        if (startNodeCombo != null) {
            startNodeCombo.setItems(FXCollections.observableArrayList(ids));
            if (currentStart != null && ids.contains(currentStart)) startNodeCombo.setValue(currentStart);
            else if (selectedNodeId != null && ids.contains(selectedNodeId)) startNodeCombo.setValue(selectedNodeId);
            else if (!ids.isEmpty()) startNodeCombo.getSelectionModel().selectFirst();
        }

        if (pathStartCombo != null) {
            pathStartCombo.setItems(FXCollections.observableArrayList(ids));
            if (currentPathS != null && ids.contains(currentPathS)) pathStartCombo.setValue(currentPathS);
            else if (!ids.isEmpty()) pathStartCombo.getSelectionModel().selectFirst();
        }

        if (pathTargetCombo != null) {
            pathTargetCombo.setItems(FXCollections.observableArrayList(ids));
            if (currentPathT != null && ids.contains(currentPathT)) pathTargetCombo.setValue(currentPathT);
            else if (ids.size() > 1) pathTargetCombo.getSelectionModel().select(1);
            else if (!ids.isEmpty()) pathTargetCombo.getSelectionModel().selectFirst();
        }
    }

    private void clearResultPanels() {
        if (traversalArea != null) traversalArea.clear();
        if (componentsArea != null) componentsArea.clear();
        if (pathOutputArea != null) pathOutputArea.clear();

        if (resultListView != null) resultListView.getItems().clear();
        if (centralityTable != null) centralityTable.getItems().clear();
        if (coloringTable != null) coloringTable.getItems().clear();

        highlightedEdges.clear();
    }

    private String edgeKey(int a, int b) {
        int x = Math.min(a, b);
        int y = Math.max(a, b);
        return x + "-" + y;
    }

    private void redraw() {
        graphCanvas.clear();

        // EDGE’LER
        for (Edge e : graph.getEdges()) {
            Node a = graph.getNode(e.getFromId()).orElse(null);
            Node b = graph.getNode(e.getToId()).orElse(null);
            if (a != null && b != null) {
                boolean hl = highlightedEdges.contains(edgeKey(e.getFromId(), e.getToId()));
                graphCanvas.drawEdge(a.getX(), a.getY(), b.getX(), b.getY(), hl);
            }
        }

        // NODE’LAR
        for (Node n : graph.getNodes()) {
            boolean selected = selectedNodeId != null && n.getId() == selectedNodeId;
            Color fill = nodeColors.getOrDefault(n.getId(), Color.DODGERBLUE);
            graphCanvas.drawNode(n.getX(), n.getY(), fill, selected);
        }

        // SAĞ PANEL - seçili node bilgisi
        if (nodeInfoLabel != null) {
            if (selectedNodeId != null) {
                Node n = graph.getNode(selectedNodeId).orElse(null);
                if (n != null) {
                    nodeInfoLabel.setText(
                            "ID: " + n.getId() +
                                    "\nX: " + (int) n.getX() +
                                    "\nY: " + (int) n.getY()
                    );
                } else nodeInfoLabel.setText("Yok");
            } else nodeInfoLabel.setText("Yok");
        }
    }

    private Integer findNodeIdAt(double x, double y) {
        double r = GraphCanvas.NODE_RADIUS;
        for (Node n : graph.getNodes()) {
            double dx = n.getX() - x;
            double dy = n.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) <= r) return n.getId();
        }
        return null;
    }

    private void recalcNextNodeId() {
        int maxId = 0;
        for (Node n : graph.getNodes()) maxId = Math.max(maxId, n.getId());
        nextNodeId = maxId + 1;
    }

    // ---------------- BUTTON ACTIONS ----------------

    @FXML
    private void onDeleteSelected() {
        if (selectedNodeId == null) {
            log("Silinecek node yok.");
            return;
        }
        graph.removeNode(selectedNodeId);
        nodeColors.remove(selectedNodeId);

        log("Node silindi: " + selectedNodeId);
        selectedNodeId = null;

        refreshCombos();
        redraw();
    }

    @FXML
    private void onImportJson() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("JSON Graf İçe Aktar");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

            File file = fc.showOpenDialog(canvasHost.getScene().getWindow());
            if (file == null) {
                log("İçe aktar iptal edildi.");
                return;
            }

            graph = graphIO.load(file.toPath());
            selectedNodeId = null;

            nodeColors.clear();
            for (Node n : graph.getNodes()) nodeColors.put(n.getId(), Color.DODGERBLUE);

            recalcNextNodeId();
            refreshCombos();
            clearResultPanels();
            redraw();

            log("İçe aktarıldı: " + file.getName());
        } catch (Exception ex) {
            log("İçe aktarma hatası: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void onExportJson() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("JSON Graf Dışa Aktar");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            fc.setInitialFileName("graph.json");

            File file = fc.showSaveDialog(canvasHost.getScene().getWindow());
            if (file == null) {
                log("Dışa aktar iptal edildi.");
                return;
            }

            Path path = file.toPath();
            graphIO.save(graph, path);

            log("Dışa aktarıldı: " + file.getName());
        } catch (Exception ex) {
            log("Dışa aktarma hatası: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void onRunBfs() {
        Integer startId = (startNodeCombo != null) ? startNodeCombo.getValue() : null;
        if (startId == null) {
            log("BFS: Başlangıç node seçilmedi!");
            if (traversalArea != null) traversalArea.setText("BFS: Başlangıç node seçilmedi!");
            return;
        }

        List<Integer> order = bfsDfsService.bfs(graph, startId);

        if (traversalArea != null) {
            String path = order.stream().map(String::valueOf).collect(Collectors.joining(" -> "));
            traversalArea.setText("BFS from " + startId + ":\n" + path + "\n\nVisited: " + order.size());
        }

        if (resultListView != null) {
            resultListView.setItems(FXCollections.observableArrayList(
                    order.stream().map(id -> "Node: " + id).toList()
            ));
        }

        log("BFS sırası: " + order);
    }

    @FXML
    private void onRunDfs() {
        Integer startId = (startNodeCombo != null) ? startNodeCombo.getValue() : null;
        if (startId == null) {
            log("DFS: Başlangıç node seçilmedi!");
            if (traversalArea != null) traversalArea.setText("DFS: Başlangıç node seçilmedi!");
            return;
        }

        List<Integer> order = bfsDfsService.dfs(graph, startId);

        if (traversalArea != null) {
            String path = order.stream().map(String::valueOf).collect(Collectors.joining(" -> "));
            traversalArea.setText("DFS from " + startId + ":\n" + path + "\n\nVisited: " + order.size());
        }

        if (resultListView != null) {
            resultListView.setItems(FXCollections.observableArrayList(
                    order.stream().map(id -> "Node: " + id).toList()
            ));
        }

        log("DFS sırası: " + order);
    }

    @FXML
    private void onFindComponents() {
        List<List<Integer>> comps = componentsService.findComponents(graph);

        if (componentsArea != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Connected Components (").append(comps.size()).append(")\n\n");
            int k = 1;
            for (List<Integer> comp : comps) {
                sb.append(k).append(") ").append(comp).append("\n");
                k++;
            }
            componentsArea.setText(sb.toString());
        }

        if (resultListView != null) {
            resultListView.getItems().clear();
            resultListView.getItems().add("Connected Components: " + comps.size());
        }

        // components renklendirme
        nodeColors.clear();
        for (Node n : graph.getNodes()) nodeColors.put(n.getId(), Color.DODGERBLUE);

        int idx = 0;
        for (List<Integer> comp : comps) {
            Color c = palette[idx % palette.length];

            if (resultListView != null) {
                resultListView.getItems().add("C" + (idx + 1) + " -> " + comp);
            }

            for (Integer nodeId : comp) nodeColors.put(nodeId, c);
            idx++;
        }

        highlightedEdges.clear();
        redraw();
        log("Components bulundu: " + comps.size() + " (renklendirildi)");
    }

    // -------- Shortest Path --------

    @FXML
    private void onRunDijkstra() {
        Integer s = (pathStartCombo != null) ? pathStartCombo.getValue() : null;
        Integer t = (pathTargetCombo != null) ? pathTargetCombo.getValue() : null;

        if (s == null || t == null) {
            log("Dijkstra: start/target seçilmedi.");
            if (pathOutputArea != null) pathOutputArea.setText("Dijkstra: start/target seçilmedi.");
            return;
        }
        if (s.equals(t)) {
            log("Dijkstra: start ve hedef aynı olamaz.");
            if (pathOutputArea != null) pathOutputArea.setText("Dijkstra: start ve hedef aynı olamaz.");
            return;
        }

        PathResult r = dijkstraService.shortestPath(graph, s, t);
        showPathResult("Dijkstra", r);
    }

    @FXML
    private void onRunAStar() {
        Integer s = (pathStartCombo != null) ? pathStartCombo.getValue() : null;
        Integer t = (pathTargetCombo != null) ? pathTargetCombo.getValue() : null;

        if (s == null || t == null) {
            log("A*: start/target seçilmedi.");
            if (pathOutputArea != null) pathOutputArea.setText("A*: start/target seçilmedi.");
            return;
        }
        if (s.equals(t)) {
            log("A*: start ve hedef aynı olamaz.");
            if (pathOutputArea != null) pathOutputArea.setText("A*: start ve hedef aynı olamaz.");
            return;
        }

        PathResult r = aStarService.shortestPath(graph, s, t);
        showPathResult("A*", r);
    }

    private void showPathResult(String name, PathResult r) {
        highlightedEdges.clear();

        if (r == null || !r.found()) {
            if (pathOutputArea != null) pathOutputArea.setText(name + ": Yol bulunamadı.");
            redraw();
            log(name + ": yol bulunamadı.");
            return;
        }

        List<Integer> p = r.getPath();
        for (int i = 0; i < p.size() - 1; i++) {
            highlightedEdges.add(edgeKey(p.get(i), p.get(i + 1)));
        }

        if (pathOutputArea != null) {
            pathOutputArea.setText(
                    name + " Path:\n" + p + "\nToplam maliyet: " + String.format("%.2f", r.getTotalCost())
            );
        }

        redraw();
        log(name + " bulundu. Path=" + p + " cost=" + r.getTotalCost());
    }

    // -------- Degree Centrality Top5 --------

    @FXML
    private void onShowTop5Centrality() {
        Map<Integer, List<Integer>> adj = graph.adjacencyList();

        List<CentralityRow> rows = adj.entrySet().stream()
                .map(e -> new CentralityRow(e.getKey(), e.getValue().size()))
                .sorted((a, b) -> Integer.compare(b.getDegree(), a.getDegree()))
                .limit(5)
                .toList();

        if (centralityTable != null) {
            centralityTable.setItems(FXCollections.observableArrayList(rows));
        }

        log("Top5 degree centrality hesaplandı.");
    }

    // -------- Welsh–Powell Coloring --------

    @FXML
    private void onRunWelshPowell() {
        Map<Integer, List<Integer>> adj = graph.adjacencyList();

        List<Integer> nodes = new ArrayList<>(adj.keySet());
        nodes.sort((a, b) -> Integer.compare(adj.getOrDefault(b, List.of()).size(),
                adj.getOrDefault(a, List.of()).size()));

        Map<Integer, Integer> colorMap = new HashMap<>(); // nodeId -> colorIndex

        int currentColor = 0;
        for (Integer u : nodes) {
            if (colorMap.containsKey(u)) continue;

            colorMap.put(u, currentColor);

            for (Integer v : nodes) {
                if (colorMap.containsKey(v)) continue;

                boolean ok = true;
                for (Integer neigh : adj.getOrDefault(v, List.of())) {
                    Integer c = colorMap.get(neigh);
                    if (c != null && c == currentColor) {
                        ok = false;
                        break;
                    }
                }
                if (ok) colorMap.put(v, currentColor);
            }

            currentColor++;
        }

        // canvas renklendirme
        nodeColors.clear();
        for (Node n : graph.getNodes()) {
            int cIndex = colorMap.getOrDefault(n.getId(), 0);
            nodeColors.put(n.getId(), palette[cIndex % palette.length]);
        }

        // tablo doldur
        List<ColoringRow> rows = nodes.stream()
                .map(id -> new ColoringRow(id, colorMap.getOrDefault(id, 0)))
                .toList();

        if (coloringTable != null) {
            coloringTable.setItems(FXCollections.observableArrayList(rows));
        }

        highlightedEdges.clear();

        redraw();
        log("Welsh–Powell çalıştı. Kullanılan renk sayısı: " + currentColor);
    }

    // ---------------- Row DTOs ----------------

    public static class CentralityRow {
        private final IntegerProperty nodeId = new SimpleIntegerProperty();
        private final IntegerProperty degree = new SimpleIntegerProperty();

        public CentralityRow(int nodeId, int degree) {
            this.nodeId.set(nodeId);
            this.degree.set(degree);
        }

        public IntegerProperty nodeIdProperty() { return nodeId; }
        public IntegerProperty degreeProperty() { return degree; }
        public int getDegree() { return degree.get(); }
    }

    public static class ColoringRow {
        private final IntegerProperty nodeId = new SimpleIntegerProperty();
        private final IntegerProperty color = new SimpleIntegerProperty();

        public ColoringRow(int nodeId, int color) {
            this.nodeId.set(nodeId);
            this.color.set(color);
        }

        public IntegerProperty nodeIdProperty() { return nodeId; }
        public IntegerProperty colorProperty() { return color; }
    }

    private void log(String msg) {
        if (logArea != null) logArea.appendText(msg + "\n");
    }
}