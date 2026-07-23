package com.tacticmaster.settings;

import static java.util.Objects.isNull;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.preference.DialogPreference;

import com.tacticmaster.R;
import com.tacticmaster.board.ChessboardPieceManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Dialog for {@link PieceSetPreference}: a list where each row shows the set's
 * label above a strip of preview pieces rendered in that set's style, with the
 * currently-selected set highlighted. Tapping a row persists the choice through
 * the preference and closes the dialog.
 *
 * <p>Unlike {@code PreferenceDialogFragmentCompat}, this resolves its preference
 * from the parent {@link androidx.preference.PreferenceFragmentCompat} by key,
 * avoiding the deprecated {@code setTargetFragment} mechanism.</p>
 */
public class PieceSetPreferenceDialog extends DialogFragment {

    /**
     * The pieces previewed for each set, paired with the row {@link ImageView}
     * they render into. The codes are the color+piece asset names (e.g. {@code wn}
     * = white knight) used by {@link ChessboardPieceManager#loadPreviewPiece}.
     */
    private enum PreviewPiece {
        KING("wk", R.id.piece_set_preview_king),
        QUEEN("wq", R.id.piece_set_preview_queen),
        ROOK("wr", R.id.piece_set_preview_rook),
        BISHOP("wb", R.id.piece_set_preview_bishop),
        KNIGHT("wn", R.id.piece_set_preview_knight),
        PAWN("wp", R.id.piece_set_preview_pawn);

        final String code;
        final int imageViewId;

        PreviewPiece(String code, int imageViewId) {
            this.code = code;
            this.imageViewId = imageViewId;
        }
    }

    private static final String ARG_KEY = "key";

    public static PieceSetPreferenceDialog newInstance(String key) {
        PieceSetPreferenceDialog fragment = new PieceSetPreferenceDialog();
        Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Locates the preference this dialog acts on via the parent fragment (a
     * {@link DialogPreference.TargetFragment}) using the key passed in arguments.
     */
    private PieceSetPreference requirePreference() {
        String key = requireArguments().getString(ARG_KEY);
        Fragment parent = getParentFragment();
        if (!(parent instanceof DialogPreference.TargetFragment)) {
            throw new IllegalStateException(
                    "Parent fragment must implement DialogPreference.TargetFragment");
        }
        return ((DialogPreference.TargetFragment) parent).findPreference(key);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = requireContext();
        String[] sets = ChessboardPieceManager.availablePieceSets(context);

        PieceSetPreference preference = requirePreference();
        int selectedIndex = indexOf(sets, preference.getValue());

        PieceSetAdapter adapter = new PieceSetAdapter(context, sets, selectedIndex);
        return new AlertDialog.Builder(context)
                .setTitle(preference.getTitle())
                .setNegativeButton(android.R.string.cancel, null)
                // Tapping a row commits immediately, so no positive button. The
                // selected set is shown via the adapter's row highlight rather than
                // a single-choice check.
                .setAdapter(adapter, (dialog, which) -> {
                    preference.setValue(sets[which]);
                    dialog.dismiss();
                })
                .create();
    }

    /**
     * Position of {@code value} in {@code sets}, or -1 if absent.
     */
    private static int indexOf(String[] sets, String value) {
        for (int i = 0; i < sets.length; i++) {
            if (sets[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Renders each set with its label on top and a strip of preview pieces below,
     * and highlights the currently-selected row. Preview bitmaps are loaded once
     * per set+piece and cached so scrolling doesn't re-decode assets.
     */
    private static final class PieceSetAdapter extends BaseAdapter {

        private final Context context;
        private final String[] sets;
        private final int selectedPosition;
        private final int highlightColor;
        private final Map<String, Bitmap> previewCache = new HashMap<>();

        PieceSetAdapter(Context context, String[] sets, int selectedPosition) {
            this.context = context;
            this.sets = sets;
            this.selectedPosition = selectedPosition;
            this.highlightColor = resolveHighlightColor(context);
        }

        /** The theme's selected/pressed surface tint, used to highlight the current set. */
        private static int resolveHighlightColor(Context context) {
            android.util.TypedValue value = new android.util.TypedValue();
            context.getTheme().resolveAttribute(
                    androidx.appcompat.R.attr.colorControlHighlight, value, true);
            return value.data;
        }

        @Override
        public int getCount() {
            return sets.length;
        }

        @Override
        public Object getItem(int position) {
            return sets[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (isNull(view)) {
                view = LayoutInflater.from(context)
                        .inflate(R.layout.item_piece_set, parent, false);
            }

            String set = sets[position];
            TextView label = view.findViewById(R.id.piece_set_label);

            for (PreviewPiece piece : PreviewPiece.values()) {
                ImageView imageView = view.findViewById(piece.imageViewId);
                imageView.setImageBitmap(previewFor(set, piece.code));
            }
            label.setText(PieceSetPreference.capitalize(set));

            // Highlight the currently-selected set. Cleared on other rows because
            // convertView is recycled.
            view.setBackgroundColor(position == selectedPosition
                    ? highlightColor
                    : android.graphics.Color.TRANSPARENT);

            return view;
        }

        private Bitmap previewFor(String set, String code) {
            String key = set + "/" + code;
            Bitmap cached = previewCache.get(key);
            if (!isNull(cached)) {
                return cached;
            }
            Bitmap bitmap = ChessboardPieceManager.loadPreviewPiece(context, set, code);
            previewCache.put(key, bitmap);
            return bitmap;
        }
    }
}
