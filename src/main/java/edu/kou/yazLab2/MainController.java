package edu.kou.yazLab2;

import edu.kou.yazLab2.algorithms.BfsDfsService;
import edu.kou.yazLab2.algorithms.ComponentsService;
import edu.kou.yazLab2.io.GraphIO;
import edu.kou.yazLab2.io.JsonGraphIO;
import edu.kou.yazLab2.model.Edge;
import edu.kou.yazLab2.model.Graph;
import edu.kou.yazLab2.model.Node;
import edu.kou.yazLab2.ui.canvas.GraphCanvas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    @FXML private Pane canvasHost;

    @FXML private Label nodeInfoLabel;
    @FXML private ComboBox<Integer> startNodeCombo;
    @FXML private ListView<String> resultListView;
    @FXML private TextArea logArea;

    private GraphCanvas graphCanvas;

    // import sırasında graph değişeceği için final OLMAMALI
    private Graph graph = new Graph();

    private Integer selectedNodeId = null;
    private int nextNodeId = 1;

    private final BfsDfsService bfsDfsService = new BfsDfsService();
    private final ComponentsService componentsService = new ComponentsService();

    private final GraphIO graphIO = new JsonGraphIO();

    // nodeId -> renk (component renklendirme için)
    private final Map<Integer, Color> nodeColors = new HashMap<>();

    private final Color[] palette = new Color[] {
            Color.DODGERBLUE, Color.MEDIUMSEAGREEN, Color.DARKORCHID, Color.CORAL,
            Color.GOLDENROD, Color.DEEPSKYBLUE, Color.TOMATO, Color.SLATEBLUE
    };

    // ---------------- INIT ----------------
    @FXML
    private void initialize() {
        graphCanvas = new GraphCanvas(900, 600);
        canvasHost.getChildren().add(graphCanvas);

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

        if (startNodeCombo != null) {
            startNodeCombo.setItems(FXCollections.observableArrayList(ids));

            if (selectedNodeId != null && ids.contains(selectedNodeId)) {
                startNodeCombo.getSelectionModel().select(selectedNodeId);
            } else if (!ids.isEmpty()) {
                startNodeCombo.getSelectionModel().selectFirst();
            }
        }
    }

    private void redraw() {
        graphCanvas.clear();

        // EDGE’LER
        for (Edge e : graph.getEdges()) {
            Node a = graph.getNode(e.getFromId()).orElse(null);
            Node b = graph.getNode(e.getToId()).orElse(null);
            if (a != null && b != null) {
                graphCanvas.drawEdge(a.getX(), a.getY(), b.getX(), b.getY(), false);
            }
        }

        // NODE’LAR (renk destekli)  ✅ imza: drawNode(x,y, selected, fillColor)
        for (Node n : graph.getNodes()) {
            boolean selected = selectedNodeId != null && n.getId() == selectedNodeId;
            Color fill = nodeColors.getOrDefault(n.getId(), Color.DODGERBLUE);
            graphCanvas.drawNode(n.getX(), n.getY(), fill, selected);
        }

        // SAĞ PANEL
        if (nodeInfoLabel != null) {
            if (selectedNodeId != null) {
                Node n = graph.getNode(selectedNodeId).orElse(null);
                if (n != null) {
                    nodeInfoLabel.setText(
                            "ID: " + n.getId() +
                                    "\nX: " + (int) n.getX() +
                                    "\nY: " + (int) n.getY()
                    );
                } else {
                    nodeInfoLabel.setText("Yok");
                }
            } else {
                nodeInfoLabel.setText("Yok");
            }
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
        for (Node n : graph.getNodes()) {
            if (n.getId() > maxId) maxId = n.getId();
        }
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
            for (Node n : graph.getNodes()) {
                nodeColors.put(n.getId(), Color.DODGERBLUE);
            }

            recalcNextNodeId();
            refreshCombos();
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
            return;
        }

        List<Integer> order = bfsDfsService.bfs(graph, startId);

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
            return;
        }

        List<Integer> order = bfsDfsService.dfs(graph, startId);

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

        if (resultListView != null) {
            resultListView.getItems().clear();
            resultListView.getItems().add("Connected Components: " + comps.size());
        }

        nodeColors.clear();
        for (Node n : graph.getNodes()) {
            nodeColors.put(n.getId(), Color.DODGERBLUE);
        }

        int idx = 0;
        for (List<Integer> comp : comps) {
            Color c = palette[idx % palette.length];

            if (resultListView != null) {
                resultListView.getItems().add("C" + (idx + 1) + " -> " + comp);
            }

            for (Integer nodeId : comp) {
                nodeColors.put(nodeId, c);
            }
            idx++;
        }

        redraw();
        log("Components bulundu: " + comps.size() + " (renklendirildi)");
    }

    private void log(String msg) {
        if (logArea != null) {
            logArea.appendText(msg + "\n");
        }
    }
}