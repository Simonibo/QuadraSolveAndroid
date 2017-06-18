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
    Paint paint, paintpressed;
    Bitmap retu;
    public NumpadKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NumpadKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        paint = new Paint();
        paintpressed = new Paint();
        paint.setColor(Color.LTGRAY);
        paintpressed.setColor(Color.DKGRAY);
        retu = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_keyboard_return_black_24dp);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Keyboard.Key enterkey = getKeyboard().getKeys().get(7);
        if(enterkey.pressed) {
            canvas.drawRect(enterkey.x, enterkey.y, enterkey.x + enterkey.width, enterkey.y + enterkey.height, paintpressed);
            canvas.drawBitmap(retu, enterkey.x, enterkey.y, paintpressed);
        } else {
            canvas.drawRect(enterkey.x, enterkey.y, enterkey.x + enterkey.width, enterkey.y + enterkey.height, paint);
            canvas.drawBitmap(retu, enterkey.x, enterkey.y, paint);
        }
    }
}
