package com.quangtruong.appbanlinhkien.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.quangtruong.appbanlinhkien.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<Uri> imageUris;
    private Context context;
    private ImageRemoveListener imageRemoveListener;

    public interface ImageRemoveListener {
        void onImageRemove(int position);
    }

    public ImageAdapter(List<Uri> imageUris, Context context, ImageRemoveListener listener) {
        this.imageUris = imageUris;
        this.context = context;
        this.imageRemoveListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        Glide.with(context)
                .load(imageUri)
                .into(holder.imageView);

        holder.removeButton.setOnClickListener(v -> {
            if (imageRemoveListener != null) {
                imageRemoveListener.onImageRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton removeButton;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.product_image);
            removeButton = itemView.findViewById(R.id.remove_image_button);
        }
    }
}