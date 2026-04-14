package com.example.appBTL.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appBTL.R;

public class SignUpActivity1 extends AppCompatActivity {
    TextView btnBack1;
    Button btnGG, btnFB, btnSUEmail;;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_signup1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnBack1=findViewById(R.id.btnBack1);
        btnSUEmail=findViewById(R.id.btnSUEmail);
        btnGG=findViewById(R.id.btnGG);
        btnFB=findViewById(R.id.btnFB);
        btnBack1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnSUEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent suEmailIntent=new Intent(SignUpActivity1.this, SignUpActivity2.class);
                startActivity(suEmailIntent);
            }
        });
        btnGG.setOnClickListener(v -> {
            Toast.makeText(SignUpActivity1.this, "đăng ký bằng google", Toast.LENGTH_LONG).show();
        });
        btnFB.setOnClickListener(v -> {
            Toast.makeText(SignUpActivity1.this, "đăng ký bằng facebook", Toast.LENGTH_LONG).show();
        });
    }
}
