package com.example.tecmovil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActividadReciclaje extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri photoURI;
    private Button btnTakePhoto;
    private Button btnSend;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividadreciclaje);

        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSend = findViewById(R.id.btnSend);

        // Inicializar Firebase Storage
        storageRef = FirebaseStorage.getInstance().getReference();

        btnTakePhoto.setOnClickListener(v -> {
            dispatchTakePictureIntent();
        });

        btnSend.setOnClickListener(v -> {
            uploadImageToFirebase();
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Asegurar que hay una actividad de cámara para manejar el intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Crear el archivo donde irá la foto
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error ocurrido mientras se creaba el archivo
                Toast.makeText(this, "Error al crear el archivo de imagen.", Toast.LENGTH_SHORT).show();
            }
            // Continuar solo si el archivo fue creado exitosamente
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Crear un nombre de archivo de imagen único
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    private void uploadImageToFirebase() {
        if (photoURI != null) {
            StorageReference fileRef = storageRef.child("images/" + photoURI.getLastPathSegment());
            fileRef.putFile(photoURI).addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(ActividadReciclaje.this, "Imagen subida con éxito", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(ActividadReciclaje.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Primero toma una foto.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Foto guardada en: " + photoURI.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
