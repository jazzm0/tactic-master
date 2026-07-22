package com.tacticmaster.settings;

import static java.util.Objects.isNull;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;

import com.tacticmaster.board.ChessboardPieceManager;

/**
 * A piece-set picker that opens a dialog listing every available set with a
 * rendered preview piece, instead of the plain text-only dropdown. Persistence
 * flows through the same {@link SettingsPreferenceDataStore} as before (keyed
 * {@code piece_set}); this class only changes how the choice is presented.
 *
 * <p>The dialog itself is handled by {@link PieceSetPreferenceDialog}, wired up
 * in {@link SettingsFragment#onDisplayPreferenceDialog}.</p>
 */
public class PieceSetPreference extends DialogPreference {

    public PieceSetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * The currently selected set (raw folder name), falling back to the default
     * set when nothing has been persisted yet.
     */
    public String getValue() {
        String stored = getPersistedString(null);
        if (isNull(stored) || stored.isEmpty()) {
            return ChessboardPieceManager.defaultPieceSet(getContext());
        }
        return stored;
    }

    /**
     * Persists the chosen set and refreshes the summary.
     */
    public void setValue(String pieceSet) {
        persistString(pieceSet);
        notifyChanged();
    }

    @NonNull
    @Override
    public CharSequence getSummary() {
        return capitalize(getValue());
    }

    static String capitalize(String s) {
        if (isNull(s) || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
