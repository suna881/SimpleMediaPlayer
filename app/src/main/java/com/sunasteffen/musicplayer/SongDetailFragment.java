package com.sunasteffen.musicplayer;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sunasteffen.musicplayer.song.SongContent;

public class SongDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "item_id";
    private SongContent.Song mItem;

    public SongDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = SongContent.ITEM_MAP.get(getArguments().getLong(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.content);
            }
        }

        View rootView = inflater.inflate(R.layout.song_detail, container, false);
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.song_detail)).setText(mItem.content);
        }

        return rootView;
    }
}
