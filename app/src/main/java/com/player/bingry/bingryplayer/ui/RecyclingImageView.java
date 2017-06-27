package com.player.bingry.bingryplayer.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.player.bingry.bingryplayer.utils.RecyclingBitmapDrawable;

/**
 * Created by bingry on 2017-06-07.
 */

public class RecyclingImageView extends android.support.v7.widget.AppCompatImageView {
    public RecyclingImageView(Context context){
        super(context);
    }

    public RecyclingImageView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    protected void onDetachedFromWindoww() {
        setImageDrawable(null);
        super.onDetachedFromWindow();
    }

    @Override
    public void setImageDrawable(Drawable drawable){
        final Drawable previousDrawable = getDrawable();

        super.setImageDrawable(drawable);
        notifyDrawable(drawable, true);
        notifyDrawable(previousDrawable, false);
    }

    private static void notifyDrawable(Drawable drawable, final boolean isDisplayed){
        if(drawable instanceof RecyclingBitmapDrawable){
            ((RecyclingBitmapDrawable) drawable).setIsDisplayed(isDisplayed);
        }
    }
}