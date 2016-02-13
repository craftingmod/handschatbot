package com.craftingmod.maplebot.light;

import android.content.Context;
import android.os.Looper;

import com.craftingmod.maplebot.model.ChatModel;

/**
 * Created by superuser on 16/2/13.
 */
public interface IService {
    Context getContext();
    void sendFMessage(ChatModel model,String text);
    void sendGMessage(String text);
    Looper getLooper();
}
