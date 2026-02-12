package com.tacticmaster.sound;

import static java.util.Objects.isNull;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.tacticmaster.R;

import java.util.concurrent.locks.ReentrantLock;

public final class SoundPlayer {

    private static final String TAG = "SoundPlayer";
    private static SoundPlayer INSTANCE;
    private final ReentrantLock lock = new ReentrantLock();
    private MediaPlayer mediaPlayer;

    private SoundPlayer() {
    }

    public static SoundPlayer getInstance() {
        synchronized (SoundPlayer.class) {
            if (isNull(INSTANCE)) {
                INSTANCE = new SoundPlayer();
            }
        }
        return INSTANCE;
    }

    public void playMoveSound(Context context, boolean isCaptureMove) {
        lock.lock();
        AssetFileDescriptor afd = null;
        try {
            if (!isNull(mediaPlayer) && mediaPlayer.isPlaying()) return;
            stopLocked();
            afd = isCaptureMove ? context.getResources().openRawResourceFd(R.raw.capture) : context.getResources().openRawResourceFd(R.raw.move);
            if (isNull(afd)) {
                Log.w(TAG, "Raw resource not found id=" + (isCaptureMove ? R.raw.capture : R.raw.move));
                return;
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            applyAttributesAndStart(context);
            Log.i(TAG, "Custom raw alarm started");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start raw alarm", e);
            stopLocked();
        } finally {
            try {
                if (!isNull(afd)) afd.close();
            } catch (Exception ignore) {
            }
            lock.unlock();
        }
    }

    private void applyAttributesAndStart(Context context) throws Exception {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        mediaPlayer.setAudioAttributes(attrs);
        mediaPlayer.setLooping(false);
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (!isNull(audioManager)) {
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxVolume, 0);
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to set alarm stream volume", e);
        }
        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    private void stopLocked() {
        if (!isNull(mediaPlayer)) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            } catch (IllegalStateException e) {
                Log.w(TAG, "MediaPlayer state error on stop", e);
            }
            try {
                mediaPlayer.release();
            } catch (Exception ignore) {
            }
            mediaPlayer = null;
            Log.i(TAG, "Alarm sound stopped");
        }
    }
}