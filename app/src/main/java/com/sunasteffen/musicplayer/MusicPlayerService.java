package com.sunasteffen.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private ConcurrentLinkedQueue<Uri> trackQueue;
    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        trackQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Uri trackUri = intent.getData();
        addTrackToQueue(trackUri);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void addTrackToQueue(Uri trackUri) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, trackUri);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.start();
        } else {
            trackQueue.offer(trackUri);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.reset();
        Uri nextTrackUri = trackQueue.poll();
        if (nextTrackUri != null) {
            try {
                mediaPlayer.setDataSource(this, nextTrackUri);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
                stopSelf();
            }
        } else {
            stopSelf();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.e(MusicPlayerService.class.getSimpleName(), "onError");
        return false;
    }
}
