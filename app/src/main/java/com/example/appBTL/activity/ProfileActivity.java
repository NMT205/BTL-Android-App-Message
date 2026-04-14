package com.example.appBTL.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appBTL.R;
import com.example.appBTL.model.User;
import com.example.appBTL.utility.Constants;

public class ProfileActivity extends AppCompatActivity {
    ImageView loadAva;
    TextView btnBack, txtName, txtPhone, txtEmail, txtDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        loadAva=findViewById(R.id.loadAvaProfile);
        txtName=findViewById(R.id.txtNameProfile);
        txtPhone=findViewById(R.id.txtPhoneProfile);
        txtEmail=findViewById(R.id.txtEmailProfile);
        txtDate=findViewById(R.id.txtBirthProfile);
        btnBack=findViewById(R.id.btnBackChat);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        User user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        if (user != null) {
            txtName.setText(user.name);
            txtPhone.setText(user.phone);
            txtEmail.setText(user.email);
            txtDate.setText(user.date);
        }
        if(user.image != null){
            loadAva.setImageBitmap(getBitmap(user.image));
        }
    }
    private Bitmap getBitmap(String encodedImg){
        byte[] bytes = Base64.decode(encodedImg, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}