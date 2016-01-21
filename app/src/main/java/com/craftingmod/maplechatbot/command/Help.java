package com.craftingmod.maplechatbot.command;

import android.support.annotation.Nullable;

import com.craftingmod.maplechatbot.chat.ISender;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.UserModel;

import java.util.ArrayList;

/**
 * Created by superuser on 16/1/21.
 */
public class Help extends BaseCommand {
    public Help(ISender sender) {
        super(sender);
    }
    protected String[] filter(){
        return new String[]{"help"};
    }
    @Override
    protected void onCommand(ChatModel chat, UserModel user, String cmdName, @Nullable ArrayList<String> args) {
        StringBuilder str = new StringBuilder();
        str.append("도움말    ");
        str.append("mail <유저> <메시지> - 해당 유저에게 메시지를 보냅니다.");
        str.append("     ");
        str.append("seen <유저> - 해당 유저가 마지막으로 말 한 시간을 출력합니다.");
        str.append("     ");
        str.append("coin <내용> - 주어진 내용에 따라 동전을 뒤집습니다. 매일 결과가 바뀝니다.");
        str.append("     ");
        str.append("math <수식> - 수학식을 계산하여 출력합니다.");

        this.sendMessage(str.toString(),user.accountID);
    }
}
