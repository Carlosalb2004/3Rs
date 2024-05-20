package com.example.tecmovil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InterfazPrincipal extends AppCompatActivity {
    private final String nombre = Login.NOMBRE_USUARIO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_interfaz_principal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView textView7 = findViewById(R.id.textView7);
        String mensajeBienvenida = "Hola \n" + nombre.toUpperCase();
        textView7.setText(mensajeBienvenida);
    }
    public void misPuntos(View view){
        startActivity(new Intent(this, MisPuntos.class));
    }

     public void Reciclar(View view){
        startActivity(new Intent(this, ActividadReciclaje.class));
    }
}