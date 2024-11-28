package com.example.enviaya;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.IOException;

public class TomarFotoActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 1;

    private TextView tvPaqueteInfo;
    private RadioGroup rgEstado;
    private RadioButton rbEntregado;
    private RadioButton rbDevuelto;
    private ImageView ivFoto;
    private Button btnGuardarReporte;
    private Uri photoUri;

    private String idPaquete;
    private StorageReference storageRef;
    private DatabaseReference paquetesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomar_foto);

        // Inicializar vistas
        tvPaqueteInfo = findViewById(R.id.tvPaqueteInfo);
        rgEstado = findViewById(R.id.rgEstado);
        rbEntregado = findViewById(R.id.rbEntregado);
        rbDevuelto = findViewById(R.id.rbDevuelto);
        ivFoto = findViewById(R.id.ivFoto);
        btnGuardarReporte = findViewById(R.id.btnGuardarReporte);

        // Configurar referencias de Firebase
        idPaquete = getIntent().getStringExtra("idPaquete");
        storageRef = FirebaseStorage.getInstance().getReference("paquetesFotos");
        paquetesRef = FirebaseDatabase.getInstance().getReference("Paquetes");

        // Obtener datos del paquete y mostrar en el TextView
        paquetesRef.child(idPaquete).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String direccion = snapshot.child("direccionEntrega").getValue(String.class);
                String estado = snapshot.child("estado").getValue(String.class);
                tvPaqueteInfo.setText("ID: " + idPaquete + "\nDirección: " + direccion + "\nEstado: " + estado);
            }
        });

        // Configurar RadioGroup para manejar estados
        rgEstado.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbEntregado) {
                // Abrir galería para seleccionar foto
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            } else if (checkedId == R.id.rbDevuelto) {
                // Ocultar la imagen si se selecciona "Devuelto"
                ivFoto.setImageURI(null);
                ivFoto.setVisibility(View.GONE);
                photoUri = null;
            }
        });

        // Configurar botón para guardar reporte
        btnGuardarReporte.setOnClickListener(v -> {
            String estado = rbEntregado.isChecked() ? "Entregado" : "Devuelto";

            // Validar selección de foto si es "Entregado"
            if (estado.equals("Entregado") && photoUri == null) {
                Toast.makeText(this, "Selecciona una foto para el estado 'Entregado'", Toast.LENGTH_SHORT).show();
                return;
            }

            guardarReporte(estado);
        });
    }

    // Método para guardar el reporte en Firebase
    private void guardarReporte(String estado) {
        String idReporte = paquetesRef.push().getKey();
        Reporte reporte = new Reporte(
                idReporte,
                "ID_CONDUCTOR", // Obtener dinámicamente el ID del conductor
                idPaquete,
                estado,
                "10:00 AM", // Reemplazar con la hora actual
                "2024-11-27" // Reemplazar con la fecha actual
        );

        // Actualizar estado del paquete y guardar reporte
        paquetesRef.child(idPaquete).child("estado").setValue(estado);
        paquetesRef.child("Reportes").child(idReporte).setValue(reporte);

        // Subir foto si es "Entregado"
        if (estado.equals("Entregado")) {
            subirFoto(photoUri, idReporte);
        } else {
            Toast.makeText(this, "Reporte guardado correctamente", Toast.LENGTH_SHORT).show();
            finish(); // Cierra esta actividad y regresa a la anterior
        }
    }

    // Subir foto a Firebase Storage
    private void subirFoto(Uri uri, String idReporte) {
        StorageReference fotoRef = storageRef.child("reportes/" + idReporte + ".jpg");
        fotoRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fotoRef.getDownloadUrl().addOnSuccessListener(url -> {
                    paquetesRef.child("Reportes").child(idReporte).child("fotoUrl").setValue(url.toString());
                    Toast.makeText(this, "Reporte guardado correctamente", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra esta actividad después de subir la foto
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Error al subir la foto", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Obtener la URI de la foto seleccionada
            photoUri = data.getData();
            ivFoto.setVisibility(View.VISIBLE);
            ivFoto.setImageURI(photoUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido para usar la cámara", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso denegado. No se puede usar la cámara", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
