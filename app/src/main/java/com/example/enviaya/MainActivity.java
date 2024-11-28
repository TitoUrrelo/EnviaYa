package com.example.enviaya;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Usuarios");

        // Inicializar vistas
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        loginButton = findViewById(R.id.button);

        // Configurar botón de inicio de sesión
        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim().toLowerCase();
        String password = passwordEditText.getText().toString().trim();

        // Validar entradas
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Por favor, ingresa correo y contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Iniciar sesión con Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Verificar si el correo está verificado
                            if (firebaseUser.isEmailVerified()) {
                                // Cargar los datos del usuario
                                loadUserData(firebaseUser.getUid(), email);
                            } else {
                                Toast.makeText(this, "Debes verificar tu correo electrónico.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Autenticación fallida. Verifica tus credenciales.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserData(String userId, String email) {
        usersRef.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    if (usuario != null) {
                        // Navegar según el rol del usuario
                        navigateToHome(usuario, email); // Pasar el correo normalizado
                    }
                } else {
                    Toast.makeText(this, "Usuario no encontrado en la base de datos.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error al cargar datos de usuario.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void navigateToHome(Usuario usuario, String email) {
        Intent intent;

        switch (usuario.getTipoUsuario()) {
            case "admin":
                intent = new Intent(this, AdminActivity.class);
                break;
            case "conductor":
                intent = new Intent(this, ConductorActivity.class);
                break;
            default:
                Toast.makeText(this, "Tipo de usuario no reconocido.", Toast.LENGTH_SHORT).show();
                return;
        }
        intent.putExtra("email", email);

        startActivity(intent);
        finish(); // Cierra la actividad de login
    }

    public void cambiarContra(View view) {
        Intent intent = new Intent(this, cambiarContra.class);
        startActivity(intent);
    }
}
