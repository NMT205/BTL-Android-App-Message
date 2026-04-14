package com.example.appBTL.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appBTL.R;
import com.example.appBTL.utility.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;

public class SignUpActivity2  extends AppCompatActivity {
    EditText inputName, inputPhone, inputEmail, inputPass, inputRePass;
    TextView btnBack2, btnSignUp;
    Spinner spnDay, spnMonth, spnYear;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_signup2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        inputName=findViewById(R.id.inputName);
        inputEmail=findViewById(R.id.inputEmail);
        inputPhone=findViewById(R.id.inputPhone);
        inputPass=findViewById(R.id.inputPass);
        inputRePass=findViewById(R.id.inputRePass);
        btnSignUp=findViewById(R.id.btnSignUp);
        btnBack2=findViewById(R.id.btnBack2);
        spnDay=findViewById(R.id.spnDay);
        spnMonth=findViewById(R.id.spnMonth);
        spnYear=findViewById(R.id.spnYear);
        ArrayList<Integer> listDay = new ArrayList<>();
        for(int i=1;i<32;i++){
            listDay.add(i);
        }
        ArrayAdapter<Integer> spnAdapter1 = new ArrayAdapter<>(
                SignUpActivity2.this, android.R.layout.simple_spinner_item, listDay
        );
        spnAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun", "Jul","Aug","Sep","Oct","Nov","Dec"};
        ArrayList<String> listMounth = new ArrayList<>(Arrays.asList(months));
        ArrayAdapter<String> spnAdapter2 = new ArrayAdapter<>(
                SignUpActivity2.this, android.R.layout.simple_spinner_item, listMounth
        );
        spnAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayList<Integer> listYear = new ArrayList<>();
        for(int i=2026;i>1970;i--){
            listYear.add(i);
        }
        ArrayAdapter<Integer> spnAdapter3 = new ArrayAdapter<>(
                SignUpActivity2.this, android.R.layout.simple_spinner_item, listYear
        );
        spnAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDay.setAdapter(spnAdapter1);
        spnMonth.setAdapter(spnAdapter2);
        spnYear.setAdapter(spnAdapter3);
        btnBack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        inputPass.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (inputPass.getRight() - inputPass.getCompoundDrawables()[2].getBounds().width())) {
                    if (inputPass.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        inputPass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        inputPass.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_hide_pass, 0);
                    }
                    else {
                        inputPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        inputPass.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show_pass, 0);
                    }
                    inputPass.setSelection(inputPass.getText().length());
                    return true;
                }
            }
            return false;
        });
        inputRePass.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (inputRePass.getRight() - inputRePass.getCompoundDrawables()[2].getBounds().width())) {
                    if (inputRePass.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        inputRePass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        inputRePass.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_hide_pass, 0);
                    }
                    else {
                        inputRePass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        inputRePass.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show_pass, 0);
                    }
                    inputRePass.setSelection(inputRePass.getText().length());
                    return true;
                }
            }
            return false;
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputName.getText().toString().isEmpty()){
                    Toast.makeText(SignUpActivity2.this, "Phải nhập tên", Toast.LENGTH_LONG).show();
                    inputName.requestFocus();
                    return;
                }
                String name=inputName.getText().toString();
                if(inputPhone.getText().toString().isEmpty()) {
                    Toast.makeText(SignUpActivity2.this, "phải nhập số điện thoại", Toast.LENGTH_LONG).show();
                    inputPhone.requestFocus();
                    return;
                }
                if(inputPhone.getText().toString().length()!=10) {
                    Toast.makeText(SignUpActivity2.this, "số điện thoại phải có 10 số", Toast.LENGTH_LONG).show();
                    inputPhone.requestFocus();
                    return;
                }
                String phone=inputPhone.getText().toString();
                if(inputEmail.getText().toString().isEmpty()) {
                    Toast.makeText(SignUpActivity2.this, "phải nhập email", Toast.LENGTH_LONG).show();
                    inputEmail.requestFocus();
                    return;
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()){
                    Toast.makeText(SignUpActivity2.this, "sai định dạng email", Toast.LENGTH_LONG).show();
                    inputEmail.requestFocus();
                    return;
                }
                String email=inputEmail.getText().toString().toLowerCase();
                if(inputPass.getText().toString().isEmpty()) {
                    Toast.makeText(SignUpActivity2.this, "phải nhập mật khẩu", Toast.LENGTH_LONG).show();
                    inputPass.requestFocus();
                    return;
                }
                if(inputPass.getText().toString().length()<8) {
                    Toast.makeText(SignUpActivity2.this, "mật khẩu có ít nhất 8 ký tự", Toast.LENGTH_LONG).show();
                    inputPass.requestFocus();
                    return;
                }
                String pass=inputPass.getText().toString();
                if(!inputRePass.getText().toString().equals(inputPass.getText().toString())) {
                    Toast.makeText(SignUpActivity2.this, "phải giống mật khẩu đã nhập", Toast.LENGTH_LONG).show();
                    inputRePass.requestFocus();
                    return;
                }
                int year = Integer.parseInt(spnYear.getSelectedItem().toString());
                if(2026-year<18){
                    Toast.makeText(SignUpActivity2.this, "phải 18 tuổi trở lên mới được đăng ký", Toast.LENGTH_LONG).show();
                    return;
                }
                checkUserExists(name, pass, phone, email);
            }
        });
    }
    private void checkUserExists(String name, String pass, String phone, String email) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        btnSignUp.setEnabled(false);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()){
                        Toast.makeText(SignUpActivity2.this, "email đã tồn tại", Toast.LENGTH_LONG).show();
                        inputEmail.requestFocus();
                        btnSignUp.setEnabled(true);

                    }
                    else{
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .whereEqualTo(Constants.KEY_PHONE, phone)
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if(task2.isSuccessful() && task2.getResult() != null && !task2.getResult().isEmpty()){
                                        Toast.makeText(SignUpActivity2.this, "số điện thoại đã tồn tại", Toast.LENGTH_LONG).show();
                                        inputPhone.requestFocus();
                                        btnSignUp.setEnabled(true);
                                    }
                                    else {
                                        String dateOfBirth = spnDay.getSelectedItem().toString()+"/"+ spnMonth.getSelectedItem().toString()+"/"+spnYear.getSelectedItem().toString();
                                        Intent avaIntent = new Intent(SignUpActivity2.this, AvatarActivity.class);
                                        avaIntent.putExtra("name", name);
                                        avaIntent.putExtra("phone", phone);
                                        avaIntent.putExtra("email", email);
                                        avaIntent.putExtra("password", pass);
                                        avaIntent.putExtra("date", dateOfBirth);
                                        btnSignUp.setEnabled(true);
                                        startActivity(avaIntent);
                                    }
                                });
                    }
                });
    }
}
