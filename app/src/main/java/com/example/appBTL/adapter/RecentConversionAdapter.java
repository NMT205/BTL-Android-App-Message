package com.example.appBTL.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appBTL.databinding.ItemContainerRecentBinding;
import com.example.appBTL.listener.ConversionListener;
import com.example.appBTL.model.ChatMessage;
import com.example.appBTL.model.User;
import com.example.appBTL.utility.Constants;
import com.example.appBTL.utility.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RecentConversionAdapter extends RecyclerView.Adapter<RecentConversionAdapter.ConversionViewHolder>{
    private List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;

    public RecentConversionAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener=conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(ItemContainerRecentBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentBinding binding;
        ConversionViewHolder(ItemContainerRecentBinding itemContainerRecentBinding){
            super(itemContainerRecentBinding.getRoot());
            binding=itemContainerRecentBinding;
        }
        void setData(ChatMessage chatMessage){
                binding.imgProfile3.setImageBitmap(getConversionImg(chatMessage.conversionImg));
                binding.txtNameUser2.setText(chatMessage.conversionName);
                binding.txtRecentMess.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection(Constants.KEY_COLLECTION_USERS)
                        .document(chatMessage.conversionId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if(documentSnapshot != null){
                                User user = new User();
                                user.id = documentSnapshot.getId();
                                user.name = documentSnapshot.getString(Constants.KEY_NAME);
                                user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
                                user.phone = documentSnapshot.getString(Constants.KEY_PHONE);
                                user.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                                user.date = documentSnapshot.getString(Constants.KEY_DATE);
                                conversionListener.onConversionClicked(user);
                            }
                        });
            });
        }
    }
    private Bitmap getConversionImg(String img){
        byte[] bytes= Base64.decode(img, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
