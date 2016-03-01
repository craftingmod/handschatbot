package com.craftingmod.maplebot.heavy.modules;

import android.support.annotation.Nullable;
import android.util.Log;

import com.craftingmod.maplebot.MapleUtil;
import com.craftingmod.maplebot.heavy.IServiceHeavy;
import com.craftingmod.maplebot.model.CModel;
import com.craftingmod.maplebot.model.FriendModel;
import com.craftingmod.maplebot.model.SimpleUserModel;
import com.craftingmod.maplebot.model.UserModel;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.ArrayList;

/**
 * Created by superuser on 16/2/15.
 */
public class UInfo extends HeavyModule {
    public UInfo(IServiceHeavy mInterface) {
        super(mInterface);
    }

    @Override
    protected void handleCommand(CModel chat, ArrayList<String> splits) {
        if(splits.size() >= 2 && splits.get(1).length() >= 2){
            requestWithUser(chat,splits.get(1));
        }
    }

    @Override
    protected void handleCommand(CModel chat, ArrayList<String> splits, UserModel say, @Nullable UserModel target) {
        Log.d("Hi",g.toJson(splits));
        if(target != null){
            if(splits.get(0).equalsIgnoreCase("!user")){
                ArrayList<SimpleUserModel> characters = MapleUtil.getCharacterList(target.accountID,chat.worldID);
                ArrayList<String> out = new ArrayList<>();
                for(SimpleUserModel character : characters){
                    out.add(character.userName);
                }
                String join;
                if(out.size() >= 1){
                    join = Joiner.on(" ").join(out);
                }else{
                    join = "없음 ㅋ";
                }
                String text = target.userName + " : " +
                        "accountID " + target.accountID + " characterID " + target.characterID +
                        " 같은 섭 부캐: " + join;
                sendMessage(chat,text);
            }else if(splits.get(0).equalsIgnoreCase("!friend")){
                ArrayList<FriendModel> friends = MapleUtil.getAccountFriendList(target.accountID);
                ArrayList<String> out = new ArrayList<>();
                int i = 0;
                String index = "1";
                for(i=0;i<friends.size();i+=1){
                    if(friends.get(i).worldID != chat.worldID){
                        friends.remove(i);
                        i -= 1;
                    }
                }
                i = 0;
                if(splits.size() >= 3 && Ints.tryParse(splits.get(2)) != null){
                    i = Ints.tryParse(splits.get(2));
                    index = (i+1) + "";
                }
                int end;
                for(end=i+10;i<end&&i<friends.size();i+=1){
                    if(friends.get(i).worldID == chat.worldID){
                        out.add(friends.get(i).nickname);
                    }
                }
                String join;
                if(out.size() >= 1){
                    join = Joiner.on(" ").skipNulls().join(out);
                }else{
                    join = "없음 ㅋ";
                }
                String text = target.userName + "의 친구들(" +index+ "번째부터): " + join;
                sendMessage(chat,text);
            }else if(splits.get(0).equalsIgnoreCase("!image")){
                UserModel profile = MapleUtil.getCharacterInfo(target.worldID,target.characterID);
                if(profile.userImage != null){
                    sendMessage(chat,profile.userImage);
                }
            }
        }else{
            sendMessage(chat,"알수 없는 유저 입니다.");
        }
    }

    @Override
    protected ArrayList<String> filter() {
        return Lists.newArrayList(new String[]{"user","friend","image"});
    }

    @Override
    protected String[] help() {
        return new String[]{
                "!user <캐릭터이름> : 같은 섭에 있는 캐릭터들을 출력합니다.",
                "!friend <캐릭터이름> [시작점] : 캐릭터의 친구 목록을 출력합니다. 시작점으로부터 10개만 보여줍니다.",
                "!image <캐릭터이름> : 캐릭터사진을 출력합니다."
        };
    }

    @Override
    protected String name() {
        return "UserInfo";
    }
}
