package com.tacticmaster;

import android.view.Window;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Window-related helpers shared across activities.
 */
public final class WindowUtils {

    private WindowUtils() {
    }

    /**
     * Hides the system navigation bar using immersive-sticky behavior: an
     * edge swipe reveals the bar transiently, then it auto-hides. Call again
     * on window focus regain to keep it hidden.
     */
    public static void hideNavigationBar(Window window) {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        controller.hide(WindowInsetsCompat.Type.navigationBars());
    }
}
