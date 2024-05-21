package com.example.tecmovil;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActividadReciclaje extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private static final int REQUEST_IMAGE_CAPTURE = 202;
    private Button btnTakePhoto, btnIncrease, btnDecrease, btnSend;
    private TextView textViewKilosAmount;
    private RadioGroup radioGroupPoints, radioGroupMaterials;
    private int kilos = 0;

    private Bitmap capturedImage;
    private String imageLocalPath;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividadreciclaje);

        // Comprueba y solicita permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        }

        // Inicialización de los componentes de la UI
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnSend = findViewById(R.id.btnSend);
        textViewKilosAmount = findViewById(R.id.textViewKilosAmount);
        radioGroupPoints = findViewById(R.id.radioGroupPoints);
        radioGroupMaterials = findViewById(R.id.radioGroupMaterials);

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        btnIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kilos++;
                textViewKilosAmount.setText(String.valueOf(kilos));
            }
        });

        btnDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (kilos > 0) {
                    kilos--;
                    textViewKilosAmount.setText(String.valueOf(kilos));
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSaveRecyclingData();
            }
        });
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            capturedImage = (Bitmap) extras.get("data");
            // Guardar la imagen capturada en almacenamiento local
            saveImageLocally(capturedImage);
        }
    }

    private void saveImageLocally(Bitmap imageBitmap) {
        // Guardar la imagen en el almacenamiento interno del dispositivo
        String filename = "captured_image.jpg";
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            imageLocalPath = file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void validateAndSaveRecyclingData() {
        if (radioGroupPoints.getCheckedRadioButtonId() == -1 || radioGroupMaterials.getCheckedRadioButtonId() == -1 || kilos == 0) {
            Toast.makeText(ActividadReciclaje.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si se ha capturado una imagen
        if (imageLocalPath == null || imageLocalPath.isEmpty()) {
            Toast.makeText(ActividadReciclaje.this, "No se ha capturado ninguna imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        // Subir la imagen al Firebase Storage
        uploadImageToFirebase();
    }

    private void uploadImageToFirebase() {
        // Referencia al Storage de Firebase
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Crear una referencia única para la imagen en Firebase Storage
        String imageName = "image_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child("images/" + imageName);

        // Subir la imagen al Storage de Firebase
        Uri imageUri = Uri.fromFile(new File(imageLocalPath));
        UploadTask uploadTask = imageRef.putFile(imageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Manejar el fallo de la carga de la imagen
                Toast.makeText(ActividadReciclaje.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // La imagen se subió exitosamente
                // Aquí puedes obtener la URL de la imagen y guardarla si es necesario
                Toast.makeText(ActividadReciclaje.this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show();
            }
        });
    }


}


