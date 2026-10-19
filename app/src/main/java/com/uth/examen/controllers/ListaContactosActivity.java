package com.uth.examen.controllers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.uth.examen.R;
import com.uth.examen.dao.ContactoDAO;
import com.uth.examen.models.Contacto;
import com.uth.examen.utils.Utilidades;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * CONTROLADOR - Pantalla 2: Lista de contactos guardados.
 */
public class ListaContactosActivity extends AppCompatActivity {

    private ListView lstContactos;
    private EditText txtBuscar;
    private TextView lblVacio;

    private ContactoDAO contactoDAO;
    private final List<Contacto> todos = new ArrayList<>();
    private final List<Contacto> filtrados = new ArrayList<>();
    private ArrayAdapter<Contacto> adapter;
    private Contacto seleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_contactos);
        Utilidades.aplicarInsets(findViewById(R.id.main));

        contactoDAO = new ContactoDAO(this);

        lstContactos = findViewById(R.id.lstContactos);
        txtBuscar = findViewById(R.id.txtBuscar);
        lblVacio = findViewById(R.id.lblVacio);
        Button btnAtras = findViewById(R.id.btnAtras);
        Button btnCompartir = findViewById(R.id.btnCompartir);
        Button btnVerImagen = findViewById(R.id.btnVerImagen);
        Button btnEliminar = findViewById(R.id.btnEliminar);
        Button btnActualizar = findViewById(R.id.btnActualizar);

        adapter = new ArrayAdapter<>(this, R.layout.item_contacto, filtrados);
        lstContactos.setAdapter(adapter);
        lstContactos.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Al seleccionar: confirmar llamada con AlertDialog.Builder
        lstContactos.setOnItemClickListener((parent, view, position, id) -> {
            seleccionado = filtrados.get(position);
            lstContactos.setItemChecked(position, true);
            confirmarLlamada(seleccionado);
        });

        txtBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { filtrar(s.toString()); }
            @Override public void afterTextChanged(Editable s) { }
        });

        btnAtras.setOnClickListener(v -> finish());
        btnCompartir.setOnClickListener(v -> compartirContacto());
        btnVerImagen.setOnClickListener(v -> verImagen());
        btnEliminar.setOnClickListener(v -> eliminarContacto());
        btnActualizar.setOnClickListener(v -> actualizarContacto());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarContactos();
    }

    // ---------------- Listar / Buscar ----------------

    private void cargarContactos() {
        todos.clear();
        todos.addAll(contactoDAO.listar());
        filtrar(txtBuscar.getText().toString());
    }

    private void filtrar(String texto) {
        String q = texto.trim().toLowerCase(Locale.getDefault());
        filtrados.clear();
        for (Contacto c : todos) {
            if (q.isEmpty()
                    || c.getNombre().toLowerCase(Locale.getDefault()).contains(q)
                    || c.getTelefonoCompleto().contains(q)) {
                filtrados.add(c);
            }
        }
        lstContactos.clearChoices();
        seleccionado = null;
        adapter.notifyDataSetChanged();
        lblVacio.setVisibility(filtrados.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean haySeleccion() {
        if (seleccionado == null) {
            Utilidades.alerta(this, "Seleccione un contacto de la lista");
            return false;
        }
        return true;
    }

    // ---------------- Llamar ----------------

    private void confirmarLlamada(Contacto c) {
        new AlertDialog.Builder(this)
                .setTitle("Acción")
                .setMessage("¿Desea llamar a " + c.getNombre() + "?")
                .setNegativeButton("No", (d, w) -> d.dismiss())
                .setPositiveButton("Sí", (d, w) -> {
                    Intent i = new Intent(this, LlamarActivity.class);
                    i.putExtra(LlamarActivity.EXTRA_ID, c.getId());
                    startActivity(i);
                })
                .show();
    }

    // ---------------- Compartir (Intent.ACTION_SEND) ----------------

    private void compartirContacto() {
        if (!haySeleccion()) return;
        Contacto c = seleccionado;

        String texto = "Contacto: " + c.getNombre() + "\n"
                + "Teléfono: " + c.getTelefonoFormateado() + "\n"
                + "País: " + c.getPais() + "\n"
                + "Nota: " + c.getNota();

        Intent enviar = new Intent(Intent.ACTION_SEND);
        enviar.putExtra(Intent.EXTRA_SUBJECT, "Contacto: " + c.getNombre());
        enviar.putExtra(Intent.EXTRA_TEXT, texto);

        File foto = c.getImagen() == null || c.getImagen().isEmpty() ? null : new File(c.getImagen());
        if (foto != null && foto.exists()) {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", foto);
            enviar.setType("image/jpeg");
            enviar.putExtra(Intent.EXTRA_STREAM, uri);
            enviar.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            enviar.setType("text/plain");
        }
        startActivity(Intent.createChooser(enviar, "Compartir contacto con..."));
    }

    // ---------------- Ver imagen ----------------

    private void verImagen() {
        if (!haySeleccion()) return;
        String ruta = seleccionado.getImagen();
        if (ruta == null || ruta.isEmpty() || !new File(ruta).exists()) {
            Utilidades.alerta(this, "El contacto " + seleccionado.getNombre() + " no tiene imagen guardada");
            return;
        }
        Bitmap bmp = Utilidades.cargarImagen(ruta, 1000, 1000);
        ImageView img = new ImageView(this);
        img.setAdjustViewBounds(true);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int pad = (int) (12 * getResources().getDisplayMetrics().density);
        img.setPadding(pad, pad, pad, pad);
        img.setImageBitmap(bmp);

        new AlertDialog.Builder(this)
                .setTitle(seleccionado.getNombre())
                .setView(img)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    // ---------------- Eliminar ----------------

    private void eliminarContacto() {
        if (!haySeleccion()) return;
        Contacto c = seleccionado;
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Desea eliminar a " + c.getNombre() + "?")
                .setNegativeButton("No", null)
                .setPositiveButton("Sí", (d, w) -> {
                    if (contactoDAO.eliminar(c.getId()) > 0) {
                        if (c.getImagen() != null && !c.getImagen().isEmpty()) {
                            new File(c.getImagen()).delete();
                        }
                        cargarContactos();
                        Utilidades.alerta(this, "Contacto eliminado");
                    }
                })
                .show();
    }

    // ---------------- Actualizar ----------------

    private void actualizarContacto() {
        if (!haySeleccion()) return;
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(MainActivity.EXTRA_ID, seleccionado.getId());
        startActivity(i);
    }
}
