package com.craftingmod.maplechatbot.model;

public class ChatModel
{
    public static final int HANDS_MESSAGE_ROOMKEY = -1;
    public static final int MESSAGE_READ = 4;
    public static final int MESSAGE_RECEIVED = 3;
    public static final int MESSAGE_SENDING = 0;
    public static final int MESSAGE_SEND_COMPLETE = 1;
    public static final int MESSAGE_SEND_FAILED = 2;
    public int AID;
    public int[] FriendAids;
    public String Msg;
    public long RegisterDate;
    public long Roomkey;
    public int SenderAID;
    public int SenderCID;
    public int Status;
    public int WID;

    public ChatModel(long roomKey, long rDate, int AID, int SenderAID, int SenderCID, int WorldID, String Message)
    {
        this.Roomkey = roomKey;
        this.RegisterDate = rDate;
        this.AID = AID;
        this.SenderAID = SenderAID;
        this.SenderCID = SenderCID;
        this.WID = WorldID;
        this.Msg = Message;
    }
    public int getSenderAccountID(){
        return this.SenderAID;
    }
    public int getSenderCharacterID(){
        return this.SenderCID;
    }
    public int getWorldID(){
        return this.WID;
    }

    public ChatModel(){
    }



    public int getReadCount()
    {
        return this.Status;
    }

    public boolean isFriendValid()
    {
        return (this.FriendAids != null) && (this.AID != 0) && (!this.Msg.isEmpty());
    }

    public boolean isGuildValid()
    {
        return (this.Roomkey > 0L) && (this.AID != 0) && (!this.Msg.isEmpty());
    }

    public boolean isValid()
    {
        return (this.Roomkey != 0L) && (this.AID != 0) && (!this.Msg.isEmpty());
    }
}