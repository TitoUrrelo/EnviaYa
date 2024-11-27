package com.example.enviaya;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class cambiarContra extends AppCompatActivity {

    private EditText editTextEmail;
    private Button buttonResetPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_contra);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Enlazar vistas
        editTextEmail = findViewById(R.id.editTextEmailContra);
        buttonResetPassword = findViewById(R.id.buttonResetPasswordContra);

        // Acción al presionar el botón
        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(cambiarContra.this, "Por favor, ingresa un correo válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Enviar correo para restablecer contraseña
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(cambiarContra.this, "Correo enviado para restablecer contraseña", Toast.LENGTH_SHORT).show();
                            } else {
                                if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                    Toast.makeText(cambiarContra.this, "El correo no está registrado", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(cambiarContra.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}
