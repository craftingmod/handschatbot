package com.craftingmod.maplechatbot.command;

import android.content.Context;
import android.support.annotation.Nullable;

import com.craftingmod.maplechatbot.chat.ChatService;
import com.craftingmod.maplechatbot.model.UserModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimerTask;

/**
 * Created by superuser on 15/12/13.
 */
public class oClock extends TimerTask {
        private int last_hour = 13;
        private Context context;

        public oClock(Context cx){
            last_hour = 13;
            context = cx;
        }
        @Override
        public void run() {
            Calendar c = Calendar.getInstance();
            if(c.get(Calendar.MINUTE) == 0){
                if(last_hour != c.get(Calendar.HOUR)){
                    BaseCommand.sendMessage(context,c.get(Calendar.HOUR) + "시에요");
                    last_hour = c.get(Calendar.HOUR);
                }
            }
        }
}
