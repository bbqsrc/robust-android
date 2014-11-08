package so.brendan.robust.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

/**
 * Fragment for retaining a value between lifecycle changes.
 */
public class RetainFragment<T> extends Fragment {
    private T mValue;

    /**
     * Tries to find a RetainFragment for provided tag, otherwise creates a new one.
     *
     * @param fm
     * @param tag
     * @return
     */
    public static RetainFragment findOrCreate(FragmentManager fm, String tag) {
        RetainFragment fragment = (RetainFragment) fm.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = new RetainFragment();
            fm.beginTransaction().add(fragment, tag).commit();
        }
        return fragment;
    }

    public RetainFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setValue(T value) {
        mValue = value;
    }

    public T getValue() {
        return mValue;
    }

}
