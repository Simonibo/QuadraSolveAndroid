package de.jamesbeans.quadrasolve;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextPaint;
import android.util.AttributeSet;

import java.util.List;

public class NumpadKeyboardView extends KeyboardView {
    private Paint paint,  paintpressed;
    private final TextPaint tp = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
    private Bitmap retu, backb;
    private int retuposx, retuposy, backX, backY;
    boolean initialized;
    final String[] texts = {"1", "2", "3", "", "4", "5", "6", "", "7", "8", "9", "−", "0", ""};
    private final int[] xs = new int[14];
    private final int[] ys = new int[14];

    public NumpadKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NumpadKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paintpressed = new Paint();
        paint.setColor(ResourcesCompat.getColor(getResources(), R.color.keyboardbackground, null));
        paintpressed.setColor(Color.LTGRAY);
        tp.setColor(Color.WHITE);
        tp.setTextSize(90);
        tp.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final Keyboard kb = getKeyboard();
        final List<Keyboard.Key> keys = kb.getKeys();
        final Keyboard.Key enterkey = keys.get(7);
        final Keyboard.Key back = keys.get(3);
        final int sz = keys.size();

        if (!initialized) {
            initialized = true;
            retu = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_subdirectory_arrow_left_white_48dp);
            backb = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_backspace_white_24dp);
            retuposx = enterkey.x + ((enterkey.width - retu.getWidth()) >> 1);
            retuposy = enterkey.y + ((kb.getHeight() - back.height - retu.getHeight()) >> 1);
            backX = back.x + ((back.width - backb.getWidth()) >> 1);
            backY = back.y + ((back.height - backb.getHeight()) >> 1);
            final float dist = -tp.getFontMetrics().ascent * 0.8f;
            for(int i = 0; i < sz; ++i) {
                final Keyboard.Key k = keys.get(i);
                xs[i] = k.x + (k.width >> 1);
                ys[i] = (int) (k.y + (k.height + dist) / 2);
            }
        }
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
        for (int i = 0; i < sz; ++i) {
            final Keyboard.Key k = keys.get(i);
            if(k.pressed) {
                canvas.drawRect(k.x, k.y, (k.x + k.width), (k.y + k.height), paintpressed);
            }
            canvas.drawText(texts[i], xs[i], ys[i], tp);
        }
        canvas.drawBitmap(retu, retuposx, retuposy, paint);
        canvas.drawBitmap(backb, backX, backY, paint);
    }
}
