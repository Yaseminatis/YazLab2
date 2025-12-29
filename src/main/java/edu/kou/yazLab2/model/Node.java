package edu.kou.yazLab2.model;

import java.util.Objects;

/**
 * Graph düğümü.
 * - id: benzersiz kimlik
 * - x,y: konum (dinamik ağırlık hesaplarında kullanılır)
 */
public class Node {

    private final int id;
    private final double x;
    private final double y;

    private double aktiflik;
    private double etkilesim;
    private int baglantiSayisi;
    private int secilmeSayisi;
    private int ziyaretSayisi;
    public Node(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
        // default değerler
        this.aktiflik = 0.0;
        this.etkilesim = 0.0;
        this.baglantiSayisi = 0;

    }
    public void secildi() { this.secilmeSayisi++; }
    public void ziyaretEdildi() { this.ziyaretSayisi++; }

    public int getSecilmeSayisi() { return secilmeSayisi; }
    public int getZiyaretSayisi() { return ziyaretSayisi; }
    public void aktiflikGuncelle() {
        // Node ne kadar seçilirse o kadar aktif
        this.aktiflik = Math.min(1.0, 0.0 + 0.1 * secilmeSayisi);
    }

    public void etkilesimGuncelle() {
        // Algoritmalar node'u ne kadar ziyaret ettiyse
        this.etkilesim = ziyaretSayisi;
    }
    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    public double getAktiflik() { return aktiflik; }
    public void setAktiflik(double aktiflik) { this.aktiflik = aktiflik; }

    public double getEtkilesim() { return etkilesim; }
    public void setEtkilesim(double etkilesim) { this.etkilesim = etkilesim; }

    public int getBaglantiSayisi() { return baglantiSayisi; }
    public void setBaglantiSayisi(int baglantiSayisi) { this.baglantiSayisi = baglantiSayisi; }
    // Node eşitliği yalnızca id üzerinden
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node node)) return false;
        return id == node.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    @Override
    public String toString() {
        return "Node{id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", aktiflik=" + aktiflik +
                ", etkilesim=" + etkilesim +
                ", baglantiSayisi=" + baglantiSayisi +
                "}";
    }
}
