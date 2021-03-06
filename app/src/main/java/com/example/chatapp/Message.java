package com.example.chatapp;

public class Message {
    private String author;
    private String messageText;
    private long date;
    private String imageUrl;

    public Message() {
    }

    public Message(String author, String messageText, long date, String imageUrl) {
        this.author = author;
        this.messageText = messageText;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
