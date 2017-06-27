package com.player.bingry.bingryplayer.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.player.bingry.bingryplayer.utils.ImageFetcher;

import com.player.bingry.bingryplayer.R;
import com.player.bingry.bingryplayer.utils.ImageWorker;

/**
 * Created by bingry on 2017-06-11.
 */

public class ContentDetailFragment extends Fragment {
    private static final String IMAGE_DATA_EXTRA = "extra_image_data";
    private String mImageUrl;
    private ImageView mImageView;
    private ImageFetcher mImageFetcher;


    public static ContentDetailFragment newInstance(String imageUrl){
        final ContentDetailFragment f= new ContentDetailFragment();

        final Bundle args = new Bundle();
        args.putString(IMAGE_DATA_EXTRA, imageUrl);
        f.setArguments(args);
        return f;
    }

    public ContentDetailFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mImageUrl = getArguments() != null ? getArguments().getString(IMAGE_DATA_EXTRA) : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final View v = inflater.inflate(R.layout.content_detail_fragment, container, false);
        mImageView = (ImageView)v.findViewById(R.id.imageView);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        if(ContentDetailActivity.class.isInstance(getActivity())){
            mImageFetcher = ((ContentDetailActivity)getActivity()).getImageFetcher();
            mImageFetcher.loadImage(mImageUrl, mImageView);
        }
    }
}
