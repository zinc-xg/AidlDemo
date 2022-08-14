package com.example.aidldemo.entity;

import android.os.Parcel;

public class Message implements android.os.Parcelable {

    private String content;
    private boolean isSendSuccess;

    public Message() {};

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSendSuccess() {
        return isSendSuccess;
    }

    public void setSendSuccess(boolean sendSuccess) {
        isSendSuccess = sendSuccess;
    }

    protected Message(Parcel in) {
        content = in.readString();
        isSendSuccess = in.readByte() != 0;
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(content);
        parcel.writeByte((byte) (isSendSuccess ? 1 : 0));
    }

    public void readFromParcel(Parcel parcel){
        // 如果我只想“引用传递一部分”，比如不想把content的改变传回来，那么我就在这里不read content的值
        // 这个想法不行。。。试了一下，会导致isSendSuccess的改变也传不回去
        content = parcel.readString();
        isSendSuccess = parcel.readByte()==1;
    }
}
