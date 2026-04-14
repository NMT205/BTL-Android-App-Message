package com.example.appBTL.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appBTL.R;
import com.example.appBTL.adapter.UserAdapter;
import com.example.appBTL.listener.UserListener;
import com.example.appBTL.model.User;
import com.example.appBTL.utility.Constants;
import com.example.appBTL.utility.PreferenceManager;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserActivity extends BaseActivity implements UserListener {
    ProgressBar progressBar;
    RecyclerView userRecyclerView;
    TextView errorMess;
    MaterialCardView btnBack;
    EditText inputSearch;
    private List<User> userList = new ArrayList<>();
    private List<User> userListFull = new ArrayList<>();
    private UserAdapter userAdapter;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);
        preferenceManager = new PreferenceManager(getApplicationContext());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        progressBar = findViewById(R.id.progressBar2);
        inputSearch = findViewById(R.id.inputSearch);
        errorMess = findViewById(R.id.txtMessError);
        userRecyclerView = findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnBack = findViewById(R.id.btnBack3);
        getUser();
        loadAllUsers();
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUser() {
        loading(true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        db.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, currentUserId)
                .get()
                .addOnCompleteListener(task1 -> {

                    db.collection(Constants.KEY_COLLECTION_CONVERSATION)
                            .whereEqualTo(Constants.KEY_RECEIVER_ID, currentUserId)
                            .get()
                            .addOnCompleteListener(task2 -> {
                                if (task1.isSuccessful() && task2.isSuccessful()) {
                                    Set<String> userIds = new HashSet<>();
                                    for (QueryDocumentSnapshot documentSnapshot : task1.getResult()) {
                                        userIds.add(documentSnapshot.getString(Constants.KEY_RECEIVER_ID));
                                    }
                                    for (QueryDocumentSnapshot documentSnapshot : task2.getResult()) {
                                        userIds.add(documentSnapshot.getString(Constants.KEY_SENDER_ID));
                                    }
                                    loadUsers(userIds);
                                }
                                else {
                                    loading(false);
                                    showError();
                                }
                            });
                });
    }

    private void loadUsers(Set<String> userIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if (userIds.contains(documentSnapshot.getId())) {
                                User user = new User();
                                user.id=documentSnapshot.getId();
                                user.name=documentSnapshot.getString(Constants.KEY_NAME);
                                user.email=documentSnapshot.getString(Constants.KEY_EMAIL);
                                user.image=documentSnapshot.getString(Constants.KEY_IMAGE);
                                user.phone = documentSnapshot.getString(Constants.KEY_PHONE);
                                user.date = documentSnapshot.getString(Constants.KEY_DATE);
                                user.token=documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                users.add(user);
                            }
                        }
                        userList = users;
                        if(userList.size() > 0){
                            userAdapter = new UserAdapter(userList, UserActivity.this);
                        }
                        else {
                            userAdapter = new UserAdapter(userListFull, UserActivity.this);
                        }
                        userRecyclerView.setAdapter(userAdapter);
                        userRecyclerView.setVisibility(View.VISIBLE);
                    }
                    else {
                        showError();
                    }
                });
    }
    private void setupSearch() {
        inputSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filter(String text){
        if(userAdapter == null) return;

        List<User> filtered = new ArrayList<>();
        if(text.isEmpty()){
            if(userList.isEmpty()){
                userAdapter.updateList(userListFull);
            } else {
                userAdapter.updateList(userList);
            }
            return;
        }
        for(User user : userListFull){
            String name = user.name != null ? user.name : "";
            String email = user.email != null ? user.email : "";
            if(name.toLowerCase().contains(text.toLowerCase())||email.toLowerCase().contains(text.toLowerCase())){
                filtered.add(user);
            }
        }

        userAdapter.updateList(filtered);
    }
    private void showError() {
        errorMess.setText("không có người dùng");
        errorMess.setVisibility(View.VISIBLE);
    }
    private void loading(Boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(UserActivity.this, ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
    private void loadAllUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        db.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null){
                        userListFull.clear();
                        for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            if(documentSnapshot.getId().equals(currentUserId)) continue;
                            User user=new User();
                            user.id=documentSnapshot.getId();
                            user.name=documentSnapshot.getString(Constants.KEY_NAME);
                            user.email=documentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image=documentSnapshot.getString(Constants.KEY_IMAGE);
                            user.phone = documentSnapshot.getString(Constants.KEY_PHONE);
                            user.date = documentSnapshot.getString(Constants.KEY_DATE);
                            user.token=documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            userListFull.add(user);
                        }
                        setupSearch();
                    }
                });
    }
}