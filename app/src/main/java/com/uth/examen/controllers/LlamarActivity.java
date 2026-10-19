package com.uth.examen.controllers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.uth.examen.R;
import com.uth.examen.dao.ContactoDAO;
import com.uth.examen.models.Contacto;
import com.uth.examen.utils.Utilidades;

/**
 * CONTROLADOR - Pantalla 3: Llamar.
 * Marca con ACTION_CALL solicitando el permiso CALL_PHONE en tiempo de ejecucion.
 */
public class LlamarActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_contacto";

    private Contacto contacto;

    // Solicitud del permiso CALL_PHONE en tiempo de ejecucion
    private final ActivityResultLauncher<String> pedirPermiso =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    realizarLlamada();
                } else {
                    Utilidades.alerta(this, "Se necesita el permiso de llamadas para marcar el número");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llamar);
        Utilidades.aplicarInsets(findViewById(R.id.main));

        int id = getIntent().getIntExtra(EXTRA_ID, 0);
        contacto = new ContactoDAO(this).buscarPorId(id);
        if (contacto == null) {
            Toast.makeText(this, "Contacto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView lblNombre = findViewById(R.id.lblNombre);
        TextView lblTelefono = findViewById(R.id.lblTelefono);
        Button btnAtras = findViewById(R.id.btnAtras);
        ImageButton btnLlamar = findViewById(R.id.btnLlamar);

        lblNombre.setText(contacto.getNombre());
        lblTelefono.setText(contacto.getTelefonoFormateado());

        btnAtras.setOnClickListener(v -> finish());
        btnLlamar.setOnClickListener(v -> confirmarLlamada());
    }

    /** Confirma la llamada con AlertDialog.Builder. */
    private void confirmarLlamada() {
        new AlertDialog.Builder(this)
                .setTitle("Acción")
                .setMessage("¿Desea llamar a " + contacto.getNombre() + "?")
                .setNegativeButton("No", null)
                .setPositiveButton("Sí", (d, w) -> llamar())
                .show();
    }

    /** Verifica el permiso CALL_PHONE y marca, o lo solicita. */
    private void llamar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            realizarLlamada();
        } else {
            pedirPermiso.launch(Manifest.permission.CALL_PHONE);
        }
    }

    private void realizarLlamada() {
        try {
            Intent llamar = new Intent(Intent.ACTION_CALL);
            llamar.setData(Uri.parse("tel:+" + contacto.getTelefonoCompleto()));
            startActivity(llamar);
        } catch (SecurityException e) {
            Utilidades.alerta(this, "Permiso de llamada no concedido");
        } catch (Exception e) {
            Utilidades.alerta(this, "No se pudo realizar la llamada: " + e.getMessage());
        }
    }
}
