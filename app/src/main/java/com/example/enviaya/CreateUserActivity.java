package com.example.enviaya;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateUserActivity extends AppCompatActivity {

    private LinearLayout adminForm, conductorForm;
    private Button btnAdmin, btnConductor, crearAdminButton, crearConductorButton;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        adminForm = findViewById(R.id.adminForm);
        conductorForm = findViewById(R.id.conductorForm);
        btnAdmin = findViewById(R.id.btnAdmin);
        btnConductor = findViewById(R.id.btnConductor);
        crearAdminButton = findViewById(R.id.crearAdminButton);
        crearConductorButton = findViewById(R.id.crearConductorButton);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Usuarios");

        btnAdmin.setOnClickListener(v -> mostrarFormularioAdmin());
        btnConductor.setOnClickListener(v -> mostrarFormularioConductor());

        crearAdminButton.setOnClickListener(v -> crearAdmin());
        crearConductorButton.setOnClickListener(v -> crearConductor());
    }

    private void mostrarFormularioAdmin() {
        adminForm.setVisibility(View.VISIBLE);
        conductorForm.setVisibility(View.GONE);
    }

    private void mostrarFormularioConductor() {
        adminForm.setVisibility(View.GONE);
        conductorForm.setVisibility(View.VISIBLE);
    }

    private void crearAdmin() {
        String nombre = ((EditText) findViewById(R.id.nombreAdminEditText)).getText().toString().trim();
        String correo = ((EditText) findViewById(R.id.correoAdminEditText)).getText().toString().trim();
        String telefono = ((EditText) findViewById(R.id.telefonoAdminEditText)).getText().toString().trim();

        // Validación de campos
        if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(correo, "=,Z]=opQ(5c7kY0<Oz2<") // temporal
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        if (verifyTask.isSuccessful()) {
                                            String userId = user.getUid();
                                            Usuario admin = new Usuario(userId, nombre, correo, telefono, "admin");

                                            usersRef.child(userId).setValue(admin)
                                                    .addOnCompleteListener(dbTask -> {
                                                        if (dbTask.isSuccessful()) {
                                                            Toast.makeText(CreateUserActivity.this, "Administrador creado exitosamente, correo de verificación enviado.", Toast.LENGTH_LONG).show();
                                                        } else {
                                                            Toast.makeText(CreateUserActivity.this, "Error al crear administrador en la base de datos", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(CreateUserActivity.this, "Error al enviar correo de verificación", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(CreateUserActivity.this, "Error al crear administrador", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void crearConductor() {

        String nombre = ((EditText) findViewById(R.id.nombreConductorEditText)).getText().toString().trim();
        String correo = ((EditText) findViewById(R.id.correoConductorEditText)).getText().toString().trim();
        String telefono = ((EditText) findViewById(R.id.telefonoConductorEditText)).getText().toString().trim();
        String matricula = ((EditText) findViewById(R.id.matriculaEditText)).getText().toString().trim();
        String licencia = ((EditText) findViewById(R.id.licenciaEditText)).getText().toString().trim();

        if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || matricula.isEmpty() || licencia.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(correo, "=,Z]=opQ(5c7kY0<Oz2<") // temporal
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        if (verifyTask.isSuccessful()) {
                                            String userId = user.getUid();
                                            Usuario conductor = new Usuario(userId, nombre, correo, telefono, "conductor", matricula, licencia, true);

                                            usersRef.child(userId).setValue(conductor)
                                                    .addOnCompleteListener(dbTask -> {
                                                        if (dbTask.isSuccessful()) {
                                                            Toast.makeText(CreateUserActivity.this, "Conductor creado exitosamente, correo de verificación enviado.", Toast.LENGTH_LONG).show();
                                                        } else {
                                                            Toast.makeText(CreateUserActivity.this, "Error al crear conductor en la base de datos", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(CreateUserActivity.this, "Error al enviar correo de verificación", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(CreateUserActivity.this, "Error al crear conductor", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validarCampos(String nombre, String correo, String telefono, String contraseña) {
        if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || contraseña.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Correo no válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (telefono.length() < 10) {
            Toast.makeText(this, "Teléfono debe tener al menos 10 dígitos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (contraseña.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void guardarUsuarioEnDB(String userId, Usuario usuario, String mensajeExito) {
        usersRef.child(userId).setValue(usuario)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, mensajeExito, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al guardar el usuario en la base de datos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void manejarErrores(Exception e) {
        if (e != null && e.getMessage().contains("email")) {
            Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al crear usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
