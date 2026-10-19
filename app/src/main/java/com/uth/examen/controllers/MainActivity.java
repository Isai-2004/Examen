package com.uth.examen.controllers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.uth.examen.R;
import com.uth.examen.dao.ContactoDAO;
import com.uth.examen.dao.PaisDAO;
import com.uth.examen.models.Contacto;
import com.uth.examen.models.Pais;
import com.uth.examen.utils.Utilidades;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * CONTROLADOR - Pantalla 1: Registro (y edicion) de contactos.
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_contacto";

    // Expresiones regulares
    private static final String REGEX_NOMBRE = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü]+( [A-Za-zÁÉÍÓÚáéíóúÑñÜü]+)*$";
    private static final String REGEX_TELEFONO = "^[0-9]{8}$";

    private ImageView imgFoto;
    private Spinner spPais;
    private EditText txtNombre, txtTelefono, txtNota;
    private Button btnSalvar;

    private ContactoDAO contactoDAO;
    private PaisDAO paisDAO;
    private List<Pais> paises;

    private String rutaImagen = "";     // foto confirmada
    private String rutaTemporal = "";   // foto en proceso de captura
    private int idEdicion = 0;          // > 0 cuando se actualiza un contacto

    /**
     * Contrato TakePicture que pide abrir la camara FRONTAL (selfie).
     * En el emulador la camara frontal esta conectada a la camara web de la computadora.
     */
    private static class TomarSelfie extends ActivityResultContracts.TakePicture {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, @NonNull Uri input) {
            Intent intent = super.createIntent(context, input);
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
            return intent;
        }
    }

    // Resultado de la camara (selfie)
    private final ActivityResultLauncher<Uri> tomarSelfie =
            registerForActivityResult(new TomarSelfie(), exito -> {
                if (Boolean.TRUE.equals(exito)) {
                    rutaImagen = rutaTemporal;
                    mostrarImagen();
                } else {
                    new File(rutaTemporal).delete();
                }
            });

    // Permiso de camara en tiempo de ejecucion
    private final ActivityResultLauncher<String> permisoCamara =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) abrirCamara();
                else Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utilidades.aplicarInsets(findViewById(R.id.main));

        contactoDAO = new ContactoDAO(this);
        paisDAO = new PaisDAO(this);

        imgFoto = findViewById(R.id.imgFoto);
        spPais = findViewById(R.id.spPais);
        txtNombre = findViewById(R.id.txtNombre);
        txtTelefono = findViewById(R.id.txtTelefono);
        txtNota = findViewById(R.id.txtNota);
        btnSalvar = findViewById(R.id.btnSalvar);
        ImageButton btnFoto = findViewById(R.id.btnFoto);
        ImageButton btnAgregarPais = findViewById(R.id.btnAgregarPais);
        Button btnContactos = findViewById(R.id.btnContactos);

        cargarPaises();

        if (savedInstanceState != null) {
            rutaImagen = savedInstanceState.getString("rutaImagen", "");
            rutaTemporal = savedInstanceState.getString("rutaTemporal", "");
            mostrarImagen();
        }

        // Modo edicion
        idEdicion = getIntent().getIntExtra(EXTRA_ID, 0);
        if (idEdicion > 0 && savedInstanceState == null) {
            cargarContactoEdicion();
        }
        if (idEdicion > 0) {
            btnSalvar.setText("Actualizar Contacto");
            btnContactos.setText("Cancelar");
        }

        btnFoto.setOnClickListener(v -> solicitarCamara());
        btnAgregarPais.setOnClickListener(v -> dialogoAgregarPais());
        btnSalvar.setOnClickListener(v -> salvarContacto());
        btnContactos.setOnClickListener(v -> {
            if (idEdicion > 0) {
                finish();
            } else {
                startActivity(new Intent(this, ListaContactosActivity.class));
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("rutaImagen", rutaImagen);
        outState.putString("rutaTemporal", rutaTemporal);
    }

    // ---------------- Paises ----------------

    private void cargarPaises() {
        paises = paisDAO.listar();
        ArrayAdapter<Pais> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, paises);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPais.setAdapter(adapter);
    }

    private void seleccionarPais(String texto) {
        for (int i = 0; i < paises.size(); i++) {
            if (paises.get(i).toString().equals(texto)) {
                spPais.setSelection(i);
                return;
            }
        }
    }

    /** Punto extra: agregar paises desde la app. */
    private void dialogoAgregarPais() {
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        contenedor.setPadding(pad, pad / 2, pad, 0);

        EditText txtPais = new EditText(this);
        txtPais.setHint("Nombre del país");
        txtPais.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        EditText txtCodigo = new EditText(this);
        txtCodigo.setHint("Código de área (ej. 505)");
        txtCodigo.setInputType(InputType.TYPE_CLASS_NUMBER);
        contenedor.addView(txtPais);
        contenedor.addView(txtCodigo);

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle("Agregar país")
                .setView(contenedor)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();
        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String nombre = txtPais.getText().toString().trim();
                    String codigo = txtCodigo.getText().toString().trim();
                    if (nombre.isEmpty() || !nombre.matches(REGEX_NOMBRE)) {
                        Utilidades.alerta(this, "debe escribir un nombre de país válido (solo letras)");
                    } else if (!codigo.matches("^[0-9]{1,4}$")) {
                        Utilidades.alerta(this, "debe escribir un código de área de 1 a 4 dígitos");
                    } else {
                        paisDAO.insertar(nombre, codigo);
                        cargarPaises();
                        seleccionarPais(nombre + " (" + codigo + ")");
                        Toast.makeText(this, "País agregado", Toast.LENGTH_SHORT).show();
                        dialogo.dismiss();
                    }
                }));
        dialogo.show();
    }

    // ---------------- Fotografia ----------------

    private void solicitarCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            permisoCamara.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamara() {
        try {
            File carpeta = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String nombre = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date()) + ".jpg";
            File archivo = new File(carpeta, nombre);
            rutaTemporal = archivo.getAbsolutePath();
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", archivo);
            tomarSelfie.launch(uri);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir la cámara: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarImagen() {
        if (rutaImagen != null && !rutaImagen.isEmpty() && new File(rutaImagen).exists()) {
            Bitmap bmp = Utilidades.cargarImagen(rutaImagen, 600, 400);
            if (bmp != null) {
                imgFoto.setImageBitmap(bmp);
                return;
            }
        }
        imgFoto.setImageResource(R.drawable.ic_persona);
    }

    // ---------------- Guardar / Actualizar ----------------

    private void cargarContactoEdicion() {
        Contacto c = contactoDAO.buscarPorId(idEdicion);
        if (c == null) {
            idEdicion = 0;
            return;
        }
        seleccionarPais(c.getPais());
        txtNombre.setText(c.getNombre());
        txtTelefono.setText(c.getTelefono());
        txtNota.setText(c.getNota());
        rutaImagen = c.getImagen() == null ? "" : c.getImagen();
        mostrarImagen();
    }

    /** Valida campos obligatorios y formato con expresiones regulares. */
    private boolean validar() {
        String nombre = txtNombre.getText().toString().trim();
        String telefono = txtTelefono.getText().toString().trim();
        String nota = txtNota.getText().toString().trim();

        if (spPais.getSelectedItem() == null) {
            Utilidades.alerta(this, "debe seleccionar un país");
            return false;
        }
        if (nombre.isEmpty()) {
            Utilidades.alerta(this, "debe escribir un nombre");
            txtNombre.requestFocus();
            return false;
        }
        if (!nombre.matches(REGEX_NOMBRE)) {
            Utilidades.alerta(this, "el nombre solo puede contener letras");
            txtNombre.requestFocus();
            return false;
        }
        if (telefono.isEmpty() || !telefono.matches(REGEX_TELEFONO)) {
            Utilidades.alerta(this, "debe escribir un teléfono de 8 dígitos");
            txtTelefono.requestFocus();
            return false;
        }
        if (nota.isEmpty()) {
            Utilidades.alerta(this, "debe escribir una nota");
            txtNota.requestFocus();
            return false;
        }
        return true;
    }

    private void salvarContacto() {
        if (!validar()) return;

        Contacto c = new Contacto(
                idEdicion,
                spPais.getSelectedItem().toString(),
                txtNombre.getText().toString().trim(),
                txtTelefono.getText().toString().trim(),
                txtNota.getText().toString().trim(),
                rutaImagen);

        if (idEdicion > 0) {
            if (contactoDAO.actualizar(c) > 0) {
                Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Utilidades.alerta(this, "No se pudo actualizar el contacto");
            }
        } else {
            if (contactoDAO.insertar(c) > 0) {
                Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show();
                limpiar();
            } else {
                Utilidades.alerta(this, "No se pudo guardar el contacto");
            }
        }
    }

    private void limpiar() {
        txtNombre.setText("");
        txtTelefono.setText("");
        txtNota.setText("");
        rutaImagen = "";
        rutaTemporal = "";
        if (!paises.isEmpty()) spPais.setSelection(0);
        mostrarImagen();
        txtNombre.requestFocus();
    }
}
