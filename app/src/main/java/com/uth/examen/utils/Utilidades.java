package com.uth.examen.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Utilidades comunes: alertas, insets de pantalla y carga de imagenes. */
public final class Utilidades {

    private Utilidades() { }

    /** Muestra una alerta con titulo "Alerta". */
    public static void alerta(Context ctx, String mensaje) {
        new AlertDialog.Builder(ctx)
                .setTitle("Alerta")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    /** Evita que el contenido quede debajo de la barra de estado / navegacion / teclado. */
    public static void aplicarInsets(View root) {
        final int l = root.getPaddingLeft(), t = root.getPaddingTop(),
                r = root.getPaddingRight(), b = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets s = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(l + s.left, t + s.top, r + s.right, b + s.bottom);
            return insets;
        });
    }

    /** Carga una imagen reducida para no agotar memoria. */
    public static Bitmap cargarImagen(String ruta, int anchoMax, int altoMax) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(ruta, op);
        int escala = 1;
        while (op.outWidth / (escala * 2) >= anchoMax && op.outHeight / (escala * 2) >= altoMax) {
            escala *= 2;
        }
        op.inJustDecodeBounds = false;
        op.inSampleSize = escala;
        return BitmapFactory.decodeFile(ruta, op);
    }
}
