package com.example.tecmovil;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class InterfazPrincipal extends AppCompatActivity {
    private FirebaseAuth auth;
    private Button buttonLogout;
    private TextView textViewUserDetails;
    private ViewPager viewPagerTips;

    private String[] tips = {"Tip 1: Recicla papel y cartón.",
            "Tip 2: Usa bolsas reutilizables en lugar de bolsas de plástico.",
            "Tip 3: Reduce el consumo de agua en casa.",
            "Tip 4: Utiliza bombillas LED para ahorrar energía."};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interfaz_principal);

        auth = FirebaseAuth.getInstance();
        buttonLogout = findViewById(R.id.logout);
        textViewUserDetails = findViewById(R.id.user_details);
        viewPagerTips = findViewById(R.id.viewPagerTips);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userName = user.getDisplayName();
            if (userName != null && !userName.isEmpty()) {
                textViewUserDetails.setText("Bienvenido, " + userName);
            } else {
                textViewUserDetails.setText("Bienvenido, Usuario");
            }
        } else {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }

        buttonLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(InterfazPrincipal.this, Login.class));
            finish();
        });

        TipsPagerAdapter tipsPagerAdapter = new TipsPagerAdapter(this, tips);
        viewPagerTips.setAdapter(tipsPagerAdapter);
    }

    public void misPuntos(View view) {
        startActivity(new Intent(this, MisPuntos.class));
    }

    public void Reciclar(View view) {
        startActivity(new Intent(this, ActividadReciclaje.class));
    }
}




