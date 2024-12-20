package com.example.enviaya;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreatePaqueteActivity extends AppCompatActivity {

    private EditText direccionEditText, pesoEditText;
    private Button guardarPaqueteButton;
    private DatabaseReference paquetesRef;
    private DatabaseReference reportesRef;
    private RadioGroup prioridadRadioGroup;
    private RadioButton prioridadAltaRadioButton, prioridadBajaRadioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_paquete);

        direccionEditText = findViewById(R.id.direccionEditText);
        pesoEditText = findViewById(R.id.pesoEditText);
        guardarPaqueteButton = findViewById(R.id.guardarPaqueteButton);

        prioridadRadioGroup = findViewById(R.id.prioridadRadioGroup);
        prioridadAltaRadioButton = findViewById(R.id.prioridadAltaRadioButton);
        prioridadBajaRadioButton = findViewById(R.id.prioridadBajaRadioButton);

        paquetesRef = FirebaseDatabase.getInstance().getReference("Paquetes");
        reportesRef = FirebaseDatabase.getInstance().getReference("Reportes");

        guardarPaqueteButton.setOnClickListener(v -> guardarPaquete());
    }

    private void guardarPaquete() {
        String direccion = direccionEditText.getText().toString().trim();
        String pesoStr = pesoEditText.getText().toString().trim();

        if (TextUtils.isEmpty(direccion) || TextUtils.isEmpty(pesoStr)) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double peso;
        try {
            peso = Double.parseDouble(pesoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El peso debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String prioridad;
        int selectedPriorityId = prioridadRadioGroup.getCheckedRadioButtonId();
        if (selectedPriorityId == R.id.prioridadAltaRadioButton) {
            prioridad = "alta";
        } else if (selectedPriorityId == R.id.prioridadBajaRadioButton) {
            prioridad = "baja";
        } else {
            Toast.makeText(this, "Por favor selecciona una prioridad", Toast.LENGTH_SHORT).show();
            return;
        }

        String estado = "registrado";

        String paqueteId = paquetesRef.push().getKey();
        String fotoUrl = "";
        Paquete paquete = new Paquete(paqueteId, estado, direccion, prioridad, peso, fotoUrl);

        paquetesRef.child(paqueteId).setValue(paquete)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Paquete guardado exitosamente", Toast.LENGTH_SHORT).show();
                        generarReporte(paqueteId, estado); // Generar reporte después de guardar el paquete
                        generarYGuardarImagen(paquete);
                        finish();
                    } else {
                        Toast.makeText(this, "Error al guardar paquete", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generarReporte(String paqueteId, String estado) {
        String idReporte = reportesRef.push().getKey();

        String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String hora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        Reporte reporte = new Reporte(idReporte, "", paqueteId, estado, hora, fecha);

        reportesRef.child(idReporte).setValue(reporte)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reporte generado exitosamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al generar reporte", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generarYGuardarImagen(Paquete paquete) {
        int width = 800;
        int height = 600;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(40);
        paint.setAntiAlias(true);


        int x = 50;
        int y = 100;
        canvas.drawText("Paquete Registrado", x, y, paint);
        y += 60;
        canvas.drawText("ID: " + paquete.getIdPaquete(), x, y, paint);
        y += 60;
        canvas.drawText("Dirección: " + paquete.getDireccionEntrega(), x, y, paint);
        y += 60;
        canvas.drawText("Peso: " + paquete.getPeso() + " kg", x, y, paint);
        y += 60;
        canvas.drawText("Prioridad: " + paquete.getPrioridad(), x, y, paint);
        y += 60;
        canvas.drawText("Estado: " + paquete.getEstado(), x, y, paint);

        guardarImagenEnDispositivo(bitmap);
    }

    private void guardarImagenEnDispositivo(Bitmap bitmap) {
        try {
            File directorio = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Paquetes");
            if (!directorio.exists()) {
                directorio.mkdirs();
            }

            File archivoImagen = new File(directorio, "paquete_" + System.currentTimeMillis() + ".png");
            FileOutputStream outputStream = new FileOutputStream(archivoImagen);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            Toast.makeText(this, "Imagen guardada en: " + archivoImagen.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
        }
    }
}
