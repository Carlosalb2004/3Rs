package com.example.tecmovil;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
import com.google.firebase.firestore.QuerySnapshot;

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

        // Validar los campos de entrada
        if (!isValidName(nombre)) {
            editTextNombre.setError("El nombre no puede contener números ni caracteres especiales");
            return;
        }

        if (!isValidEmail(email)) {
            editTextEmail.setError("Correo electrónico no válido");
            return;
        }

        if (!isValidPassword(password)) {
            editTextPassword.setError("La contraseña debe tener al menos 8 caracteres, incluyendo al menos una letra mayúscula, una letra minúscula, un número y un carácter especial");
            return;
        }

        // Verificar si el correo electrónico ya está registrado
        db.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // El correo electrónico ya está registrado
                            Toast.makeText(Registrar.this, "Este correo electrónico ya está registrado", Toast.LENGTH_SHORT).show();
                        } else {
                            // Registrar el nuevo usuario
                            Map<String, Object> usuario = new HashMap<>();
                            usuario.put("nombre", nombre);
                            usuario.put("email", email);
                            usuario.put("password", password);

                            db.collection("usuarios")
                                    .add(usuario)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(Registrar.this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
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
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Registrar.this, "Error al verificar el correo electrónico: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    private boolean isValidPassword(String password) {
        return (password != null && password.length() >= 8 && password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).*$"));
    }

    private boolean isValidName(String name) {
        return (!TextUtils.isEmpty(name) && name.matches("[a-zA-Z\\s]+"));
    }
}



