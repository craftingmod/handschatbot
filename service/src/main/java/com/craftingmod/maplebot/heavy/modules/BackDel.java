package com.craftingmod.maplebot.heavy.modules;

import android.support.annotation.Nullable;
import android.util.Log;

import com.craftingmod.maplebot.MapleUtil;
import com.craftingmod.maplebot.heavy.IServiceHeavy;
import com.craftingmod.maplebot.heavy.core.SQLFinder;
import com.craftingmod.maplebot.model.CModel;
import com.craftingmod.maplebot.model.FriendModel;
import com.craftingmod.maplebot.model.SimpleUserModel;
import com.craftingmod.maplebot.model.UserModel;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import java.util.ArrayList;

/**
 * Created by superuser on 16/2/16.
 */
public class BackDel extends HeavyModule {
    public BackDel(IServiceHeavy mInterface) {
        super(mInterface);
    }

    @Override
    protected void handleCommand(CModel chat, ArrayList<String> splits) {
        if(splits.size() == 2){
            requestWithUser(chat,splits.get(1));
        }
    }

    @Override
    protected void handleCommand(CModel chat, ArrayList<String> splits, UserModel say, @Nullable UserModel target) {
        if(target == null){
            sendMessage(chat,"유저 찾기 불가능!");
        }else{
            try{
                final ArrayList<String> sakList = new ArrayList<>();
                final int aID = target.accountID;
                ArrayList<FriendModel> fModel = MapleUtil.getAccountFriendList(aID);
                for(int i=0;i<fModel.size();i+=1){
                    Boolean sak = true;
                    ArrayList<FriendModel> sideM = MapleUtil.getAccountFriendList(fModel.get(i).accountID);
                    for(int j=0;j<sideM.size();j+=1){
                        if(sideM.get(j).accountID == aID){
                            sak = false;
                            break;
                        }
                    }
                    if (sak) {
                        if(fModel.get(i) != null){
                            if(fModel.get(i).nickname == null){
                                if(fModel.get(i).charName != null){
                                    sakList.add(fModel.get(i).charName);
                                }else{
                                    ArrayList<SimpleUserModel> m = MapleUtil.getCharacterList(fModel.get(i).accountID,chat.worldID);
                                    if(m.size() >= 1){
                                        for(SimpleUserModel um : m){
                                            if(um.worldID == chat.worldID){
                                                sakList.add(um.userName);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }else{
                                sakList.add(fModel.get(i).nickname);
                            }
                        }
                    }
                }
                String out = Joiner.on(",").skipNulls().join(sakList);
                if(sakList.size() >= 1 && out.length() >= 2){
                    sendMessage(chat,say.userName + "님은 " + out + "한테 뒷삭을 당했습니다. ㅜㅜ");
                }else{
                    sendMessage(chat,say.userName + "님은 친구창이 깔끔합니다 :)");
                }
            }catch (Exception e){
                sendMessage(chat, "내부 에러 :p");
            }
        }
    }

    @Override
    protected ArrayList<String> filter() {
        return Lists.newArrayList(new String[]{"친구검색","searchF"});
    }

    @Override
    protected String[] help() {
        return new String[]{"!친구검색 <캐릭터 이름> : 캐릭터가 누구한테 뒷삭 당했는지 검색합니다. 좀 오래 걸림"};
    }

    @Override
    protected String name() {
        return "BackDel";
    }
}
