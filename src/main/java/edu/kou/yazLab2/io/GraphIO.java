package edu.kou.yazLab2.io;

import edu.kou.yazLab2.model.Graph;

import java.nio.file.Path;

public interface GraphIO {
    Graph load(Path path) throws Exception;
    void save(Graph graph, Path path) throws Exception;
}