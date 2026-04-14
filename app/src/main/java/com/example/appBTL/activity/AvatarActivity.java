package com.example.appBTL.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appBTL.R;
import com.example.appBTL.utility.Constants;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.appBTL.utility.PreferenceManager;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

public class AvatarActivity extends AppCompatActivity {
    MaterialCardView cardAva1, cardAva2, cardAva3, cardAva4, cardAva5, cardAddAva;
    MaterialCardView currentSelected = null;
    ImageView loadAva;
    Button btnSubmit1;
    View progressBar;
    Uri imageUri;
    String selectedImageBase64 = null;
    private PreferenceManager preferenceManager;
    ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            loadAva.setImageURI(imageUri);
                            selectedImageBase64 = encodeImage(imageUri);
                            if (currentSelected != null) {
                                currentSelected.setStrokeWidth(0);
                                currentSelected = null;
                            }
                        }
                    }
            );
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_avatar1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        preferenceManager=new PreferenceManager(getApplicationContext());
        loadAva=findViewById(R.id.loadAva);
        cardAva1=findViewById(R.id.cardAva1);
        cardAva2=findViewById(R.id.cardAva2);
        cardAva3=findViewById(R.id.cardAva3);
        cardAva4=findViewById(R.id.cardAva4);
        cardAva5=findViewById(R.id.cardAva5);
        cardAddAva=findViewById(R.id.cardAddAva);
        btnSubmit1=findViewById(R.id.btnSubmit1);
        progressBar=findViewById(R.id.progressBar);
        Intent editIntent=getIntent();
        String name=editIntent.getStringExtra("name");
        String phone=editIntent.getStringExtra("phone");
        String email=editIntent.getStringExtra("email");
        String pass=editIntent.getStringExtra("password");
        String dateOfBirth=editIntent.getStringExtra("date");
        View.OnClickListener listener = v -> {
            MaterialCardView card = (MaterialCardView) v;
            if (currentSelected != null) {
                currentSelected.setStrokeWidth(0);
            }
            card.setStrokeWidth(6);
            card.setStrokeColor(Color.parseColor("#00BCD4"));
            currentSelected = card;
            ImageView img = (ImageView) card.getChildAt(0);
            loadAva.setImageDrawable(img.getDrawable());
            selectedImageBase64 = encodeDrawable(img);
            imageUri = null;
        };
        cardAva1.setOnClickListener(listener);
        cardAva2.setOnClickListener(listener);
        cardAva3.setOnClickListener(listener);
        cardAva4.setOnClickListener(listener);
        cardAva5.setOnClickListener(listener);
        cardAddAva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                pickImageLauncher.launch(intent);
            }
        });
        btnSubmit1.setOnClickListener(v -> {
            loading(true);

            if (selectedImageBase64 == null) {
                Toast.makeText(this, "Chưa chọn ảnh!", Toast.LENGTH_SHORT).show();
                loading(false);
                return;
            }
            saveUserToFirestore(name, phone, email, pass, dateOfBirth, selectedImageBase64);
        });
    }
    private void saveUserToFirestore(String name, String phone, String email,
                                     String pass, String dateOfBirth, String imageUrl){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, name);
        user.put(Constants.KEY_PHONE, phone);
        user.put(Constants.KEY_EMAIL, email);
        user.put(Constants.KEY_PASSWORD, pass);
        user.put(Constants.KEY_DATE, dateOfBirth);
        user.put(Constants.KEY_IMAGE, imageUrl);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, name);
                    preferenceManager.putString(Constants.KEY_IMAGE, imageUrl);
                    Intent intent=new Intent(AvatarActivity.this, Mess1Activity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    Toast.makeText(AvatarActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private String encodeImage(Uri uri){
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            bitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    private String encodeDrawable(ImageView imageView){
        try {
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();
            Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable) imageView.getDrawable()).getBitmap();
            bitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    private void loading(boolean isLoading){
        if(isLoading){
            btnSubmit1.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
        else{
            btnSubmit1.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
