package de.jamesbeans.quadrasolve;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

import java.util.List;

public class NumpadKeyboardView extends KeyboardView {
    private Paint paint;
    private Paint paintpressed;
    private Bitmap retu;
    private int posx;
    private int posy;
    private boolean initialized;

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
        paint.setColor(Color.rgb(236, 239, 241));
        paintpressed.setColor(Color.rgb(219, 224, 225));
        retu = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_subdirectory_arrow_left_black_48dp);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Keyboard kb = getKeyboard();
        List<Keyboard.Key> keys = kb.getKeys();
        Keyboard.Key enterkey = keys.get(7);
        Keyboard.Key back = keys.get(3);

        if(!initialized) {
            initialized = true;
            posx = enterkey.x + (int) ((double) (enterkey.width - retu.getWidth()) / 2.0);
            posy = enterkey.y + (int) ((double) (kb.getHeight() - back.height - retu.getHeight()) / 2.0);
        }

        if(enterkey.pressed) {
            canvas.drawRect((float) enterkey.x, (float) enterkey.y, (float) (enterkey.x + enterkey.width), (float) (enterkey.y + enterkey.height), paintpressed);
        } else {
            canvas.drawRect((float) enterkey.x, (float) enterkey.y, (float) (enterkey.x + enterkey.width), (float) (enterkey.y + enterkey.height), paint);
        }
        canvas.drawBitmap(retu, (float) posx, (float) posy, paint);
    }
}
