package com.tacticmaster.board;

import static java.util.Objects.isNull;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.tacticmaster.R;

public class AntiAliasedImageView extends AppCompatImageView {

    private Paint paint;
    private Bitmap bitmap;

    public AntiAliasedImageView(Context context) {
        super(context);
        init();
    }

    public AntiAliasedImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AntiAliasedImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_white_turn);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isNull(bitmap)) {
            canvas.drawBitmap(bitmap, 0, 0, paint);
        }
    }
}