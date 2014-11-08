package so.brendan.robust.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ListView;

import so.brendan.robust.listeners.OnOverScrolledListener;
import so.brendan.robust.utils.Constants;

/**
 * Allows scroll to refresh on a fairly ordinary ListView.
 *
 * Adds the <code>setOnOverScrolledListener</code> method to allow listening for overscroll events.
 */
public class RefreshableListView extends ListView {
    private static final String TAG = Constants.createTag(RefreshableListView.class);

    // Beautiful magic numbers. Sets how far you can drag beyond the top boundary of the list.
    private static final int MAX_Y_OVERSCROLL_DISTANCE = 10;

    private int mMaxYOverscrollDistance;
    private OnOverScrolledListener mListener;

    public RefreshableListView(Context context) {
        super(context);
        init(context);
    }

    public RefreshableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RefreshableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * Sets the maximum overscroll distance in a platform-independent manner.
     *
     * @param context
     */
    private void init(Context context) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float density = metrics.density;

        mMaxYOverscrollDistance = (int) (density * MAX_Y_OVERSCROLL_DISTANCE);
    }

    /**
     * Sets the <code>OnOverScrolledListener</code> for this instance.
     *
     * @param listener
     */
    public void setOnOverScrolledListener(OnOverScrolledListener listener) {
        mListener = listener;
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        // Allow overscroll on top, but not bottom, of list.
        scrollY = Math.min(0, scrollY);

        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (mListener != null) {
            mListener.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        }
    }

    /**
     * Caps <code>overScrollBy</code> so that one can only overscroll at the top of the list.
     *
     * @param deltaX
     * @param deltaY
     * @param scrollX
     * @param scrollY
     * @param scrollRangeX
     * @param scrollRangeY
     * @param maxOverScrollX
     * @param maxOverScrollY
     * @param isTouchEvent
     * @return
     */
    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY,
                                   int scrollRangeX, int scrollRangeY,
                                   int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
                scrollRangeY, maxOverScrollX, mMaxYOverscrollDistance, isTouchEvent);
    }
}
