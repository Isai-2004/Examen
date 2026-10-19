package com.uth.examen.models;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MODELO: representa un contacto de la agenda.
 * Campos: id, pais, nombre, telefono, nota, imagen (ruta de la fotografia).
 */
public class Contacto {

    private int id;
    private String pais;      // Ej: "Honduras (504)"
    private String nombre;
    private String telefono;  // 8 digitos
    private String nota;
    private String imagen;    // ruta absoluta de la foto

    public Contacto() { }

    public Contacto(int id, String pais, String nombre, String telefono, String nota, String imagen) {
        this.id = id;
        this.pais = pais;
        this.nombre = nombre;
        this.telefono = telefono;
        this.nota = nota;
        this.imagen = imagen;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    /** Obtiene el codigo de area del pais, ej. "504" de "Honduras (504)". */
    public String getCodigoPais() {
        if (pais == null) return "";
        Matcher m = Pattern.compile("\\((\\d+)\\)").matcher(pais);
        return m.find() ? m.group(1) : "";
    }

    /** Numero completo para marcar, ej. "50499765645". */
    public String getTelefonoCompleto() {
        return getCodigoPais() + telefono;
    }

    /** Numero con formato, ej. "+504 9976-5645". */
    public String getTelefonoFormateado() {
        String t = telefono == null ? "" : telefono;
        if (t.length() == 8) t = t.substring(0, 4) + "-" + t.substring(4);
        return "+" + getCodigoPais() + " " + t;
    }

    /** Texto mostrado en la lista: "Nombre | 50499765645". */
    @Override
    public String toString() {
        return "● " + nombre + " | " + getTelefonoCompleto();
    }
}
