package com.player.bingry.bingryplayer.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.player.bingry.bingryplayer.R;
import com.player.bingry.bingryplayer.provider.BingryContent;
import com.player.bingry.bingryplayer.utils.ImageCache;
import com.player.bingry.bingryplayer.utils.ImageFetcher;
import com.player.bingry.bingryplayer.utils.Utils;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PlayerListFragment extends Fragment {
    private static final String TAG = "PlayerListFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    //private ContentAdapter mAdapter;
    private ImageFetcher mImageFetcher;
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 2;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayerListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static PlayerListFragment newInstance(int columnCount) {
        PlayerListFragment fragment = new PlayerListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
        mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        //final RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        // Set the adapter
        if (view instanceof RecyclerView) {
 //           Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

//            if (mColumnCount <= 1) {
//                recyclerView.setLayoutManager(new LinearLayoutManager(context));
//            } else {
//                //recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
//                recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
//            }
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
            recyclerView.setAdapter(new BingryItemRecyclerViewAdapter(/*BingryContent.ITEMS, */mListener));
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                    super.onScrollStateChanged(recyclerView, scrollState);
                    // TODO Auto-generated method stub
                    if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                        if (!Utils.hasHoneycomb()) {
                            mImageFetcher.setPauseWork(true);
                        }
                    } else {
                        mImageFetcher.setPauseWork(false);
                    }
                }
            });
        }

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause(){
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override
    public void  onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.main_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_cache:
                mImageFetcher.clearCache();
                Toast.makeText(getActivity(), R.string.clear_cache_complete_toast,
                        Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(BingryItemRecyclerViewAdapter.ViewHolder holder);
    }

    public class BingryItemRecyclerViewAdapter extends RecyclerView.Adapter<BingryItemRecyclerViewAdapter.ViewHolder> {

        private final List<BingryContent.BingryItem> mValues;
        private final OnListFragmentInteractionListener mListener;
        private final List<BingryContent.VodData> mVodList;

        public BingryItemRecyclerViewAdapter(/*List<BingryContent.BingryItem> items, */OnListFragmentInteractionListener listener) {
            mValues = BingryContent.ITEMS;//items;
            mListener = listener;
            mVodList = BingryContent.VOD_ITEMS;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

//            holder.mItem = mValues.get(position);
//            holder.mIdView.setText(mValues.get(position).id);
//            holder.mContentView.setText(mValues.get(position).content);
//            holder.mContentDetailView.setText(mValues.get(position).details);
//            //holder.mImageView.setImageResource(R.mipmap.ic_launcher);
            //mImageFetcher.loadImage(mValues.get(position).content, holder.mImageView);

            //mVodItem
            holder.mVodItem = mVodList.get(position);
            holder.mIdView.setText(mVodList.get(position).mVideoId);
            holder.mContentView.setText(mVodList.get(position).mVideoTitle);
            holder.mContentDetailView.setText(mVodList.get(position).mData);
            mImageFetcher.loadImage(mVodList.get(position).mVideoId, holder.mImageView);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        mListener.onListFragmentInteraction(holder);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {

            //return mValues.size();
            return mVodList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mImageView;
            public final TextView mIdView;
            public final TextView mContentView;
            public final TextView mContentDetailView;
            public BingryContent.BingryItem mItem;
            public BingryContent.VodData mVodItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.imageView);
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
                mContentDetailView = (TextView) view.findViewById(R.id.detail);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
