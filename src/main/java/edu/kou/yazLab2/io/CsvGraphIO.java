package edu.kou.yazLab2.io;

import edu.kou.yazLab2.model.Graph;
import edu.kou.yazLab2.model.Node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CsvGraphIO implements GraphIO {

    private static final double MIN_X = 50, MIN_Y = 50;
    private static final double MAX_X = 850, MAX_Y = 550;

    @Override
    public Graph load(Path path) throws IOException {
        Graph g = new Graph();

        // 1) CSV satırlarını oku
        // id -> rowData (aktiflik, etkilesim, baglanti, komsular)
        Map<Integer, Row> rows = new LinkedHashMap<>();

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String header = br.readLine(); // başlık
            if (header == null) return g;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                // Basit CSV parse (tırnaklı komşular dahil)
                List<String> parts = splitCsvLine(line);
                if (parts.size() < 5) continue;

                int id = Integer.parseInt(parts.get(0).trim());
                double aktiflik = Double.parseDouble(parts.get(1).trim());
                double etkilesim = Double.parseDouble(parts.get(2).trim());
                int baglantiSayisi = Integer.parseInt(parts.get(3).trim());
                String komsularStr = parts.get(4).trim().replace("\"", "");

                rows.put(id, new Row(id, aktiflik, etkilesim, baglantiSayisi, komsularStr));
            }
        }


        // 2) Node’ları ekle (x/y CSV’de yok → grid layout, her seferinde aynı)
        int count = rows.size();
        if (count == 0) return g;

        double width = (MAX_X - MIN_X);
        double height = (MAX_Y - MIN_Y);

// kaç kolon olsun? (yaklaşık kareye yakın)
        int cols = (int) Math.ceil(Math.sqrt(count));
        int rowsCount = (int) Math.ceil((double) count / cols);

// hücre aralıkları
        double cellW = width / (cols + 1);
        double cellH = height / (rowsCount + 1);

        int idx = 0;
        for (Row r : rows.values()) {
            int row = idx / cols;
            int col = idx % cols;

            double x = MIN_X + (col + 1) * cellW;
            double y = MIN_Y + (row + 1) * cellH;

            Node n = new Node(r.id, x, y);
            n.setAktiflik(r.aktiflik);
            n.setEtkilesim(r.etkilesim);
            n.setBaglantiSayisi(r.baglantiSayisi);

            g.addNode(n);
            idx++;
        }

        // 3) Edge’leri komşulardan üret (undirected, duplicate engelli)
        for (Row r : rows.values()) {
            List<Integer> komsular = parseNeighbors(r.komsularStr);
            for (int nb : komsular) {
                if (!rows.containsKey(nb)) continue; // CSV'de olmayan komşu varsa geç
                // duplicate olmaması için sadece küçük->büyük ekle
                if (r.id < nb) {
                    try {
                        g.addEdge(r.id, nb);
                    } catch (Exception ignore) { }
                }
            }
        }

        return g;
    }

    @Override
    public void save(Graph graph, Path path) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(path)) {
            bw.write("DugumId,Aktiflik,Etkilesim,BaglantiSayisi,Komsular");
            bw.newLine();

            for (Node n : graph.getNodes()) {
                int id = n.getId();
                // komşuluk listesi
                List<Integer> neighbors = graph.getNeighbors(id);
                String komsular = String.join(",", neighbors.stream().map(String::valueOf).toList());

                // bağlantiSayisi istersen node'dan, istersen neighbors.size()
                int baglantiSayisi = n.getBaglantiSayisi();

                bw.write(id + "," +
                        fmt(n.getAktiflik()) + "," +
                        fmt(n.getEtkilesim()) + "," +
                        baglantiSayisi + "," +
                        "\"" + komsular + "\"");
                bw.newLine();
            }
        }
    }

    private static String fmt(double v) {
        // TR virgül olmasın diye nokta ile yaz
        return String.format(java.util.Locale.US, "%.2f", v);
    }

    private static List<Integer> parseNeighbors(String s) {
        if (s == null) return List.of();
        s = s.trim();
        if (s.isEmpty()) return List.of();

        String[] arr = s.split(",");
        List<Integer> out = new ArrayList<>();
        for (String a : arr) {
            String t = a.trim();
            if (t.isEmpty()) continue;
            out.add(Integer.parseInt(t));
        }
        return out;
    }

    // Çok basit CSV split: virgülleri ayırır, tırnak içini tek parça tutar
    private static List<String> splitCsvLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                sb.append(c);
            } else if (c == ',' && !inQuotes) {
                parts.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        parts.add(sb.toString());
        return parts;
    }

    private static class Row {
        int id;
        double aktiflik;
        double etkilesim;
        int baglantiSayisi;
        String komsularStr;

        Row(int id, double aktiflik, double etkilesim, int baglantiSayisi, String komsularStr) {
            this.id = id;
            this.aktiflik = aktiflik;
            this.etkilesim = etkilesim;
            this.baglantiSayisi = baglantiSayisi;
            this.komsularStr = komsularStr;
        }
    }
}