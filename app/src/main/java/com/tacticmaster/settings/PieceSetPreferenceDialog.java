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
import android.widget.RadioButton;
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
 * Dialog for {@link PieceSetPreference}: a single-choice list where each row shows
 * preview pieces (knight, rook, queen) rendered in that set's style alongside the
 * set label. Selecting a row persists the choice through the preference and closes
 * the dialog.
 *
 * <p>Unlike {@code PreferenceDialogFragmentCompat}, this resolves its preference
 * from the parent {@link androidx.preference.PreferenceFragmentCompat} by key,
 * avoiding the deprecated {@code setTargetFragment} mechanism.</p>
 */
public class PieceSetPreferenceDialog extends DialogFragment {

    /**
     * Representative pieces shown for each set — recognizable and distinctive.
     */
    private static final String[] PREVIEW_PIECE_CODES = {"wn", "wr", "wq"};

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

        PieceSetAdapter adapter = new PieceSetAdapter(context, sets);
        return new AlertDialog.Builder(context)
                .setTitle(preference.getTitle())
                .setNegativeButton(android.R.string.cancel, null)
                // Selecting a row commits immediately, so no positive button.
                .setSingleChoiceItems(adapter, selectedIndex, (dialog, which) -> {
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
     * Renders each set as [knight rook queen | label | radio]. Preview bitmaps are
     * loaded once per set+piece and cached so scrolling doesn't re-decode assets.
     */
    private static final class PieceSetAdapter extends BaseAdapter {

        private final Context context;
        private final String[] sets;
        private final Map<String, Bitmap> previewCache = new HashMap<>();

        PieceSetAdapter(Context context, String[] sets) {
            this.context = context;
            this.sets = sets;
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
            RadioButton radio = view.findViewById(R.id.piece_set_radio);

            bindPreview(view, R.id.piece_set_preview_knight, set, PREVIEW_PIECE_CODES[0]);
            bindPreview(view, R.id.piece_set_preview_rook, set, PREVIEW_PIECE_CODES[1]);
            bindPreview(view, R.id.piece_set_preview_queen, set, PREVIEW_PIECE_CODES[2]);
            label.setText(PieceSetPreference.capitalize(set));

            // The list's own single-choice state drives the radio; reflect it here.
            boolean checked = parent instanceof android.widget.ListView
                    && ((android.widget.ListView) parent).isItemChecked(position);
            radio.setChecked(checked);

            return view;
        }

        private void bindPreview(View row, int imageViewId, String set, String code) {
            ImageView imageView = row.findViewById(imageViewId);
            imageView.setImageBitmap(previewFor(set, code));
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
