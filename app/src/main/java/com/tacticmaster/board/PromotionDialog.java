package com.tacticmaster.board;

import android.app.Dialog;
import android.content.Context;
import android.widget.ImageView;

import com.tacticmaster.R;

public class PromotionDialog {

    public interface PromotionListener {
        void onPieceSelected(char piece);
    }

    public static void show(Context context, ChessboardPieceManager chessboardPieceManager, boolean isWhite, float tileSize, PromotionListener listener) {
        Dialog dialog = new Dialog(context, R.style.TransparentDialog);
        dialog.setContentView(R.layout.promotion_dialog);

        int pieceSize = (int) tileSize;

        ImageView queen = dialog.findViewById(R.id.queen);
        ImageView rook = dialog.findViewById(R.id.rook);
        ImageView bishop = dialog.findViewById(R.id.bishop);
        ImageView knight = dialog.findViewById(R.id.knight);

        queen.getLayoutParams().width = pieceSize;
        queen.getLayoutParams().height = pieceSize;
        rook.getLayoutParams().width = pieceSize;
        rook.getLayoutParams().height = pieceSize;
        bishop.getLayoutParams().width = pieceSize;
        bishop.getLayoutParams().height = pieceSize;
        knight.getLayoutParams().width = pieceSize;
        knight.getLayoutParams().height = pieceSize;

        queen.setImageBitmap(chessboardPieceManager.getPieceBitmap(isWhite ? 'Q' : 'q'));
        rook.setImageBitmap(chessboardPieceManager.getPieceBitmap(isWhite ? 'R' : 'r'));
        bishop.setImageBitmap(chessboardPieceManager.getPieceBitmap(isWhite ? 'B' : 'b'));
        knight.setImageBitmap(chessboardPieceManager.getPieceBitmap(isWhite ? 'N' : 'n'));

        queen.setOnClickListener(v -> {
            listener.onPieceSelected('q');
            dialog.dismiss();
        });
        rook.setOnClickListener(v -> {
            listener.onPieceSelected('r');
            dialog.dismiss();
        });
        bishop.setOnClickListener(v -> {
            listener.onPieceSelected('b');
            dialog.dismiss();
        });
        knight.setOnClickListener(v -> {
            listener.onPieceSelected('n');
            dialog.dismiss();
        });

        dialog.show();
    }
}