package com.uth.examen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.uth.examen.database.DBConfig;
import com.uth.examen.database.DBHelper;
import com.uth.examen.models.Pais;

import java.util.ArrayList;
import java.util.List;

/** DAO: operaciones de la tabla paises (permite agregar paises desde la app). */
public class PaisDAO {

    private final DBHelper helper;

    public PaisDAO(Context context) {
        helper = new DBHelper(context);
    }

    public long insertar(String nombre, String codigo) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DBConfig.P_NOMBRE, nombre);
        v.put(DBConfig.P_CODIGO, codigo);
        long id = db.insert(DBConfig.TABLA_PAISES, null, v);
        db.close();
        return id;
    }

    public List<Pais> listar() {
        List<Pais> lista = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cur = db.query(DBConfig.TABLA_PAISES, null, null, null,
                null, null, DBConfig.P_ID + " ASC");
        while (cur.moveToNext()) {
            lista.add(new Pais(
                    cur.getInt(cur.getColumnIndexOrThrow(DBConfig.P_ID)),
                    cur.getString(cur.getColumnIndexOrThrow(DBConfig.P_NOMBRE)),
                    cur.getString(cur.getColumnIndexOrThrow(DBConfig.P_CODIGO))));
        }
        cur.close();
        db.close();
        return lista;
    }
}
