package so.brendan.robust.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * A button that allows a text badge to be written to the right-hand side.
 *
 * This is similar to menu buttons in the drawer of the Gmail application.
 */
public class MenuButton extends Button {
    private TextViewDrawable mBadge;

    public MenuButton(Context context) {
        super(context);
        init();
    }

    public MenuButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MenuButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mBadge = new TextViewDrawable(this, getBackground());

        // There's a 'feature' where padding gets reset when setting background...
        int paddingBottom = getPaddingBottom();
        int paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();

        setBackground(mBadge);
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    /**
     * Sets the badge text.
     *
     * @param text
     */
    public void setBadge(CharSequence text) {
        mBadge.setText(text);
    }

    /**
     * Implements the required boilerplate to write the text to the background of the button.
     */
    private class TextViewDrawable extends LayerDrawable {
        private Paint mPaint;
        private Button mParent;
        private ColorStateList mColors;
        private Rect mBounds;
        private String mText = "";

        public TextViewDrawable(Button button, Drawable drawable) {
            super(new Drawable[]{drawable});

            mParent = button;

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setTextAlign(Paint.Align.RIGHT);

            mPaint.setTextSize(mParent.getTextSize() * 0.7f);
            mPaint.setTypeface(Typeface.DEFAULT);

            mColors = button.getTextColors();
            mBounds = new Rect();
        }

        public void setText(CharSequence text) {
            mText = text.toString();
            mPaint.getTextBounds(mText, 0, mText.length(), mBounds);
            invalidateSelf();
        }

        @Override
        protected boolean onStateChange(int[] state) {
            invalidateSelf();
            return super.onStateChange(state);
        }

        @Override
        public boolean isStateful() {
            return true;
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);

            // Gives enough space that the text doesn't spew out the side.
            float x = canvas.getWidth() - mPaint.getTextSize() * 1.25f;

            int[] stateSet = getState();

            int color = mColors.getColorForState(stateSet, 0xff000000);
            mPaint.setColor(color);
            mPaint.setAlpha(127);

            canvas.drawText(mText, x, mParent.getBaseline(), mPaint);
        }
    }
}
