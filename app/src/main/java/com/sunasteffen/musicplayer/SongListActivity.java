package com.sunasteffen.musicplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sunasteffen.musicplayer.song.SongContent;

import java.io.File;
import java.util.List;

public class SongListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Void>{
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1000;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1001;
    private static final int AUDIO_LOADER = 2000;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);
        if (savedInstanceState == null) {
            checkPermission();
        } else {
            setupRecyclerView((RecyclerView) findViewById(R.id.song_list));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.song_detail_container) != null) {
            mTwoPane = true;
        }
    }

    private void checkPermission() {
        boolean hasNoReadPermission = checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasNoReadPermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        boolean hasNoWritePermission = checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasNoWritePermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        if (!hasNoReadPermission && !hasNoWritePermission) {
            initLoader();
        }
    }

    private boolean checkPermission(String writeExternalStorage) {
        return ContextCompat.checkSelfPermission(this, writeExternalStorage) != PackageManager.PERMISSION_GRANTED;
    }

    private void initLoader() {
        LoaderManager loaderManager = getSupportLoaderManager();
        if (loaderManager.getLoader(AUDIO_LOADER) == null) {
            loaderManager.initLoader(AUDIO_LOADER, null, this);
        } else {
            loaderManager.restartLoader(AUDIO_LOADER, null, this);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    initLoader();
                }
                break;
            }
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    initLoader();
                }
                break;
            }
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(SongContent.ITEMS));
    }

    @Override
    public Loader<Void> onCreateLoader(int id, Bundle args) {
        return new AudioLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Void> loader, Void data) {
        setupRecyclerView((RecyclerView) findViewById(R.id.song_list));
    }

    @Override
    public void onLoaderReset(Loader<Void> loader) {

    }

    private static class AudioLoader extends AsyncTaskLoader<Void> {
        private AudioLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }

        @Override
        public Void loadInBackground() {
            ContentResolver resolver = getContext().getContentResolver();
            Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            if (cursor == null) return null;
            if (!cursor.moveToFirst()) return null;
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            do {
                Log.d("Test", String.valueOf(cursor.getLong(idColumn)));
                SongContent.addItem(new SongContent.Song(cursor.getLong(idColumn),
                        cursor.getString(titleColumn),
                        cursor.getString(artistColumn)));

            } while (cursor.moveToNext());
            cursor.close();
            return null;
        }

        private void delete() {
            ContentResolver resolver = getContext().getContentResolver();
            resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.TITLE + "=?", new String[]{"Hangouts Call"});
        }

        private void insert() {
            ContentResolver resolver = getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.TITLE, "Suna Song");
            values.put(MediaStore.Audio.Media.ARTIST, "Suna Steffen");
            String path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "TestAudio.mp3").getAbsolutePath();
            values.put(MediaStore.Audio.AudioColumns.DATA, path);
            resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        }
    }

    private class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<SongContent.Song> mValues;

        private SimpleItemRecyclerViewAdapter(List<SongContent.Song> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.song_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(String.valueOf(mValues.get(position).id));
            holder.mContentView.setText(mValues.get(position).content);
            holder.mArtistView.setText(mValues.get(position).artist);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putLong(SongDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        SongDetailFragment fragment = new SongDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.song_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, SongDetailActivity.class);
                        intent.putExtra(SongDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }

                    Intent intent = new Intent(SongListActivity.this, MusicPlayerService.class);
                    intent.setData(ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, holder.mItem.id));
                    startService(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final View mView;
            private final TextView mIdView;
            private final TextView mContentView;
            private final TextView mArtistView;
            private SongContent.Song mItem;

            private ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = view.findViewById(R.id.id);
                mContentView = view.findViewById(R.id.content);
                mArtistView = view.findViewById(R.id.artist);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
