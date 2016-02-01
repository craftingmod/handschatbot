package com.craftingmod.maplechatbot.model;

public class CharaInfoModel  {

    public static final int FRIEND_ACCOUNT = 2;
    public static final int FRIEND_NORMAL = 1;
    public int mAccountID;
    public String mAvatarImgUrl = "";
    public int mCharacterID;
    public String mCharacterName = "";
    public boolean mCheckLogined = false;
    public long mExp;
    public String mExtraInfo = "";
    public int mFlag;
    public int mFriendType = 1;
    public int mGender;
    public int mGuildGrade = 0;
    public int mGuildID = 0;
    public String mGuildName = "";
    public boolean mIsEnable = true;
    public boolean mIsMasterCharacter;
    public boolean mIsOnline;
    public boolean mIsStart;
    public boolean mIsUpdateUserdata = false;
    public boolean mIsValid = false;
    public String mJobDetailName = "";
    public String mJobName = "";
    public int mJobRank;
    public long mLastReadTime;
    public int mLevel;
    public String mNickName = "";
    public int mPop;
    public int mPopRnk;
    public boolean mRecvLogined = false;
    public String mStarName;
    public int mTotalRank;
    public boolean mUseCheckbox = false;
    public boolean mUseMobile;
    public int mWorldID;
    public String mWorldIconUrl = "";
    public String mWorldName = "";
    public int mWorldRank;

    public CharaInfoModel() {}

    public void Copy(CharaInfoModel param)
    {
        this.mAccountID = param.mAccountID;
        this.mCharacterID = param.mCharacterID;
        this.mAvatarImgUrl = param.mAvatarImgUrl;
        this.mCharacterName = param.mCharacterName;
        this.mNickName = param.mNickName;
        this.mLevel = param.mLevel;
        this.mJobName = param.mJobName;
        this.mJobDetailName = param.mJobDetailName;
        this.mWorldName = param.mWorldName;
        this.mWorldIconUrl = param.mWorldIconUrl;
        this.mWorldID = param.mWorldID;
        this.mIsOnline = param.mIsOnline;
        this.mFriendType = param.mFriendType;
        this.mGuildName = param.mGuildName;
        this.mGuildID = param.mGuildID;
        this.mExtraInfo = param.mExtraInfo;
        this.mIsMasterCharacter = param.mIsMasterCharacter;
        this.mIsValid = param.mIsValid;
        this.mStarName = param.mStarName;
        this.mUseMobile = param.mUseMobile;
        this.mLastReadTime = param.mLastReadTime;
        this.mPop = param.mPop;
        this.mTotalRank = param.mTotalRank;
        this.mExp = param.mExp;
        this.mWorldRank = param.mWorldRank;
        this.mJobRank = param.mJobRank;
        this.mPopRnk = param.mPopRnk;
        this.mIsStart = param.mIsStart;
        this.mFlag = param.mFlag;
    }

    public void CopyCharacterInfo(UserModel param){
        this.mAccountID = param.accountID;
        this.mCharacterID = param.characterID;
        this.mAvatarImgUrl = param.userImage;
        this.mCharacterName = param.userName;
        this.mLevel = param.level;
        this.mJobName = param.job;
       // this.mJobDetailName = param.
        this.mWorldName = param.worldName;
     //   this.mWorldIconUrl =
        this.mWorldID = param.worldID;
        this.mPop = param.pop;
        this.mTotalRank = param.totalRank;
        this.mExp = param.Exp;
        this.mWorldRank = param.worldRank;
        this.mJobRank = param.jobRank;
        this.mPopRnk = param.popRank;
    }

    public String GetDetailString(){
        return "Lv." + this.mLevel;
    }

    public void Init()
    {
        this.mIsUpdateUserdata = false;
        if (this.mFriendType == 2) {
            this.mIsValid = false;
        }
    }

    public boolean hasGuild(){
        return this.mGuildID > 0;
    }

    public boolean isGuildMaster()
    {
        boolean bool2 = true;
        boolean bool1 = bool2;
        if (this.mGuildGrade != 1)
        {
            bool1 = bool2;
            if (this.mGuildGrade != 2) {
                bool1 = false;
            }
        }
        return bool1;
    }
}