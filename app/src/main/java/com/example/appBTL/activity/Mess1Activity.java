package com.example.appBTL.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appBTL.R;
import com.example.appBTL.adapter.RecentConversionAdapter;
import com.example.appBTL.listener.ConversionListener;
import com.example.appBTL.model.ChatMessage;
import com.example.appBTL.model.User;
import com.example.appBTL.receiver.NetworkReceiver;
import com.example.appBTL.utility.Constants;
import com.example.appBTL.utility.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Mess1Activity extends BaseActivity implements ConversionListener {
    TextView txtNameUser;
    CardView btnProfile, btnLogOut;
    ImageView imgProfile;
    RecyclerView recyclerView;
    FloatingActionButton btnNewChat;
    ProgressBar progressBar;
    EditText inputSearch;
    NetworkReceiver networkReceiver;
    private List<ChatMessage> conversationFull;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversation;
    private RecentConversionAdapter conversionAdapter;
    private FirebaseFirestore database;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_mess1);
        preferenceManager=new PreferenceManager(getApplicationContext());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtNameUser=findViewById(R.id.txtNameUser);
        inputSearch=findViewById(R.id.inputSearchMess);
        btnProfile=findViewById(R.id.btnProfile);
        btnLogOut=findViewById(R.id.imgLogOut);
        imgProfile=findViewById(R.id.imgProfile);
        btnNewChat=findViewById(R.id.btnNewChat);
        progressBar=findViewById(R.id.progressBar4);
        recyclerView=findViewById(R.id.conversationRecyclerView);
        init();
        loadUserDetail();
        setupSearch();
        getToken();
        listenConversations();
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.app.AlertDialog.Builder(Mess1Activity.this)
                        .setTitle("Xác nhận")
                        .setMessage("Bạn có muốn đăng xuất không?")
                        .setCancelable(false)
                        .setPositiveButton("Có", (dialog, which) -> {
                            Toast.makeText(Mess1Activity.this,"Logout Success",Toast.LENGTH_LONG).show();
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            DocumentReference documentReference =
                                    database.collection(Constants.KEY_COLLECTION_USERS)
                                            .document(preferenceManager.getString(Constants.KEY_USER_ID));
                            HashMap<String, Object> update = new HashMap<>();
                            update.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
                            documentReference.update(update)
                                    .addOnSuccessListener(unused -> {
                                        preferenceManager.clear();
                                        Intent intent = new Intent(Mess1Activity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Mess1Activity.this,"fail",Toast.LENGTH_LONG).show();
                                    });
                        })
                        .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
        btnNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newChatIntent=new Intent(Mess1Activity.this, UserActivity.class);
                startActivity(newChatIntent);
            }
        });
    }

    private void init(){
        conversation=new ArrayList<>();
        conversationFull = new ArrayList<>();
        conversionAdapter=new RecentConversionAdapter(conversation, this);
        recyclerView.setAdapter(conversionAdapter);
        database=FirebaseFirestore.getInstance();
    }

    private void loadUserDetail(){
        txtNameUser.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes= Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imgProfile.setImageBitmap(bitmap);
    }
    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }
    private void updateToken(String token){
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e->{
                    Toast.makeText(Mess1Activity.this,"fail",Toast.LENGTH_LONG).show();
                });
    }
    private final EventListener<QuerySnapshot> eventListener=(value, error)->{
        if(error!=null){
            return;
        }
        if(value!=null){
            for(DocumentChange documentChange: value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversionImg = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    } else {
                        chatMessage.conversionImg = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject=documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversation.add(chatMessage);
                    conversationFull.add(chatMessage);
                }
                else if(documentChange.getType()==DocumentChange.Type.MODIFIED){
                    for(int i=0;i<conversation.size();i++){
                        String senderId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if(conversation.get(i).senderId.equals(senderId) && conversation.get(i).receiverId.equals(receiverId)){
                            conversation.get(i).message=documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversation.get(i).dateObject=documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversation, (obj1, obj2)->obj2.dateObject.compareTo(obj1.dateObject));
            conversionAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(0);
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    };
    private void listenConversations() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID)).addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID)).addSnapshotListener(eventListener);
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intentChat=new Intent(Mess1Activity.this, ChatActivity.class);
        intentChat.putExtra(Constants.KEY_USER, user);
        startActivity(intentChat);
    }

    private void setupSearch(){
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
        List<ChatMessage> filtered = new ArrayList<>();
        if(text.isEmpty()){
            conversation.clear();
            conversation.addAll(conversationFull);
            conversionAdapter.notifyDataSetChanged();
            return;
        }
        for(ChatMessage chat : conversationFull){
            String name = chat.conversionName != null ? chat.conversionName : "";
            if(name.toLowerCase().contains(text.toLowerCase())){
                filtered.add(chat);
            }
        }
        conversation.clear();
        conversation.addAll(filtered);
        conversionAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkReceiver = new NetworkReceiver();
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }
}
