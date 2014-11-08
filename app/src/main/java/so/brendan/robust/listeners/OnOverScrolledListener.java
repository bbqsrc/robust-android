package so.brendan.robust.listeners;

/**
 * A listener for OverScrolled on list views.
 */
public interface OnOverScrolledListener {
    /**
     * Listens for the overscroll event.
     *
     * @param scrollX
     * @param scrollY
     * @param clampedX
     * @param clampedY
     */
    public void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY);
}
