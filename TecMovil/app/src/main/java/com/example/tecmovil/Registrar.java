package com.example.tecmovil;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registrar extends AppCompatActivity {

    private EditText editTextNombre;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button btnRegistrar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Conectar con los elementos de la UI
        editTextNombre = findViewById(R.id.editTextText2);
        editTextEmail = findViewById(R.id.editTextText3);
        editTextPassword = findViewById(R.id.editTextTextPassword2);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });
    }

    private void registrarUsuario() {
        String nombre = editTextNombre.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Registrar.this, "Por favor, complete todos los campos.", Toast.LENGTH_LONG).show();
        } else {
            // Crear un mapa con los datos del usuario
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("nombre", nombre);
            usuario.put("email", email);
            usuario.put("password", password);

            // Agregar el usuario a Firestore
            db.collection("usuarios")
                    .add(usuario)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(Registrar.this, "Usuario registrado con éxito.", Toast.LENGTH_SHORT).show();
                            // Aquí puedes redirigir al usuario a la pantalla principal o realizar otras acciones
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Registrar.this, "Error al registrar el usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}



