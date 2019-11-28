package yskim.sample.camera2porting.data;

import yskim.sample.camera2porting.ui.FilmStripView;

public abstract class AbstractLocalDataAdapterWrapper implements FilmStripView.DataAdapter {

    protected final FilmStripView.DataAdapter mAdapter;
    protected int mSuggestedWidth;
    protected int mSuggestedHeight;

    /**
     * Constructor.
     *
     * @param wrappedAdapter  The {@link LocalDataAdapter} to be wrapped.
     */
    AbstractLocalDataAdapterWrapper(FilmStripView.DataAdapter wrappedAdapter) {
        if (wrappedAdapter == null) {
            throw new AssertionError("data adapter is null");
        }
        mAdapter = wrappedAdapter;
    }

    @Override
    public void suggestViewSizeBound(int w, int h) {
        mSuggestedWidth = w;
        mSuggestedHeight = h;
        mAdapter.suggestViewSizeBound(w, h);
    }

    @Override
    public void setListener(Listener listener) {
        mAdapter.setListener(listener);
    }
}

