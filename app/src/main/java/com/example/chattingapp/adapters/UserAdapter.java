package com.example.chattingapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chattingapp.databinding.ItemConstainerUserBinding;
import com.example.chattingapp.listners.UserListner;
import com.example.chattingapp.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private final List<User> users;
    private final UserListner userListner;

    public UserAdapter(List<User> users,UserListner userListner) {
        this.users = users;
        this.userListner = userListner;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemConstainerUserBinding itemConstainerUserBinding = ItemConstainerUserBinding.inflate(LayoutInflater.from(parent.getContext())
                ,parent,false);
        return new UserViewHolder(itemConstainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    class UserViewHolder extends RecyclerView.ViewHolder{
        ItemConstainerUserBinding binding;

        UserViewHolder(ItemConstainerUserBinding itemConstainerUserBinding){
            super(itemConstainerUserBinding.getRoot());
            binding = itemConstainerUserBinding;
        }
        void setUserData(User user){
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v-> userListner.onUserClicked(user));
        }
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
}
