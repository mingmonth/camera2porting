package yskim.sample.camera2porting.data;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

public abstract class AbstractLocalDataAdapterWrapper implements LocalDataAdapter {

    protected final LocalDataAdapter mAdapter;
    protected int mSuggestedWidth;
    protected int mSuggestedHeight;

    /**
     * Constructor.
     *
     * @param wrappedAdapter  The {@link LocalDataAdapter} to be wrapped.
     */
    AbstractLocalDataAdapterWrapper(LocalDataAdapter wrappedAdapter) {
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

    @Override
    public void requestLoad(ContentResolver resolver) {
        mAdapter.requestLoad(resolver);
    }

    @Override
    public void addNewVideo(ContentResolver resolver, Uri uri) {
        mAdapter.addNewVideo(resolver, uri);
    }

    @Override
    public void addNewPhoto(ContentResolver resolver, Uri uri) {
        mAdapter.addNewPhoto(resolver, uri);
    }

    @Override
    public void insertData(LocalData data) {
        mAdapter.insertData(data);
    }

    @Override
    public void flush() {
        mAdapter.flush();
    }

    @Override
    public boolean executeDeletion(Context context) {
        return mAdapter.executeDeletion(context);
    }

    @Override
    public boolean undoDataRemoval() {
        return mAdapter.undoDataRemoval();
    }

    @Override
    public void refresh(ContentResolver resolver, Uri uri) {
        mAdapter.refresh(resolver, uri);
    }
}

