package com.example.tecmovil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Login extends AppCompatActivity {
    EditText correo;
    EditText pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        correo= findViewById(R.id.correo);
        pass = findViewById(R.id.password);
    }
    public void ingresar(View view){
        if(correo.getText().toString().equalsIgnoreCase("admin") && pass.getText().toString().equalsIgnoreCase("admin")){
            startActivity(new Intent(this, InterfazPrincipal.class));
        }
        else{
            Toast.makeText(this, "Datos de Inicio Incorrectos", Toast.LENGTH_LONG).show();
        }
    }

}