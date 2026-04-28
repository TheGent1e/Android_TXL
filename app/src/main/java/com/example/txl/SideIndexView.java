package com.example.txl;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.content.ContextCompat;
public class SideIndexView extends View {
    private static final String[] LETTERS = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","#"};
    private Paint paint;
    private Rect textBounds;
    private OnIndexSelectedListener listener;
    private int selectedIndex = -1;
    public interface OnIndexSelectedListener { void onIndexSelected(String letter); }
    public SideIndexView(Context context) { this(context, null); }
    public SideIndexView(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SideIndexView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }
    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(28);
        paint.setColor(Color.parseColor("#484F58"));
        textBounds = new Rect();
    }
    public void setOnIndexSelectedListener(OnIndexSelectedListener l) { this.listener = l; }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int h = getHeight(), w = getWidth(), sh = h / LETTERS.length;
        for (int i = 0; i < LETTERS.length; i++) {
            String letter = LETTERS[i];
            paint.getTextBounds(letter, 0, letter.length(), textBounds);
            float xp = (w - textBounds.width()) / 2f, yp = sh * i + sh;
            if (i == selectedIndex) {
                paint.setColor(Color.parseColor("#00E5FF"));
                paint.setTextSize(34);
                paint.setFakeBoldText(true);
            } else {
                paint.setColor(Color.parseColor("#484F58"));
                paint.setTextSize(26);
                paint.setFakeBoldText(false);
            }
            canvas.drawText(letter, xp, yp, paint);
        }
    }
    @Override public boolean onTouchEvent(MotionEvent e) {
        int a = e.getAction(); float y = e.getY(); int idx = (int)(y / getHeight() * LETTERS.length);
        if (a == MotionEvent.ACTION_DOWN || a == MotionEvent.ACTION_MOVE) {
            if (idx >= 0 && idx < LETTERS.length) { selectedIndex = idx; invalidate(); if (listener != null) listener.onIndexSelected(LETTERS[idx]); }
        } else if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) { selectedIndex = -1; invalidate(); }
        return true;
    }
}