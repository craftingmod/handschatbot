package com.craftingmod.maplechatbot.model;

public class ChatModel {
    public int AID;
    public int[] FriendAids;
    public String Msg;
    public long RegisterDate;
    public long Roomkey;
    public int SenderAID;
    public int SenderCID;
    public int Status;
    public int WID;

    public ChatModel(long roomKey, long rDate, int AID, int SenderAID, int SenderCID, int WorldID, String Message){
        this.Roomkey = roomKey;
        this.RegisterDate = rDate;
        this.AID = AID;
        this.SenderAID = SenderAID;
        this.SenderCID = SenderCID;
        this.WID = WorldID;
        this.Msg = Message;
    }
    public ChatModel(String Message,int[] Friends) {
        this(-1,-1,-1,-1,-1,-1,Message);
        this.FriendAids = Friends;
    }
}