package com.uth.examen.models;

/** MODELO: pais con su codigo de marcado. */
public class Pais {

    private int id;
    private String nombre;
    private String codigo;

    public Pais(int id, String nombre, String codigo) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCodigo() { return codigo; }

    /** Texto mostrado en el Spinner, ej. "Honduras (504)". */
    @Override
    public String toString() {
        return nombre + " (" + codigo + ")";
    }
}
