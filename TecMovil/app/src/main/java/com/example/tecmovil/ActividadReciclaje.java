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
        setContentView(R.layout.activity_actividadreciclaje);  // Cambia "tu_layout_xml" por el nombre real de tu archivo XML

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
                if (auth.getCurrentUser() != null) {
                    openCamera();
                } else {
                    Toast.makeText(ActividadReciclaje.this, "Debes iniciar sesión para tomar una foto", Toast.LENGTH_SHORT).show();
                }
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
                if (radioGroupPoints.getCheckedRadioButtonId() == -1 || radioGroupMaterials.getCheckedRadioButtonId() == -1 || kilos == 0) {
                    Toast.makeText(ActividadReciclaje.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                } else {
                    // Placeholder para la funcionalidad que se debe implementar tras verificar los campos
                }
            }
        });
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No hay ninguna aplicación de cámara disponible", Toast.LENGTH_SHORT).show();
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

    private void uploadImageToFirebase(Bitmap bitmap) {
        if (auth.getCurrentUser() != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString() + ".jpg");

            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            saveRecyclingData(imageUrl);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ActividadReciclaje.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(ActividadReciclaje.this, "Usuario no autenticado. Por favor, inicie sesión.", Toast.LENGTH_SHORT).show();
            // Aquí puedes redirigir al usuario a la pantalla de inicio de sesión si lo deseas
        }
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
                            // Aquí puedes redirigir al usuario a la pantalla principal o realizar otras acciones
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
}