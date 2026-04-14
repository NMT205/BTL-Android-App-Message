package com.example.appBTL.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appBTL.databinding.ItemContainerReceivedMessageBinding;
import com.example.appBTL.databinding.ItemContainerSentMessageBinding;
import com.example.appBTL.model.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessages;
    private final Bitmap receiverProfileImg;
    private String senderId;

    public static int VIEW_TYPE_SENT=1;
    public static int VIEW_TYPE_RECEIVED=2;
    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImg, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImg = receiverProfileImg;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),parent,false
                    )
            );
        }
        else {
            return new ReceivedMessageHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),parent,false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position)==VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        }
        else {
            ((ReceivedMessageHolder) holder).setData(chatMessages.get(position), receiverProfileImg);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }
        else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;
        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding=itemContainerSentMessageBinding;
        }
        void setData(ChatMessage chatMessage){
            if(chatMessage.image != null){
                binding.imgMessage.setVisibility(View.VISIBLE);
                binding.txtMessage.setVisibility(View.GONE);
                binding.imgMessage.setImageBitmap(getBitmap(chatMessage.image));
            }
            else {
                binding.imgMessage.setVisibility(View.GONE);
                binding.txtMessage.setVisibility(View.VISIBLE);
                binding.txtMessage.setText(chatMessage.message);
            }
            binding.txtDateTime.setText(chatMessage.dateTime);
        }
        private Bitmap getBitmap(String encodedImage){
            byte[] bytes = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);
            return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }
    static class ReceivedMessageHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceivedMessageBinding binding;
        ReceivedMessageHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding){
            super(itemContainerReceivedMessageBinding.getRoot());
            binding=itemContainerReceivedMessageBinding;
        }
        void setData(ChatMessage chatMessage, Bitmap receiverProfileImg){
            if(chatMessage.image != null){
                binding.imgMessage1.setVisibility(View.VISIBLE);
                binding.txtMessage1.setVisibility(View.GONE);
                binding.imgMessage1.setImageBitmap(getBitmap(chatMessage.image));
            }
            else {
                binding.imgMessage1.setVisibility(View.GONE);
                binding.txtMessage1.setVisibility(View.VISIBLE);
                binding.txtMessage1.setText(chatMessage.message);
            }
            binding.txtDateTime1.setText(chatMessage.dateTime);
            binding.imgProfile2.setImageBitmap(receiverProfileImg);
        }
        private Bitmap getBitmap(String encodedImage){
            byte[] bytes = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);
            return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }
}
