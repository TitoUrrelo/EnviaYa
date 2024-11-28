package com.example.enviaya;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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

    private EditText etDescripcionProblema;

    private String idPaquete;
    private String idConductor;
    private StorageReference storageRef;
    private DatabaseReference reportesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomar_foto);

        tvPaqueteInfo = findViewById(R.id.tvPaqueteInfo);
        rgEstado = findViewById(R.id.rgEstado);
        rbEntregado = findViewById(R.id.rbEntregado);
        rbDevuelto = findViewById(R.id.rbDevuelto);
        ivFoto = findViewById(R.id.ivFoto);
        btnGuardarReporte = findViewById(R.id.btnGuardarReporte);

        etDescripcionProblema = findViewById(R.id.etDescripcionProblema);

        idPaquete = getIntent().getStringExtra("idPaquete");
        idConductor = FirebaseAuth.getInstance().getCurrentUser().getUid();
        storageRef = FirebaseStorage.getInstance().getReference("paquetesFotos");
        reportesRef = FirebaseDatabase.getInstance().getReference("Reportes");

        DatabaseReference paquetesRef = FirebaseDatabase.getInstance().getReference("Paquetes");
        paquetesRef.child(idPaquete).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String direccion = snapshot.child("direccionEntrega").getValue(String.class);
                String estado = snapshot.child("estado").getValue(String.class);
                double peso = snapshot.child("peso").getValue(Double.class);

                tvPaqueteInfo.setText("ID: " + idPaquete + "\nDirección: " + direccion + "\nEstado: " + estado + "\nPeso: " + peso + " kg");
            }
        });

        rgEstado.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbEntregado) {
                etDescripcionProblema.setVisibility(View.GONE);
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            } else if (checkedId == R.id.rbDevuelto) {
                etDescripcionProblema.setVisibility(View.VISIBLE);
                ivFoto.setImageURI(null);
                ivFoto.setVisibility(View.GONE);
                photoUri = null;
            }
        });
        btnGuardarReporte.setOnClickListener(v -> {
            String estado = rbEntregado.isChecked() ? "Entregado" : "Devuelto";

            if (estado.equals("Entregado") && photoUri == null) {
                Toast.makeText(this, "Selecciona una foto para el estado 'Entregado'", Toast.LENGTH_SHORT).show();
                return;
            }

            if (estado.equals("Devuelto") && etDescripcionProblema.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Ingresa una razón para la devolución", Toast.LENGTH_SHORT).show();
                return;
            }

            guardarReporte(estado);
        });
    }

    private void guardarReporte(String estado) {
        String idReporte = reportesRef.push().getKey();
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String fechaActual = sdfDate.format(Calendar.getInstance().getTime());
        String horaActual = sdfTime.format(Calendar.getInstance().getTime());
        Reporte reporte = new Reporte(
                idReporte,
                idConductor,
                idPaquete,
                estado,
                horaActual,
                fechaActual
        );

        reportesRef.child(idReporte).setValue(reporte);

        if (estado.equals("Devuelto")) {
            guardarProblema();
        } else if (estado.equals("Entregado")) {
            subirFoto(photoUri, idReporte);
        } else {
            Toast.makeText(this, "Reporte guardado correctamente", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void guardarProblema() {
        String idProblema = reportesRef.push().getKey();
        String descripcion = etDescripcionProblema.getText().toString().trim();

        Problema problema = new Problema(
                idProblema,
                idConductor,
                idPaquete,
                descripcion,
                new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime()),
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime())
        );
        DatabaseReference problemasRef = FirebaseDatabase.getInstance().getReference("Problemas");
        problemasRef.child(idProblema).setValue(problema).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DatabaseReference paquetesRef = FirebaseDatabase.getInstance().getReference("Paquetes");
                paquetesRef.child(idPaquete).child("estado").setValue("Devuelto").addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(this, "Problema y estado del paquete actualizados correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al actualizar el estado del paquete", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Error al guardar el problema", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subirFoto(Uri uri, String idReporte) {
        StorageReference fotoRef = storageRef.child("reportes/" + idReporte + ".jpg");
        fotoRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fotoRef.getDownloadUrl().addOnSuccessListener(url -> {
                    reportesRef.child(idReporte).child("fotoUrl").setValue(url.toString());
                    Toast.makeText(this, "Reporte guardado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Error al subir la foto", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
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
