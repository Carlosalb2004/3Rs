package com.example.tecmovil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ActividadReciclaje extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private static final int REQUEST_IMAGE_CAPTURE = 202;
    private Button btnTakePhoto, btnIncrease, btnDecrease, btnSend;
    private TextView textViewKilosAmount, labelsView;
    private RadioGroup radioGroupPoints, radioGroupMaterials;
    private int kilos = 0;
    private Bitmap imageBitmap = null; // Almacena la foto tomada
    private ImageView imageViewCaptured;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = auth.getCurrentUser();
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("usuarios");
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images");

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
        labelsView = findViewById(R.id.labels_view);
        imageViewCaptured = findViewById(R.id.imageViewCaptured);

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
            try {
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");
                if (imageBitmap != null) {
                    imageViewCaptured.setImageBitmap(imageBitmap);
                    imageViewCaptured.setVisibility(View.VISIBLE);  // Hacer visible la imagen
                    Toast.makeText(this, "Foto tomada con éxito", Toast.LENGTH_SHORT).show();
                    // Procesar imagen para etiquetado
                    processImageForLabeling(imageBitmap);
                } else {
                    Toast.makeText(this, "Error al capturar la imagen. Intenta de nuevo.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al procesar la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void processImageForLabeling(Bitmap bitmap) {
        if (bitmap != null) {
            InputImage image;
            try {
                image = InputImage.fromBitmap(bitmap, 0);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al convertir la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            ImageLabeler labeler= ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

            labeler.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                        @Override
                        public void onSuccess(List<ImageLabel> labels) {
                            StringBuilder labelText = new StringBuilder("Detectado:\n");
                            for (ImageLabel label : labels) {
                                labelText.append(label.getText()).append(" - ")
                                        .append(String.format("%.1f%%", label.getConfidence() * 100)).append("\n");
                            }
                            labelsView.setText(labelText.toString());
                            labelsView.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ActividadReciclaje.this, "Error al etiquetar la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "La imagen no puede ser nula.", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateAndSaveRecyclingData() {
        if (imageBitmap == null) {
            Toast.makeText(ActividadReciclaje.this, "Por favor, primero toma una foto.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (radioGroupPoints.getCheckedRadioButtonId() == -1 || radioGroupMaterials.getCheckedRadioButtonId() == -1 || kilos == 0) {
            Toast.makeText(ActividadReciclaje.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadImageAndSaveData();
    }

    private void uploadImageAndSaveData() {
        // Subir la imagen al Storage de Firebase
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Obtener el nombre del usuario actual
        String userName = currentUser.getDisplayName();
        String userEmail = currentUser.getEmail();

        // Crear un nombre único para la imagen
        String imageName = "image_" + userName + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(imageName);

        // Subir la imagen al Storage
        UploadTask uploadTask = imageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
                    // Imagen subida con éxito, obtener su URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        // Crear una entrada en la base de datos para el usuario
                        DatabaseReference userRef = usersRef.child(currentUser.getUid());
                        userRef.child("nombre").setValue(userName);
                        userRef.child("correo").setValue(userEmail);

                        // Crear una nueva entrada en la base de datos para el reciclaje
                        DatabaseReference reciclajesRef = userRef.child("reciclajes").push();
                        String key = reciclajesRef.getKey();

                        Map<String, Object> reciclaje = new HashMap<>();
                        reciclaje.put("imageUrl", imageUrl);
                        reciclaje.put("kilos", kilos);
                        reciclaje.put("material", ((RadioButton)findViewById(radioGroupMaterials.getCheckedRadioButtonId())).getText().toString());
                        reciclaje.put("puntoDeEntrega", ((RadioButton)findViewById(radioGroupPoints.getCheckedRadioButtonId())).getText().toString());

                        // Guardar los datos del reciclaje bajo la nueva clave
                        reciclajesRef.setValue(reciclaje)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ActividadReciclaje.this, "Datos de reciclaje guardados correctamente", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ActividadReciclaje.this, InterfazPrincipal.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ActividadReciclaje.this, "Error al guardar los datos del reciclaje: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    // Error al subir la imagen
                    Toast.makeText(ActividadReciclaje.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos de cámara SI concedidos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permisos de cámara NO concedidos", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

