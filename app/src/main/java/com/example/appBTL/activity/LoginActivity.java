package com.example.appBTL.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appBTL.R;
import com.example.appBTL.utility.Constants;
import com.example.appBTL.utility.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    TextView btnSignUp1;
    EditText inputEmail1, inputPass1;
    Button btnLogin;
    ImageView btnGG, btnFB;
    ProgressBar progressBar;
    private PreferenceManager preferenceManager;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_login);
        preferenceManager=new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intentMess=new Intent(LoginActivity.this, Mess1Activity.class);
            startActivity(intentMess);
            finish();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnSignUp1=findViewById(R.id.btnSignUp1);
        btnLogin=findViewById(R.id.btnLogin);
        btnGG=findViewById(R.id.btnGG1);
        btnFB=findViewById(R.id.btnFB1);
        inputEmail1=findViewById(R.id.inputEmail1);
        inputPass1=findViewById(R.id.inputPass1);
        progressBar=findViewById(R.id.progressBar1);
        inputPass1.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (inputPass1.getRight() - inputPass1.getCompoundDrawables()[2].getBounds().width())) {
                    if (inputPass1.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        inputPass1.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        inputPass1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_hide_pass, 0);
                    }
                    else {
                        inputPass1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        inputPass1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show_pass, 0);
                    }
                    inputPass1.setSelection(inputPass1.getText().length());
                    return true;
                }
            }
            return false;
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputEmail1.getText().toString().isEmpty()){
                    Toast.makeText(LoginActivity.this,"phải nhập email", Toast.LENGTH_LONG).show();
                    inputEmail1.requestFocus();
                    return;
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(inputEmail1.getText().toString()).matches()){
                    Toast.makeText(LoginActivity.this,"sai định dạng email", Toast.LENGTH_LONG).show();
                    inputEmail1.requestFocus();
                    return;
                }
                if(inputPass1.getText().toString().isEmpty()){
                    Toast.makeText(LoginActivity.this,"phải nhập mật khẩu", Toast.LENGTH_LONG).show();
                    inputPass1.requestFocus();
                    return;
                }
                loading(true);
                FirebaseFirestore database=FirebaseFirestore.getInstance();
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .whereEqualTo(Constants.KEY_EMAIL, inputEmail1.getText().toString())
                        .whereEqualTo(Constants.KEY_PASSWORD, inputPass1.getText().toString())
                        .get()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
                                DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                                preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                                preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                                Intent intent=new Intent(LoginActivity.this, Mess1Activity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else {
                                loading(false);
                                Toast.makeText(LoginActivity.this, "không thể đăng nhập", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
        btnSignUp1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSignUp1=new Intent(LoginActivity.this, SignUpActivity1.class);
                startActivity(intentSignUp1);
            }
        });
        btnGG.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "đăng nhập bằng google", Toast.LENGTH_LONG).show();
        });
        btnFB.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "đăng nhập bằng facebook", Toast.LENGTH_LONG).show();
        });
    }
    private void loading(boolean isLoading){
        if(isLoading){
            btnLogin.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
        else{
            btnLogin.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}