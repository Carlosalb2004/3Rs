package com.example.tecmovil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
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

    private void validateAndSaveRecyclingData() {
        if (radioGroupPoints.getCheckedRadioButtonId() == -1 || radioGroupMaterials.getCheckedRadioButtonId() == -1 || kilos == 0) {
            Toast.makeText(ActividadReciclaje.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        String puntoDeEntrega = ((RadioButton)findViewById(radioGroupPoints.getCheckedRadioButtonId())).getText().toString();
        String material = ((RadioButton)findViewById(radioGroupMaterials.getCheckedRadioButtonId())).getText().toString();

        // Aquí podrías añadir una verificación adicional para comprobar datos duplicados o lo que necesites.
        // Ejemplo: comprobar si ya se ha registrado un reciclaje en la misma fecha, etc.

        // Si todo está correcto, proceder a abrir la cámara para tomar foto
        openCamera();
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
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            uploadImageToFirebase(imageBitmap);
        }
    }

    private void uploadImageToFirebase(final Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString() + ".jpg");

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ActividadReciclaje.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Obtiene la URL de la imagen subida
                        String imageUrl = uri.toString();
                        saveRecyclingData(imageUrl);
                    }
                });
            }
        });
    }

    private void saveRecyclingData(String imageUrl) {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            Map<String, Object> data = new HashMap<>();
            data.put("puntoDeEntrega", ((RadioButton)findViewById(radioGroupPoints.getCheckedRadioButtonId())).getText().toString());
            data.put("material", ((RadioButton)findViewById(radioGroupMaterials.getCheckedRadioButtonId())).getText().toString());
            data.put("kilos", kilos);
            data.put("imageUrl", imageUrl);

            db.collection("usuarios").document(userId)
                    .collection("reciclajes").add(data)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(ActividadReciclaje.this, "Se subio su reciclaje con éxito", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ActividadReciclaje.this, "Error al guardar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permisos concedidos, la cámara puede ser abierta
            } else {
                Toast.makeText(this, "Permisos de cámara no concedidos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sendData(View view) {
        if (radioGroupPoints.getCheckedRadioButtonId() == -1 || radioGroupMaterials.getCheckedRadioButtonId() == -1 || kilos == 0) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
        } else {
            // Puedes optar por abrir la cámara aquí si es parte de la lógica de enviar datos
            openCamera();
        }
    }

}
