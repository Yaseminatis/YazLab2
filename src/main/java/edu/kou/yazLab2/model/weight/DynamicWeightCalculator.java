package edu.kou.yazLab2.model.weight;

import edu.kou.yazLab2.model.Node;

public class DynamicWeightCalculator {

    // İsterdeki formül:
    // w(i,j) = 1 / ( 1 + (Ai-Aj)^2 + (Ei-Ej)^2 + (Bi-Bj)^2 )
    public double weight(Node a, Node b) {
        double da = a.getAktiflik() - b.getAktiflik();
        double de = a.getEtkilesim() - b.getEtkilesim();
        double db = a.getBaglantiSayisi() - b.getBaglantiSayisi();

        double sum = (da * da) + (de * de) + (db * db);
        return 1.0 / (1.0 + sum);
    }
}