package com.uth.examen.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Crea y actualiza la base de datos SQLite. */
public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, DBConfig.DB_NAME, null, DBConfig.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBConfig.CREATE_TABLE_CONTACTOS);
        db.execSQL(DBConfig.CREATE_TABLE_PAISES);

        for (String[] p : DBConfig.PAISES_INICIALES) {
            ContentValues v = new ContentValues();
            v.put(DBConfig.P_NOMBRE, p[0]);
            v.put(DBConfig.P_CODIGO, p[1]);
            db.insert(DBConfig.TABLA_PAISES, null, v);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DBConfig.DROP_TABLE_CONTACTOS);
        db.execSQL(DBConfig.DROP_TABLE_PAISES);
        onCreate(db);
    }
}
