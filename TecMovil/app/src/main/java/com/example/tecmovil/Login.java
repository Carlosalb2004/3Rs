package com.example.tecmovil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class Login extends AppCompatActivity {

    EditText correo;
    EditText pass;
    FirebaseFirestore db;
    public static String NOMBRE_USUARIO;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        correo = findViewById(R.id.correo);
        pass = findViewById(R.id.password);
        db = FirebaseFirestore.getInstance();
    }

    public void ingresar(View view) {
        String email = correo.getText().toString();
        String password = pass.getText().toString();

        if (!isValidEmail(email)) {
            correo.setError("Correo electrónico no válido");
            return;
        }

        // Obtener la referencia al documento del usuario con el correo electrónico proporcionado
        db.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // No se encontró ningún usuario con el correo electrónico proporcionado
                            Toast.makeText(Login.this, "Correo electrónico no registrado.", Toast.LENGTH_SHORT).show();
                        } else {
                            for (DocumentSnapshot document : task.getResult()) {
                                // Obtener la contraseña del documento
                                String storedPassword = document.getString("password");
                                if (storedPassword != null && storedPassword.equals(password)) {
                                    // Inicio de sesión exitoso
                                    NOMBRE_USUARIO = document.getString("nombre");
                                    startActivity(new Intent(Login.this, InterfazPrincipal.class));
                                } else {
                                    // Contraseña incorrecta
                                    Toast.makeText(Login.this, "Contraseña incorrecta.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } else {
                        // Error al obtener el documento del usuario
                        Toast.makeText(Login.this, "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }
}
