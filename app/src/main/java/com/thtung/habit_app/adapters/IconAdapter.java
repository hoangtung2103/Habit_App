package com.thtung.habit_app.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {

    private final Context context;
    private final String[] icons;
    private final OnIconClickListener listener;

    public interface OnIconClickListener {
        void onIconClick(String assetPath);
    }

    public IconAdapter(Context context, String[] icons, OnIconClickListener listener) {
        this.context = context;
        this.icons = icons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(150, 150));
        imageView.setPadding(8, 8, 8, 8);
        return new IconViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        String assetPath = "icons/" + icons[position];
        try {
            InputStream is = context.getAssets().open(assetPath);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            holder.imageView.setImageBitmap(bitmap);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        holder.imageView.setOnClickListener(v -> listener.onIconClick(assetPath));
    }

    @Override
    public int getItemCount() {
        return icons.length;
    }

    static class IconViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public IconViewHolder(@NonNull ImageView itemView) {
            super(itemView);
            imageView = itemView;
        }
    }
}

