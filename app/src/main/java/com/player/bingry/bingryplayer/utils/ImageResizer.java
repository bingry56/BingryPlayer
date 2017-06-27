package com.player.bingry.bingryplayer.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileDescriptor;
import com.player.bingry.bingryplayer.BuildConfig;
/**
 * Created by bingry on 2017-06-08.
 */

public class ImageResizer extends ImageWorker{
    private static final String TAG = "ImageResizer";
    protected int mImageWidth;
    protected int mImageHeight;

    public ImageResizer(Context context, int imageWidth, int imageHeight) {
        super(context);
        setImageSize(imageWidth, imageHeight);
    }

    public ImageResizer(Context context, int imageSize) {
        // TODO Auto-generated constructor stub
        super(context);
        setImageSize(imageSize);
    }

    public void setImageSize(int width, int height) {
        // TODO Auto-generated method stub
        mImageWidth = width;
        mImageHeight= height;
    }
    public void setImageSize(int size) {
        setImageSize(size, size);
    }

    private Bitmap processBitmap(int resId){
        if(BuildConfig.DEBUG){
            Log.d(TAG, "processBitmap - "+ resId);
        }
        return decodeSampledBitmapFromResource(mResources, resId, mImageWidth,
                mImageHeight, getImageCache());
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        // TODO Auto-generated method stub
        return processBitmap(Integer.parseInt(String.valueOf(data)));
    }
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight, ImageCache cache) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        if(Utils.hasHoneycomb()){
            addInBitmapOptions(options, cache);
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromFile(String filename,
                                                     int reqWidth, int reqHeight, ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    public static Bitmap decodeSampledBitmapFromDescriptor(
            FileDescriptor fileDescriptor, int reqWidth, int reqHeight, ImageCache cache) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        // If we're running on Honeycomb or newer, try to use inBitmap
        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    private static void addInBitmapOptions(BitmapFactory.Options options, ImageCache cache) {
        // TODO Auto-generated method stub
        options.inMutable = true;

        if(cache != null){
            Bitmap inBitmap = cache.getBitmapFromReusableSet(options);
            if(inBitmap != null){
                options.inBitmap = inBitmap;
            }
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize =1;

        if(height > reqHeight || width > reqWidth){
            final int halfHeight = height /2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            long totalPixels = width*height / inSampleSize;

            final long totalReqPixelsCap = reqWidth*reqHeight*2;

            while(totalPixels > totalReqPixelsCap){
                inSampleSize *=2;
                totalPixels /=2;
            }
        }

        return inSampleSize;
    }



}
