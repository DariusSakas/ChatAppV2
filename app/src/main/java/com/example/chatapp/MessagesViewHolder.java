package com.example.chatapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

public class MessagesViewHolder  extends RecyclerView.ViewHolder {

    private TextView textViewAuthor;
    private TextView textViewMessageText;
    private ImageView imageViewImage;

    public MessagesViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        textViewAuthor = itemView.findViewById(R.id.textViewAuthor);
        textViewMessageText = itemView.findViewById(R.id.textViewMessageText);
        imageViewImage = itemView.findViewById(R.id.imageViewImage);
    }

    public TextView getTextViewAuthor() {
        return textViewAuthor;
    }

    public TextView getTextViewMessageText() {
        return textViewMessageText;
    }

    public ImageView getImageViewImage() {
        return imageViewImage;
    }
}
