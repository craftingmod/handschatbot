package com.craftingmod.maplechatbot.chat;

import android.content.Context;

/**
 * Created by superuser on 16/1/21.
 */
public interface ISender {
    Context getContext();
    void sendMessage(String msg,int sender);
    void sendMessageAll(String msg);
}
