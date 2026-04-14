package com.example.appBTL.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appBTL.R;
import com.example.appBTL.adapter.ChatAdapter;
import com.example.appBTL.model.ChatMessage;
import com.example.appBTL.model.User;
import com.example.appBTL.utility.Constants;
import com.example.appBTL.utility.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatActivity extends BaseActivity {
    TextView txtName;
    RecyclerView chatView;
    EditText inputMes;
    AppCompatImageView btnSend, btnSendImg;
    ProgressBar progressBar;
    AppCompatImageView imgBack, imgInfo;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Boolean isReceiverAvailable=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        txtName=findViewById(R.id.txtNameUser2);
        imgBack=findViewById(R.id.imgBack1);
        chatView=findViewById(R.id.chatRecyclerView);
        inputMes=findViewById(R.id.inputMes);
        btnSend=findViewById(R.id.btnSend);
        btnSendImg=findViewById(R.id.btnSendImg);
        progressBar=findViewById(R.id.progressBar3);
        imgInfo=findViewById(R.id.imgInfo);
        loadReceiverDetail();
        init();
        listenMessages();
        imgInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.putExtra(Constants.KEY_USER, receiverUser);
                startActivity(intent);
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isConnected()){
                    Toast.makeText(ChatActivity.this, "không có internet", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(inputMes.getText().toString().trim().isEmpty()){
                    return;
                }
                HashMap<String, Object> message = new HashMap<>();
                message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID)); message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                message.put(Constants.KEY_MESSAGE, inputMes.getText().toString());
                message.put(Constants.KEY_TIMESTAMP, new Date());
                database.collection (Constants.KEY_COLLECTION_CHAT).add(message);
                if(conversionId!=null){
                    updateConversion(inputMes.getText().toString());
                }
                else {
                    HashMap<String, Object> conversion=new HashMap<>();
                    conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                    conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                    conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                    conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                    conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                    conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
                    conversion.put(Constants.KEY_LAST_MESSAGE, inputMes.getText().toString());
                    conversion.put(Constants.KEY_TIMESTAMP, new Date());
                    addConversion(conversion);
                }
                inputMes.setText(null);
            }
        });
        btnSendImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isConnected()){
                    Toast.makeText(ChatActivity.this, "không có internet", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
    }
    private Bitmap getBitmap(String encodedImg){
        byte[] bytes= Base64.decode(encodedImg, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    private void loadReceiverDetail(){
        receiverUser=(User) getIntent().getSerializableExtra(Constants.KEY_USER);
        txtName.setText(receiverUser.name);
    }
    private void init(){
        preferenceManager=new PreferenceManager(getApplicationContext());
        chatMessages=new ArrayList<>();
        chatAdapter=new ChatAdapter(chatMessages,
                getBitmap(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID));
        chatView.setAdapter(chatAdapter);
        database=FirebaseFirestore.getInstance();
    }
    private String getReadableDateTime (Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault()).format(date);
    }
    private final EventListener<QuerySnapshot> eventListener=(value, error) -> {
        if(error!=null){
            return;
        }
        if(value!=null){
            int count=chatMessages.size();
            for(DocumentChange documentChange:value.getDocumentChanges()){
                if(documentChange.getType()==DocumentChange.Type.ADDED){
                    ChatMessage chatMessage=new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.image = documentChange.getDocument().getString(Constants.KEY_IMAGE);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count==0) {
                chatAdapter.notifyDataSetChanged();
            }
            else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                chatView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            chatView.setVisibility(View.VISIBLE);
        }
        progressBar.setVisibility(View.GONE);
        if(conversionId==null){
            checkConversion();
        }
    };
    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    private void checkConversion(){
        checkForConversionRemotely(
                preferenceManager.getString(Constants.KEY_USER_ID),
                receiverUser.id
        );
        checkForConversionRemotely(
                receiverUser.id,
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
    }
    private void checkForConversionRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get().addOnCompleteListener(conversionCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot> conversionCompleteListener=task -> {
        if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
            conversionId=documentSnapshot.getId();
        }
    };
    private void addConversion(HashMap<String, Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId=documentReference.getId());
    }
    private void updateConversion(String message){
        DocumentReference documentReference=database.collection(Constants.KEY_COLLECTION_CONVERSATION).document(conversionId);
        documentReference.update(
            Constants.KEY_LAST_MESSAGE, message,
            Constants.KEY_TIMESTAMP, new Date()
        );
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                String encodedImage = encodeImage(bitmap);
                sendImageMessage(encodedImage);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    private void sendImageMessage(String encodedImage) {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_IMAGE, encodedImage);
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversionId != null){
            updateConversion("đã gửi 1 ảnh");
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, "đã gửi 1 ảnh");

            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
    }
    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
    private void listenAvailability(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.id)
                .addSnapshotListener(ChatActivity.this, (value, error)->{
                    if(error!=null) return;
                    if(value!=null){
                        if(value.getLong(Constants.KEY_AVAILABILITY)!=null){
                            int availability= Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY))
                                    .intValue();
                            isReceiverAvailable=availability==1;
                        }
                        receiverUser.token=value.getString(Constants.KEY_FCM_TOKEN);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailability();
    }
}