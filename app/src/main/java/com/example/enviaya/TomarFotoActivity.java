package com.example.enviaya;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class TomarFotoActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private ImageView imageView;
    private Button btnTomarFoto;
    private Button btnSubirFoto;
    private Uri photoUri;
    private String idPaquete;
    private StorageReference storageRef;
    private DatabaseReference paquetesRef;

    private ActivityResultLauncher<Intent> takePictureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomar_foto);

        // Verificar si el permiso de la cámara ha sido concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

        imageView = findViewById(R.id.imageView);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnSubirFoto = findViewById(R.id.btnSubirFoto);

        // Obtener el ID del paquete desde el Intent
        idPaquete = getIntent().getStringExtra("idPaquete");

        storageRef = FirebaseStorage.getInstance().getReference("paquetesFotos");
        paquetesRef = FirebaseDatabase.getInstance().getReference("Paquetes");

        // Configurar el launcher para tomar fotos
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Mostrar la foto capturada en el ImageView
                        imageView.setImageURI(photoUri);
                    } else {
                        Toast.makeText(TomarFotoActivity.this, "No se tomó la foto", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Configurar los botones
        btnTomarFoto.setOnClickListener(v -> dispatchTakePictureIntent());
        btnSubirFoto.setOnClickListener(v -> {
            if (photoUri != null) {
                subirFoto(photoUri);
            }
        });
    }

    // El método para manejar la respuesta del permiso
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido para usar la cámara", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso denegado. No se puede usar la cámara", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Abrir la cámara para tomar una foto
    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = crearArchivoDeImagen();
                } catch (IOException ex) {
                    Log.e("TomarFotoActivity", "Error al crear el archivo", ex);
                }
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this, "com.example.enviaya.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    takePictureLauncher.launch(takePictureIntent); // Usar el launcher
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    // Crear un archivo para la foto
    private File crearArchivoDeImagen() throws IOException {
        String nombreImagen = "foto_paquete_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(nombreImagen, ".jpg", storageDir);
    }

    // Subir la foto a Firebase Storage
    private void subirFoto(Uri photoUri) {
        StorageReference fotoRef = storageRef.child(idPaquete + ".jpg");
        fotoRef.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> fotoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String fotoUrl = uri.toString();
                    actualizarFotoEnBaseDeDatos(fotoUrl);
                }))
                .addOnFailureListener(e -> Toast.makeText(TomarFotoActivity.this, "Error al subir la foto", Toast.LENGTH_SHORT).show());
    }

    // Actualizar la URL de la foto en Firebase Database
    private void actualizarFotoEnBaseDeDatos(String fotoUrl) {
        paquetesRef.child(idPaquete).child("fotoUrl").setValue(fotoUrl)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(TomarFotoActivity.this, "Foto subida exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(TomarFotoActivity.this, "Error al actualizar la URL", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
