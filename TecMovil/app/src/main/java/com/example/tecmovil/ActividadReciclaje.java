package com.example.tecmovil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActividadReciclaje extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri photoURI;
    private RadioGroup radioGroupPoints, radioGroupMaterials;
    private TextView textViewKilosAmount;
    private StorageReference storageRef;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividadreciclaje);

        storageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        radioGroupPoints = findViewById(R.id.radioGroupPoints);
        radioGroupMaterials = findViewById(R.id.radioGroupMaterials);
        textViewKilosAmount = findViewById(R.id.textViewKilosAmount);

        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        Button btnSend = findViewById(R.id.btnSend);
        Button btnDecrease = findViewById(R.id.btnDecrease);
        Button btnIncrease = findViewById(R.id.btnIncrease);

        btnTakePhoto.setOnClickListener(v -> dispatchTakePictureIntent());
        btnSend.setOnClickListener(v -> sendData());

        btnDecrease.setOnClickListener(v -> adjustKilos(-1));
        btnIncrease.setOnClickListener(v -> adjustKilos(1));
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error al crear el archivo de imagen.", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this, "com.example.tecmovil.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void uploadImageToFirebase() {
        if (photoURI != null) {
            StorageReference fileRef = storageRef.child("reciclajeImages/" + photoURI.getLastPathSegment());
            fileRef.putFile(photoURI)
                    .addOnSuccessListener(taskSnapshot -> Toast.makeText(ActividadReciclaje.this, "Imagen subida con éxito", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(ActividadReciclaje.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Primero toma una foto.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendData() {
        RadioButton selectedPoint = findViewById(radioGroupPoints.getCheckedRadioButtonId());
        RadioButton selectedMaterial = findViewById(radioGroupMaterials.getCheckedRadioButtonId());
        String kilos = textViewKilosAmount.getText().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("lugarEntrega", selectedPoint.getText().toString());
        data.put("material", selectedMaterial.getText().toString());
        data.put("kilos", kilos);

        db.collection("reciclajeData").add(data)
                .addOnSuccessListener(documentReference -> {
                    String docId = documentReference.getId();
                    updateDocumentWithImage(docId);
                    Toast.makeText(ActividadReciclaje.this, "Datos guardados con éxito", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(ActividadReciclaje.this, "Error al guardar datos", Toast.LENGTH_SHORT).show());
    }

    private void updateDocumentWithImage(String documentId) {
        StorageReference fileRef = storageRef.child("reciclajeImages/" + photoURI.getLastPathSegment());
        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put("imageUrl", uri.toString());
            db.collection("reciclajeData").document(documentId).update(updates);
        }).addOnFailureListener(e -> Toast.makeText(ActividadReciclaje.this, "Error al obtener URL de la imagen", Toast.LENGTH_SHORT).show());
    }

    private void adjustKilos(int adjustment) {
        int currentKilos = Integer.parseInt(textViewKilosAmount.getText().toString());
        currentKilos += adjustment;
        if (currentKilos < 0) {
            currentKilos = 0;
        }
        textViewKilosAmount.setText(String.valueOf(currentKilos));
    }
}
