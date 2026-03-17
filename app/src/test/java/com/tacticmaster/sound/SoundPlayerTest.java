package com.tacticmaster.sound;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.MediaPlayer;

import com.tacticmaster.R;
import com.tacticmaster.settings.SettingsManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.FileDescriptor;

@RunWith(MockitoJUnitRunner.class)
public class SoundPlayerTest {

    @Mock
    private Context mockContext;

    @Mock
    private Resources mockResources;

    @Mock
    private AssetFileDescriptor mockAfd;

    @Mock
    private FileDescriptor mockFd;

    @Mock
    private SettingsManager mockSettingsManager;

    private SoundPlayer soundPlayer;

    @Before
    public void setUp() {
        soundPlayer = SoundPlayer.getInstance();
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getResources()).thenReturn(mockResources);
        when(mockAfd.getFileDescriptor()).thenReturn(mockFd);
        when(mockAfd.getStartOffset()).thenReturn(0L);
        when(mockAfd.getLength()).thenReturn(1000L);
    }

    @Test
    public void testGetInstance_ReturnsSingleton() {
        SoundPlayer instance1 = SoundPlayer.getInstance();
        SoundPlayer instance2 = SoundPlayer.getInstance();

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    public void testPlayMoveSound_WhenSoundDisabled_DoesNotPlay() {
        when(mockSettingsManager.isSoundEnabled()).thenReturn(false);

        try (var settingsStatic = Mockito.mockStatic(SettingsManager.class)) {
            settingsStatic.when(() -> SettingsManager.getInstance(mockContext))
                    .thenReturn(mockSettingsManager);

            soundPlayer.playMoveSound(mockContext, false);

            verify(mockResources, never()).openRawResourceFd(anyInt());
        }
    }

    @Test
    public void testPlayMoveSound_PlaysMoveSound() throws Exception {
        when(mockSettingsManager.isSoundEnabled()).thenReturn(true);
        when(mockResources.openRawResourceFd(R.raw.move)).thenReturn(mockAfd);

        try (var settingsStatic = Mockito.mockStatic(SettingsManager.class);
             var mediaPlayerConstruction = Mockito.mockConstruction(MediaPlayer.class)) {

            settingsStatic.when(() -> SettingsManager.getInstance(mockContext))
                    .thenReturn(mockSettingsManager);

            soundPlayer.playMoveSound(mockContext, false);

            verify(mockResources).openRawResourceFd(R.raw.move);
            if (!mediaPlayerConstruction.constructed().isEmpty()) {
                MediaPlayer mp = mediaPlayerConstruction.constructed().get(0);
                verify(mp).setDataSource(mockFd, 0L, 1000L);
                verify(mp).prepare();
                verify(mp).start();
            }
        }
    }

    @Test
    public void testPlayMoveSound_PlaysCaptureSound() throws Exception {
        when(mockSettingsManager.isSoundEnabled()).thenReturn(true);
        when(mockResources.openRawResourceFd(R.raw.capture)).thenReturn(mockAfd);

        try (var settingsStatic = Mockito.mockStatic(SettingsManager.class);
             var mediaPlayerConstruction = Mockito.mockConstruction(MediaPlayer.class)) {

            settingsStatic.when(() -> SettingsManager.getInstance(mockContext))
                    .thenReturn(mockSettingsManager);

            soundPlayer.playMoveSound(mockContext, true);

            verify(mockResources).openRawResourceFd(R.raw.capture);
            if (!mediaPlayerConstruction.constructed().isEmpty()) {
                MediaPlayer mp = mediaPlayerConstruction.constructed().get(0);
                verify(mp).setDataSource(mockFd, 0L, 1000L);
                verify(mp).prepare();
                verify(mp).start();
            }
        }
    }

    @Test
    public void testPlayMoveSound_WhenAlreadyPlaying_DoesNotStartNew() throws Exception {
        when(mockSettingsManager.isSoundEnabled()).thenReturn(true);
        when(mockResources.openRawResourceFd(R.raw.move)).thenReturn(mockAfd);

        try (var settingsStatic = Mockito.mockStatic(SettingsManager.class);
             var mediaPlayerConstruction = Mockito.mockConstruction(MediaPlayer.class,
                     (mock, context) -> {
                         when(mock.isPlaying()).thenReturn(true);
                     })) {

            settingsStatic.when(() -> SettingsManager.getInstance(mockContext))
                    .thenReturn(mockSettingsManager);

            // First call - should play
            soundPlayer.playMoveSound(mockContext, false);

            // Second call - should be ignored because already playing
            soundPlayer.playMoveSound(mockContext, false);

            // Only one MediaPlayer should be created
            if (!mediaPlayerConstruction.constructed().isEmpty()) {
                MediaPlayer mp = mediaPlayerConstruction.constructed().get(0);
                verify(mp).isPlaying();
            }
        }
    }

    @Test
    public void testPlayMoveSound_HandlesException() throws Exception {
        when(mockSettingsManager.isSoundEnabled()).thenReturn(true);
        when(mockResources.openRawResourceFd(R.raw.move)).thenThrow(new RuntimeException("Test exception"));

        try (var settingsStatic = Mockito.mockStatic(SettingsManager.class)) {
            settingsStatic.when(() -> SettingsManager.getInstance(mockContext))
                    .thenReturn(mockSettingsManager);

            // Should not throw exception
            soundPlayer.playMoveSound(mockContext, false);
        }
    }

    @Test
    public void testRelease_CleansUpResources() throws Exception {
        when(mockSettingsManager.isSoundEnabled()).thenReturn(true);
        when(mockResources.openRawResourceFd(R.raw.move)).thenReturn(mockAfd);

        try (var settingsStatic = Mockito.mockStatic(SettingsManager.class);
             var mediaPlayerConstruction = Mockito.mockConstruction(MediaPlayer.class)) {

            settingsStatic.when(() -> SettingsManager.getInstance(mockContext))
                    .thenReturn(mockSettingsManager);

            soundPlayer.playMoveSound(mockContext, false);
            soundPlayer.release();

            if (!mediaPlayerConstruction.constructed().isEmpty()) {
                MediaPlayer mp = mediaPlayerConstruction.constructed().get(0);
                verify(mp).release();
            }
        }
    }

    @Test
    public void testMediaPlayerOnCompletion_ReleasesResources() throws Exception {
        when(mockSettingsManager.isSoundEnabled()).thenReturn(true);
        when(mockResources.openRawResourceFd(R.raw.move)).thenReturn(mockAfd);

        try (var settingsStatic = Mockito.mockStatic(SettingsManager.class);
             var mediaPlayerConstruction = Mockito.mockConstruction(MediaPlayer.class,
                     (mock, context) -> {
                         when(mock.isPlaying()).thenReturn(false);
                     })) {

            settingsStatic.when(() -> SettingsManager.getInstance(mockContext))
                    .thenReturn(mockSettingsManager);

            soundPlayer.playMoveSound(mockContext, false);

            if (!mediaPlayerConstruction.constructed().isEmpty()) {
                MediaPlayer mp = mediaPlayerConstruction.constructed().get(0);

                // Get the completion listener and trigger it
                verify(mp).setOnCompletionListener(any());
                MediaPlayer.OnCompletionListener listener =
                        Mockito.mockingDetails(mp).getInvocations().stream()
                                .filter(inv -> inv.getMethod().getName().equals("setOnCompletionListener"))
                                .findFirst()
                                .map(inv -> (MediaPlayer.OnCompletionListener) inv.getArgument(0))
                                .orElse(null);

                if (listener != null) {
                    listener.onCompletion(mp);
                    verify(mp).release();
                }
            }
        }
    }

    @Test
    public void testMediaPlayerOnError_ReleasesResources() throws Exception {
        when(mockSettingsManager.isSoundEnabled()).thenReturn(true);
        when(mockResources.openRawResourceFd(R.raw.move)).thenReturn(mockAfd);

        try (var settingsStatic = Mockito.mockStatic(SettingsManager.class);
             var mediaPlayerConstruction = Mockito.mockConstruction(MediaPlayer.class)) {

            settingsStatic.when(() -> SettingsManager.getInstance(mockContext))
                    .thenReturn(mockSettingsManager);

            soundPlayer.playMoveSound(mockContext, false);

            if (!mediaPlayerConstruction.constructed().isEmpty()) {
                MediaPlayer mp = mediaPlayerConstruction.constructed().get(0);

                // Get the error listener and trigger it
                verify(mp).setOnErrorListener(any());
                MediaPlayer.OnErrorListener listener =
                        Mockito.mockingDetails(mp).getInvocations().stream()
                                .filter(inv -> inv.getMethod().getName().equals("setOnErrorListener"))
                                .findFirst()
                                .map(inv -> (MediaPlayer.OnErrorListener) inv.getArgument(0))
                                .orElse(null);

                if (listener != null) {
                    listener.onError(mp, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
                    verify(mp).release();
                }
            }
        }
    }
}