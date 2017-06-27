package com.player.bingry.bingryplayer.ui;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.player.bingry.bingryplayer.R;
import com.player.bingry.bingryplayer.BuildConfig;
import com.player.bingry.bingryplayer.provider.BingryContent;
import com.player.bingry.bingryplayer.utils.ImageCache;
import com.player.bingry.bingryplayer.utils.ImageFetcher;
import com.player.bingry.bingryplayer.utils.Utils;

/**
 * Created by bingry on 2017-06-11.
 */

public class ContentDetailActivity extends FragmentActivity implements View.OnClickListener {
    private static final String IMAGE_CACHE_DIR = "images";
    public static final String EXTRA_IMAGE = "large_image";


    private ImagePagerAdapter mAdapter;
    private ImageFetcher mImageFetcher;
    private ViewPager mPager;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstaceState){
        if(BuildConfig.DEBUG){
            Utils.enableStrictMode();
        }

        super.onCreate(savedInstaceState);
        setContentView(R.layout.content_detail_pager);

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        final int longest = (height > width ? height:width)/2;

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f);

        mImageFetcher = new ImageFetcher(this, longest);
        mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
        mImageFetcher.setImageFadeIn(false);

        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), BingryContent.ITEMS.size());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setPageMargin((int)getResources().getDimension(R.dimen.horizontal_page_margin));
        mPager.setOffscreenPageLimit(2);

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
/*
        if(Utils.hasHoneycomb()){
            final ActionBar actionBar = getActionBar();

            //actionBar.setDisplayShowTitleEnabled(false);
            //actionBar.setDisplayHomeAsUpEnabled(true);

            mPager.setOnSystemUiVisibilityChangeListener(
                    new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int vis) {
                            // TODO Auto-generated method stub
                            if((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) !=0){
                                actionBar.hide();
                            } else {
                                actionBar.show();
                            }
                        }
                    });
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            actionBar.show();
        }
*/
        final int extraCurrentItem = getIntent().getIntExtra(EXTRA_IMAGE, -1);
        if(extraCurrentItem !=-1){
            mPager.setCurrentItem(extraCurrentItem);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mImageFetcher.closeCache();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.clear_cache:
                mImageFetcher.clearCache();
                Toast.makeText(this, "Caches have been cleared", Toast.LENGTH_SHORT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public ImageFetcher getImageFetcher(){
        return mImageFetcher;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        final int vis = mPager.getSystemUiVisibility();

        if((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0){
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        } else {
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {
        private final int mSize;

        public ImagePagerAdapter(android.support.v4.app.FragmentManager fm, int size) {
            super(fm);
            mSize = size;
        }

        @Override
        public int getCount() {
            return mSize;
        }

        @Override
        public Fragment getItem(int position) {
            return ContentDetailFragment.newInstance(BingryContent.ITEMS.get(position).details);
        }
    }
}
