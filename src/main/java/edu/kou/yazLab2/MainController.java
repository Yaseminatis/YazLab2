package edu.kou.yazLab2;

import edu.kou.yazLab2.algorithms.*;
import edu.kou.yazLab2.io.CsvGraphIO;
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
    @FXML private TableView<PerfRow> perfTable;
    @FXML private TableColumn<PerfRow, String> perfAlgoCol;
    @FXML private TableColumn<PerfRow, Number> perfMsCol;
    @FXML private Pane canvasHost;
    @FXML private TextField aktiflikField;
    @FXML private TextField etkilesimField;
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

    // import sırasında graph değişeceği için final OLMAMALI
    private Graph graph = new Graph();

    private Integer selectedNodeId = null;
    private int nextNodeId = 1;

    // --- CORE SERVICES ---
    private final BfsDfsService bfsDfsService = new BfsDfsService();
    private final ComponentsService componentsService = new ComponentsService();
    private final ShortestPathService shortestPathService = new ShortestPathService();
    private final CentralityService centralityService = new CentralityService();
    private final ColoringService coloringService = new ColoringService();
    // --- PERFORMANCE SERVICE ---
    private final edu.kou.yazLab2.perf.PerformanceService performanceService =
            new edu.kou.yazLab2.perf.PerformanceService();
    private final GraphIO jsonIO = new JsonGraphIO();
    private final GraphIO csvIO  = new CsvGraphIO();

    // nodeId -> renk (component / coloring için)
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
    private void onRunPerformance() {
        List<Integer> ids = graph.nodeIdsInOrder();
        if (ids.size() < 2) {
            log("Performans: en az 2 node gerekli.");
            return;
        }

        Integer s = (pathStartCombo != null) ? pathStartCombo.getValue() : ids.get(0);
        Integer t = (pathTargetCombo != null) ? pathTargetCombo.getValue() : ids.get(1);

        if (s == null || t == null || s.equals(t)) {
            // garanti fallback
            s = ids.get(0);
            t = ids.get(1);
        }

        var results = performanceService.runAll(graph, s, t);

        if (perfTable != null) {
            var rows = results.stream()
                    .map(r -> new PerfRow(r.algo(), r.ms()))
                    .toList();
            perfTable.setItems(FXCollections.observableArrayList(rows));
        }

        log("Performans ölçümü tamamlandı. (start=" + s + ", target=" + t + ")");
    }
    @FXML
    private void onUpdateSelectedNode() {
        if (selectedNodeId == null) {
            log("Güncelleme: seçili node yok.");
            return;
        }

        Node n = graph.getNode(selectedNodeId).orElse(null);
        if (n == null) return;

        try {
            double a = Double.parseDouble(aktiflikField.getText().trim());
            double e = Double.parseDouble(etkilesimField.getText().trim());

            if (a < 0 || a > 1) {
                log("Aktiflik 0-1 arası olmalı.");
                return;
            }

            n.setAktiflik(a);
            n.setEtkilesim(e);

            // bağlantı sayısı zaten edge’lerden geliyor (istersen n.setBaglantiSayisi(...))
            redraw();
            log("Node güncellendi: " + n.getId());
        } catch (Exception ex) {
            log("Güncelleme hatası: geçersiz değer.");
        }
    }
    @FXML
    private void initialize() {
        graphCanvas = new GraphCanvas(900, 600);
        canvasHost.getChildren().add(graphCanvas);

// ✅ Canvas boyutu her zaman orta panel kadar olsun
        graphCanvas.widthProperty().bind(canvasHost.widthProperty());
        graphCanvas.heightProperty().bind(canvasHost.heightProperty());

// Pencere/orta panel boyutu değişince yeniden çiz
        canvasHost.widthProperty().addListener((obs, o, n) -> redraw());
        canvasHost.heightProperty().addListener((obs, o, n) -> redraw());
//  Canvas boyutu her zaman orta panel (canvasHost) kadar olsun
        graphCanvas.widthProperty().bind(canvasHost.widthProperty());
        graphCanvas.heightProperty().bind(canvasHost.heightProperty());
        //  canvasHost dışına taşan çizimler görünmesin
        // ✅ Orta panel dışına taşan çizimler görünmesin
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(canvasHost.widthProperty());
        clip.heightProperty().bind(canvasHost.heightProperty());
        canvasHost.setClip(clip);
        if (perfAlgoCol != null) perfAlgoCol.setCellValueFactory(c -> c.getValue().algoProperty());
        if (perfMsCol != null) perfMsCol.setCellValueFactory(c -> c.getValue().msProperty());

        // TableView column bindings
        if (centralityNodeCol != null) centralityNodeCol.setCellValueFactory(c -> c.getValue().nodeIdProperty());
        if (centralityDegreeCol != null) centralityDegreeCol.setCellValueFactory(c -> c.getValue().degreeProperty());

        if (colorNodeCol != null) colorNodeCol.setCellValueFactory(c -> c.getValue().nodeIdProperty());
        if (colorCol != null) colorCol.setCellValueFactory(c -> c.getValue().colorProperty());

        graphCanvas.setOnMouseClicked(e -> {
            Integer clickedId = findNodeIdAt(e.getX(), e.getY());

            if (clickedId != null) {
                // SHIFT + tık → EDGE EKLE
                // CTRL + SHIFT → EDGE SİL
                if (e.isControlDown() && e.isShiftDown()
                        && selectedNodeId != null
                        && !selectedNodeId.equals(clickedId)) {

                    boolean removed = graph.removeEdge(selectedNodeId, clickedId);

                    if (removed){
// bağlantı sayısı güncelle
                        graph.getNode(selectedNodeId).ifPresent(nn -> nn.setBaglantiSayisi(graph.getNeighbors(selectedNodeId).size()));
                        graph.getNode(clickedId).ifPresent(nn -> nn.setBaglantiSayisi(graph.getNeighbors(clickedId).size()));
                        log("Edge silindi: " + selectedNodeId + " - " + clickedId);

                }  else log("Edge yoktu: " + selectedNodeId + " - " + clickedId);
// SHIFT → EDGE EKLE
                } else if (e.isShiftDown() && selectedNodeId != null && !selectedNodeId.equals(clickedId)) {
                    try {
                        graph.addEdge(selectedNodeId, clickedId);
                        log("Edge eklendi: " + selectedNodeId + " - " + clickedId);
                        // bağlantı sayısı güncelle
                        graph.getNode(selectedNodeId).ifPresent(nn -> nn.setBaglantiSayisi(graph.getNeighbors(selectedNodeId).size()));
                        graph.getNode(clickedId).ifPresent(nn -> nn.setBaglantiSayisi(graph.getNeighbors(clickedId).size()));
                    } catch (Exception ex) {
                        log("Edge eklenemedi: " + ex.getMessage());

                    }

// NORMAL TIK → NODE SEÇ
                } else {
                    selectedNodeId = clickedId;

                    Node n = graph.getNode(clickedId).orElse(null);
                    if (n != null) {
                        n.secildi();           // seçilme sayısı +1
                        n.aktiflikGuncelle();  // aktiflik hesapla
                    }

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

        // mevcut seçimleri koru
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


        updateSelectedNodeInfo();

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
            warn("Seçim Yok", "Silmek için önce bir düğüm seçin.");
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
    private void onImportCsv() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("CSV Graf İçe Aktar");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            File file = fc.showOpenDialog(canvasHost.getScene().getWindow());
            if (file == null) {
                log("CSV içe aktar iptal edildi.");
                return;
            }

            graph = csvIO.load(file.toPath());
            selectedNodeId = null;

            nodeColors.clear();
            for (Node n : graph.getNodes()) nodeColors.put(n.getId(), Color.DODGERBLUE);

            recalcNextNodeId();
            refreshCombos();
            clearResultPanels();
            redraw();

            log("CSV içe aktarıldı: " + file.getName());
        } catch (Exception ex) {
            log("CSV içe aktarma hatası: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    @FXML
    private void onExportCsv() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("CSV Graf Dışa Aktar");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fc.setInitialFileName("graph.csv");

            File file = fc.showSaveDialog(canvasHost.getScene().getWindow());
            if (file == null) {
                log("CSV dışa aktar iptal edildi.");
                return;
            }

            csvIO.save(graph, file.toPath());
            log("CSV dışa aktarıldı: " + file.getName());
        } catch (Exception ex) {
            log("CSV dışa aktarma hatası: " + ex.getMessage());
            ex.printStackTrace();
        }
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
                graph.refreshDerivedFields();
                return;
            }

            graph = jsonIO.load(file.toPath());
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
            jsonIO.save(graph, path);

            log("Dışa aktarıldı: " + file.getName());
        } catch (Exception ex) {
            log("Dışa aktarma hatası: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // -------- BFS / DFS --------

    @FXML
    private void onRunBfs() {
        if (isGraphEmpty()) {
            warn("Graf Boş", "BFS çalıştırmak için graf üzerinde düğüm olmalı.");
            return;
        }
        Integer startId = (startNodeCombo != null) ? startNodeCombo.getValue() : null;
        if (startId == null) {
            warn("Eksik Seçim", "Lütfen BFS için başlangıç düğümü seçin.");
            if (traversalArea != null) traversalArea.setText("BFS: Başlangıç node seçilmedi!");
            return;
        }

        List<Integer> order = bfsDfsService.bfs(graph, startId);
        etkilesimArtir(order);

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
        updateSelectedNodeInfo();
    }

    @FXML
    private void onRunDfs() {
        Integer startId = (startNodeCombo != null) ? startNodeCombo.getValue() : null;
        if (startId == null) {
            warn("Eksik Seçim", "DFS için başlangıç node seçilmelidir.");
            return;
        }

        List<Integer> order = bfsDfsService.dfs(graph, startId);
        etkilesimArtir(order);

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
        updateSelectedNodeInfo();
    }

    // -------- Components --------

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
    private void etkilesimArtir(List<Integer> ids) {
        if (ids == null) return;

        for (Integer id : ids) {
            Node n = graph.getNode(id).orElse(null);
            if (n != null) {
                n.ziyaretEdildi();      // ziyaret sayısı +1
                n.etkilesimGuncelle();  // etkilesim hesapla
            }
        }
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

        PathResult r = shortestPathService.dijkstra(graph, s, t);
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

        PathResult r = shortestPathService.aStar(graph, s, t);
        showPathResult("A*", r);
    }

    // ✅ PathResult RECORD uyumlu (path(), totalCost())
    private void showPathResult(String name, PathResult r) {
        highlightedEdges.clear();

        if (r == null || r.path() == null || r.path().isEmpty()) {
            if (pathOutputArea != null) pathOutputArea.setText(name + ": Yol bulunamadı.");
            redraw();
            log(name + ": yol bulunamadı.");
            return;
        }

        List<Integer> p = r.path();
        for (int i = 0; i < p.size() - 1; i++) {
            highlightedEdges.add(edgeKey(p.get(i), p.get(i + 1)));
        }

        if (pathOutputArea != null) {
            pathOutputArea.setText(
                    name + " Path:\n" + p + "\nToplam maliyet: " + String.format("%.2f", r.totalCost())
            );
        }

        redraw();
        log(name + " bulundu. Path=" + p + " cost=" + r.totalCost());
    }

    // -------- Degree Centrality Top5 --------
    @FXML
    private void onShowTop5Centrality() {
        // ✅ senin CentralityService metoduna uyumlu:
        // public List<CentralityResult> top5DegreeCentrality(Graph graph)
        List<CentralityResult> results = centralityService.top5DegreeCentrality(graph);

        List<CentralityRow> rows = results.stream()
                .map(r -> new CentralityRow(r.getNodeId(), r.getDegree()))
                .toList();

        if (centralityTable != null) {
            centralityTable.setItems(FXCollections.observableArrayList(rows));
        }

        log("Top5 degree centrality hesaplandı.");
    }
    public static class PerfRow {
        private final javafx.beans.property.SimpleStringProperty algo =
                new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.SimpleDoubleProperty ms =
                new javafx.beans.property.SimpleDoubleProperty();

        public PerfRow(String algo, double ms) {
            this.algo.set(algo);
            this.ms.set(ms);
        }
        public javafx.beans.property.StringProperty algoProperty() { return algo; }
        public javafx.beans.property.DoubleProperty msProperty() { return ms; }
    }
    // -------- Welsh–Powell Coloring --------
    @FXML
    private void onRunWelshPowell() {
        // ✅ senin ColoringService metoduna uyumlu:
        // public List<ColoringResult> welshPowell(Graph graph)
        List<ColoringResult> results = coloringService.welshPowell(graph);

        // canvas renklendirme
        nodeColors.clear();
        for (ColoringResult r : results) {
            int cIndex = r.getColor(); // sende ColoringResult getter adı böyle olmalı
            nodeColors.put(r.getNodeId(), palette[Math.floorMod(cIndex, palette.length)]);
        }

        // tablo doldur
        List<ColoringRow> rows = results.stream()
                .sorted(Comparator.comparingInt(ColoringResult::getNodeId))
                .map(r -> new ColoringRow(r.getNodeId(), r.getColor()))
                .toList();

        if (coloringTable != null) {
            coloringTable.setItems(FXCollections.observableArrayList(rows));
        }

        highlightedEdges.clear();
        redraw();

        int usedColors = results.stream().map(ColoringResult::getColor).max(Integer::compareTo).orElse(0);
        log("Welsh–Powell çalıştı. Kullanılan renk sayısı: ~" + (usedColors + 1));
    }

    // ---------------- Row DTOs (UI) ----------------

    public static class CentralityRow {
        private final IntegerProperty nodeId = new SimpleIntegerProperty();
        private final IntegerProperty degree = new SimpleIntegerProperty();

        public CentralityRow(int nodeId, int degree) {
            this.nodeId.set(nodeId);
            this.degree.set(degree);
        }

        public IntegerProperty nodeIdProperty() { return nodeId; }
        public IntegerProperty degreeProperty() { return degree; }
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
    private void updateSelectedNodeInfo() {
        if (nodeInfoLabel == null) return;

        if (selectedNodeId == null) {
            nodeInfoLabel.setText("Yok");
            return;
        }

        Node n = graph.getNode(selectedNodeId).orElse(null);
        if (n == null) {
            nodeInfoLabel.setText("Yok");
            return;
        }

        nodeInfoLabel.setText(
                "ID: " + n.getId() +
                        "\nX: " + (int) n.getX() +
                        "\nY: " + (int) n.getY() +
                        "\nAktiflik: " + String.format("%.2f", n.getAktiflik()) +
                        "\nEtkileşim: " + String.format("%.2f", n.getEtkilesim()) +
                        "\nBağlantı: " + n.getBaglantiSayisi()
        );
    }
    private void log(String msg) {
        if (logArea != null) logArea.appendText(msg + "\n");
    }
    private void warn(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
        log("UYARI: " + title + " - " + msg);
    }

    private void error(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
        log("HATA: " + title + " - " + msg);
    }

    private boolean isGraphEmpty() {
        return graph == null || graph.getNodes() == null || graph.getNodes().isEmpty();
    }
}