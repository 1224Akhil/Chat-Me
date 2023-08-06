package com.example.chattingapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chattingapp.databinding.ItemConstainerRecentConversionBinding;
import com.example.chattingapp.listners.ConversionListner;
import com.example.chattingapp.models.ChatMessage;
import com.example.chattingapp.models.User;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionHolder>{

    private final List<ChatMessage> chatMessagelist;
    private final ConversionListner conversionListner;


    public RecentConversationsAdapter(List<ChatMessage> chatMessagelist,ConversionListner conversionListner) {
        this.chatMessagelist = chatMessagelist;
        this.conversionListner = conversionListner;
    }

    @NonNull
    @Override
    public ConversionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionHolder(ItemConstainerRecentConversionBinding.inflate
                (LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionHolder holder, int position) {
        holder.setData(chatMessagelist.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessagelist.size();
    }


    class ConversionHolder extends RecyclerView.ViewHolder{

        ItemConstainerRecentConversionBinding binding;

        ConversionHolder (ItemConstainerRecentConversionBinding itemConstainerRecentConversionBinding){
            super(itemConstainerRecentConversionBinding.getRoot());
            binding = itemConstainerRecentConversionBinding;

        }
        void setData(ChatMessage chatMessage){
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v-> {
                User user = new User();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
                conversionListner.onConversionClicked(user);
            });
        }

    }

    private Bitmap getConversionImage(String encodeImage){
        byte[] bytes = Base64.decode(encodeImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }

}
