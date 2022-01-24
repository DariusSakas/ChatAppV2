package com.example.chatapp;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesViewHolder> {

    private static final int TYPE_MY_MESSAGE = 0;
    private static final int TYPE_OTHER_MESSAGE = 1;

    private Context context;

    private List<Message> messageList;

    public MessagesAdapter(Context context) {
        messageList = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_MY_MESSAGE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_my_message, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_my_message, parent, false);
        }
        return new MessagesViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        String author = message.getAuthor();
        if (author != null && author.equals(PreferenceManager.getDefaultSharedPreferences(context).getString("author", "Anononymous"))) {
            return TYPE_MY_MESSAGE;
        } else {
            return TYPE_OTHER_MESSAGE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MessagesViewHolder holder, int position) {
        holder.getTextViewAuthor().setText(messageList.get(position).getAuthor());
        String urlToImage = messageList.get(position).getImageUrl();
        String messageText = messageList.get(position).getMessageText();

        if (urlToImage == null || urlToImage.isEmpty()) {
            holder.getImageViewImage().setVisibility(View.GONE);
        } else {
            holder.getImageViewImage().setVisibility(View.VISIBLE);
        }
//
//        if (messageText == null || messageText.isEmpty()) {
//            holder.getTextViewMessageText().setVisibility(View.GONE);
//        } else {
//            holder.getTextViewMessageText().setVisibility(View.VISIBLE);
//        }

        if (messageText != null && !messageText.isEmpty()) {
            holder.getTextViewMessageText().setVisibility(View.VISIBLE);
            holder.getTextViewMessageText().setText(messageList.get(position).getMessageText());
        }else {
            holder.getTextViewMessageText().setVisibility(View.GONE);
        }

        if (urlToImage != null && !urlToImage.isEmpty()) {

            Picasso.get().load(messageList.get(position).getImageUrl()).into(holder.getImageViewImage());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
        notifyDataSetChanged();
    }
}
