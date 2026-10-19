package com.uth.examen.database;

/** Configuracion de la base de datos SQLite (nombres de tablas, campos y sentencias DDL). */
public class DBConfig {

    public static final String DB_NAME = "agenda_contactos.db";
    public static final int DB_VERSION = 1;

    // ---------- Tabla contactos ----------
    public static final String TABLA_CONTACTOS = "contactos";
    public static final String C_ID = "id";
    public static final String C_PAIS = "pais";
    public static final String C_NOMBRE = "nombre";
    public static final String C_TELEFONO = "telefono";
    public static final String C_NOTA = "nota";
    public static final String C_IMAGEN = "imagen";

    public static final String CREATE_TABLE_CONTACTOS =
            "CREATE TABLE " + TABLA_CONTACTOS + " (" +
                    C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    C_PAIS + " TEXT NOT NULL, " +
                    C_NOMBRE + " TEXT NOT NULL, " +
                    C_TELEFONO + " TEXT NOT NULL, " +
                    C_NOTA + " TEXT NOT NULL, " +
                    C_IMAGEN + " TEXT)";

    public static final String DROP_TABLE_CONTACTOS = "DROP TABLE IF EXISTS " + TABLA_CONTACTOS;

    // ---------- Tabla paises (punto extra) ----------
    public static final String TABLA_PAISES = "paises";
    public static final String P_ID = "id";
    public static final String P_NOMBRE = "nombre";
    public static final String P_CODIGO = "codigo";

    public static final String CREATE_TABLE_PAISES =
            "CREATE TABLE " + TABLA_PAISES + " (" +
                    P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    P_NOMBRE + " TEXT NOT NULL, " +
                    P_CODIGO + " TEXT NOT NULL)";

    public static final String DROP_TABLE_PAISES = "DROP TABLE IF EXISTS " + TABLA_PAISES;

    public static final String[][] PAISES_INICIALES = {
            {"Honduras", "504"},
            {"Costa Rica", "506"},
            {"Guatemala", "502"},
            {"El Salvador", "503"}
    };
}
