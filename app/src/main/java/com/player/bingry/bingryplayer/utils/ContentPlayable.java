package com.player.bingry.bingryplayer.utils;

import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;

import com.player.bingry.bingryplayer.provider.BingryContent;

/**
 * Created by bingry on 2017-06-03.
 */

public class ContentPlayable {

    private int mType; //Content Type
    private BitmapDrawable mBitmapDrawable;
    private BingryContent mBingryContent;
    public ContentPlayable(int type) {
        mType = type;
    }


    public ContentPlayable(Resources res, BingryContent bingrycontent) {
        mBingryContent = bingrycontent;
        if(bingrycontent.mType == 0) {
            mBitmapDrawable = new BitmapDrawable(res, bingrycontent.mBitmap);
        }
    }

    public BingryContent getBingryContent(){
        return mBingryContent;
    }
}
