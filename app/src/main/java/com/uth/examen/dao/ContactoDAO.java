package com.uth.examen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.uth.examen.database.DBConfig;
import com.uth.examen.database.DBHelper;
import com.uth.examen.models.Contacto;

import java.util.ArrayList;
import java.util.List;

/** DAO: operaciones CRUD de la tabla contactos. */
public class ContactoDAO {

    private final DBHelper helper;

    public ContactoDAO(Context context) {
        helper = new DBHelper(context);
    }

    private ContentValues toValues(Contacto c) {
        ContentValues v = new ContentValues();
        v.put(DBConfig.C_PAIS, c.getPais());
        v.put(DBConfig.C_NOMBRE, c.getNombre());
        v.put(DBConfig.C_TELEFONO, c.getTelefono());
        v.put(DBConfig.C_NOTA, c.getNota());
        v.put(DBConfig.C_IMAGEN, c.getImagen());
        return v;
    }

    private Contacto fromCursor(Cursor cur) {
        return new Contacto(
                cur.getInt(cur.getColumnIndexOrThrow(DBConfig.C_ID)),
                cur.getString(cur.getColumnIndexOrThrow(DBConfig.C_PAIS)),
                cur.getString(cur.getColumnIndexOrThrow(DBConfig.C_NOMBRE)),
                cur.getString(cur.getColumnIndexOrThrow(DBConfig.C_TELEFONO)),
                cur.getString(cur.getColumnIndexOrThrow(DBConfig.C_NOTA)),
                cur.getString(cur.getColumnIndexOrThrow(DBConfig.C_IMAGEN)));
    }

    /** Inserta un contacto. Devuelve el id generado o -1 si falla. */
    public long insertar(Contacto c) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insert(DBConfig.TABLA_CONTACTOS, null, toValues(c));
        db.close();
        return id;
    }

    /** Actualiza un contacto. Devuelve filas afectadas. */
    public int actualizar(Contacto c) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int filas = db.update(DBConfig.TABLA_CONTACTOS, toValues(c),
                DBConfig.C_ID + "=?", new String[]{String.valueOf(c.getId())});
        db.close();
        return filas;
    }

    /** Elimina un contacto por id. Devuelve filas afectadas. */
    public int eliminar(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int filas = db.delete(DBConfig.TABLA_CONTACTOS,
                DBConfig.C_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return filas;
    }

    /** Lista todos los contactos ordenados por nombre. */
    public List<Contacto> listar() {
        List<Contacto> lista = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cur = db.query(DBConfig.TABLA_CONTACTOS, null, null, null,
                null, null, DBConfig.C_NOMBRE + " COLLATE NOCASE ASC");
        while (cur.moveToNext()) {
            lista.add(fromCursor(cur));
        }
        cur.close();
        db.close();
        return lista;
    }

    /** Busca un contacto por id. */
    public Contacto buscarPorId(int id) {
        Contacto c = null;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cur = db.query(DBConfig.TABLA_CONTACTOS, null,
                DBConfig.C_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cur.moveToFirst()) {
            c = fromCursor(cur);
        }
        cur.close();
        db.close();
        return c;
    }
}
