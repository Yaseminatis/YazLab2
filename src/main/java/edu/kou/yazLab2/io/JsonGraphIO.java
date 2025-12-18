package edu.kou.yazLab2.io;

import edu.kou.yazLab2.model.Edge;
import edu.kou.yazLab2.model.Graph;
import edu.kou.yazLab2.model.Node;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonGraphIO implements GraphIO {

    // ---------------- LOAD ----------------
    @Override
    public Graph load(Path path) throws Exception {

        String json = Files.readString(path);
        Graph graph = new Graph();

        /* -------- NODES --------
           {"id":1,"x":123.0,"y":45.0}
        */
        Pattern nodePattern = Pattern.compile(
                "\\{\\s*\"id\"\\s*:\\s*(\\d+)\\s*,\\s*\"x\"\\s*:\\s*([0-9.]+)\\s*,\\s*\"y\"\\s*:\\s*([0-9.]+)\\s*}"
        );

        Matcher nodeMatcher = nodePattern.matcher(json);
        while (nodeMatcher.find()) {
            int id = Integer.parseInt(nodeMatcher.group(1));
            double x = Double.parseDouble(nodeMatcher.group(2));
            double y = Double.parseDouble(nodeMatcher.group(3));
            graph.addNode(new Node(id, x, y));
        }

        /* -------- EDGES --------
           {"from":1,"to":2}
        */
        Pattern edgePattern = Pattern.compile(
                "\\{\\s*\"from\"\\s*:\\s*(\\d+)\\s*,\\s*\"to\"\\s*:\\s*(\\d+)\\s*}"
        );

        Matcher edgeMatcher = edgePattern.matcher(json);
        while (edgeMatcher.find()) {
            int from = Integer.parseInt(edgeMatcher.group(1));
            int to = Integer.parseInt(edgeMatcher.group(2));
            graph.addEdge(from, to);
        }

        return graph;
    }

    // ---------------- SAVE ----------------
    @Override
    public void save(Graph graph, Path path) throws Exception {

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        // ---- NODES ----
        sb.append("  \"nodes\": [\n");
        boolean first = true;
        for (Node n : graph.getNodes()) {
            if (!first) sb.append(",\n");
            first = false;

            sb.append("    {")
                    .append("\"id\":").append(n.getId()).append(",")
                    .append("\"x\":").append(n.getX()).append(",")
                    .append("\"y\":").append(n.getY())
                    .append("}");
        }
        sb.append("\n  ],\n");

        // ---- EDGES ----
        sb.append("  \"edges\": [\n");
        first = true;
        for (Edge e : graph.getEdges()) {
            if (!first) sb.append(",\n");
            first = false;

            sb.append("    {")
                    .append("\"from\":").append(e.getFromId()).append(",")
                    .append("\"to\":").append(e.getToId())
                    .append("}");
        }
        sb.append("\n  ]\n");

        sb.append("}\n");

        Files.writeString(path, sb.toString());
    }
}