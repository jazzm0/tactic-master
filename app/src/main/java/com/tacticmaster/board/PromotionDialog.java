package com.tacticmaster.board;

import android.app.Dialog;
import android.content.Context;
import android.widget.ImageView;

import com.tacticmaster.R;

public class PromotionDialog {

    public interface PromotionListener {
        void onPieceSelected(char piece);
    }

    public static void show(Context context, ChessboardPieceManager bitmapManager, boolean isWhite, PromotionListener listener) {
        Dialog dialog = new Dialog(context, R.style.TransparentDialog);
        dialog.setContentView(R.layout.promotion_dialog);

        ImageView queenView = dialog.findViewById(R.id.queen);
        ImageView rookView = dialog.findViewById(R.id.rook);
        ImageView bishopView = dialog.findViewById(R.id.bishop);
        ImageView knightView = dialog.findViewById(R.id.knight);

        queenView.setImageBitmap(bitmapManager.getPieceBitmap(isWhite ? 'Q' : 'q'));
        rookView.setImageBitmap(bitmapManager.getPieceBitmap(isWhite ? 'R' : 'r'));
        bishopView.setImageBitmap(bitmapManager.getPieceBitmap(isWhite ? 'B' : 'b'));
        knightView.setImageBitmap(bitmapManager.getPieceBitmap(isWhite ? 'N' : 'n'));

        queenView.setOnClickListener(v -> {
            listener.onPieceSelected('q');
            dialog.dismiss();
        });
        rookView.setOnClickListener(v -> {
            listener.onPieceSelected('r');
            dialog.dismiss();
        });
        bishopView.setOnClickListener(v -> {
            listener.onPieceSelected('b');
            dialog.dismiss();
        });
        knightView.setOnClickListener(v -> {
            listener.onPieceSelected('n');
            dialog.dismiss();
        });

        dialog.show();
    }
}
