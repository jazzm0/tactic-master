package com.tacticmaster.sound;

import static java.util.Objects.isNull;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;

import com.tacticmaster.R;
import com.tacticmaster.settings.SettingsManager;

import java.util.concurrent.locks.ReentrantLock;

public final class SoundPlayer {

    private static final String TAG = "SoundPlayer";
    private static SoundPlayer INSTANCE;
    private final ReentrantLock lock = new ReentrantLock();
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private boolean hasAudioFocus = false;

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

    /**
     * Plays a move or capture sound. If a sound is already playing, this call returns early.
     * Uses application context to avoid memory leaks.
     *
     * @param context       Activity or application context
     * @param isCaptureMove true for capture sound, false for move sound
     */
    public void playMoveSound(Context context, boolean isCaptureMove) {
        SettingsManager settingsManager = SettingsManager.getInstance(context);
        if (!settingsManager.isSoundEnabled()) {
            return;
        }

        lock.lock();
        AssetFileDescriptor afd = null;
        try {
            if (!isNull(mediaPlayer) && isMediaPlayerPlaying()) {
                return;
            }

            stopLocked();

            Context appContext = context.getApplicationContext();

            afd = isCaptureMove ?
                    appContext.getResources().openRawResourceFd(R.raw.capture) :
                    appContext.getResources().openRawResourceFd(R.raw.move);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

            mediaPlayer.setOnCompletionListener(mp -> {
                lock.lock();
                try {
                    abandonAudioFocus();
                    releaseMediaPlayer();
                    Log.d(TAG, "Move sound playback completed");
                } finally {
                    lock.unlock();
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: what=" + what + " extra=" + extra);
                lock.lock();
                try {
                    abandonAudioFocus();
                    releaseMediaPlayer();
                } finally {
                    lock.unlock();
                }
                return true;
            });

            applyAttributesAndStart(appContext);
            Log.d(TAG, "Move sound started: " + (isCaptureMove ? "capture" : "move"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to play move sound", e);
            abandonAudioFocus();
            releaseMediaPlayer();
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

        // Request audio focus before playing
        if (!requestAudioFocus(context, attrs)) {
            Log.w(TAG, "Failed to gain audio focus, playing anyway");
        }
        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    private boolean requestAudioFocus(Context context, AudioAttributes attrs) {
        try {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (isNull(audioManager)) {
                return false;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                        .setAudioAttributes(attrs)
                        .setOnAudioFocusChangeListener(focusChange -> {
                            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                                lock.lock();
                                try {
                                    stopLocked();
                                } finally {
                                    lock.unlock();
                                }
                            }
                        })
                        .build();
                int result = audioManager.requestAudioFocus(audioFocusRequest);
                hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
                return hasAudioFocus;
            } else {
                int result = audioManager.requestAudioFocus(
                        focusChange -> {
                            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                                lock.lock();
                                try {
                                    stopLocked();
                                } finally {
                                    lock.unlock();
                                }
                            }
                        },
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                );
                hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
                return hasAudioFocus;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error requesting audio focus", e);
            return false;
        }
    }

    private void abandonAudioFocus() {
        if (hasAudioFocus && !isNull(audioManager)) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isNull(audioFocusRequest)) {
                    audioManager.abandonAudioFocusRequest(audioFocusRequest);
                } else {
                    audioManager.abandonAudioFocus(null);
                }
                hasAudioFocus = false;
                Log.d(TAG, "Audio focus abandoned");
            } catch (Exception e) {
                Log.w(TAG, "Error abandoning audio focus", e);
            }
        }
    }

    private boolean isMediaPlayerPlaying() {
        try {
            return mediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            Log.w(TAG, "MediaPlayer in invalid state when checking isPlaying", e);
            return false;
        }
    }

    private void releaseMediaPlayer() {
        if (!isNull(mediaPlayer)) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                Log.w(TAG, "Error releasing MediaPlayer", e);
            }
            mediaPlayer = null;
        }
    }

    private void stopLocked() {
        if (!isNull(mediaPlayer)) {
            try {
                if (isMediaPlayerPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (IllegalStateException e) {
                Log.w(TAG, "MediaPlayer state error on stop", e);
            }
            abandonAudioFocus();
            releaseMediaPlayer();
            Log.d(TAG, "Move sound stopped");
        }
    }

    public void release() {
        lock.lock();
        try {
            stopLocked();
            audioManager = null;
            audioFocusRequest = null;
            Log.d(TAG, "SoundPlayer released");
        } finally {
            lock.unlock();
        }
    }
}
