package yskim.sample.camera2porting.data;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.View;

import yskim.sample.camera2porting.ui.FilmStripView.DataAdapter;
import yskim.sample.camera2porting.ui.FilmStripView.ImageData;

public class FixedFirstDataAdapter extends AbstractLocalDataAdapterWrapper
        implements DataAdapter.Listener {

    @SuppressWarnings("unused")
    private static final String TAG = "CAM_FixedFirstDataAdapter";

    private LocalData mFirstData;
    private Listener mListener;

    /**
     * Constructor.
     *
     * @param wrappedAdapter The {@link LocalDataAdapter} to be wrapped.
     * @param firstData      The {@link LocalData} to be placed at the first
     *                       position.
     */
    public FixedFirstDataAdapter(
            LocalDataAdapter wrappedAdapter,
            LocalData firstData) {
        super(wrappedAdapter);
        if (firstData == null) {
            throw new AssertionError("data is null");
        }
        mFirstData = firstData;
    }

    @Override
    public LocalData getLocalData(int dataID) {
        if (dataID == 0) {
            return mFirstData;
        }
        return mAdapter.getLocalData(dataID - 1);
    }

    @Override
    public void removeData(Context context, int dataID) {
        if (dataID > 0) {
            mAdapter.removeData(context, dataID - 1);
        }
    }

    @Override
    public int findDataByContentUri(Uri uri) {
        int pos = mAdapter.findDataByContentUri(uri);
        if (pos != -1) {
            return pos + 1;
        }
        return -1;
    }

    @Override
    public void updateData(int pos, LocalData data) {
        if (pos == 0) {
            mFirstData = data;
            if (mListener != null) {
                mListener.onDataUpdated(new UpdateReporter() {
                    @Override
                    public boolean isDataRemoved(int dataID) {
                        return false;
                    }

                    @Override
                    public boolean isDataUpdated(int dataID) {
                        return (dataID == 0);
                    }
                });
            }
        } else {
            mAdapter.updateData(pos - 1, data);
        }
    }

    @Override
    public int getTotalNumber() {
        return (mAdapter.getTotalNumber() + 1);
    }

    @Override
    public View getView(Activity activity, int dataID) {
        if (dataID == 0) {
            return mFirstData.getView(
                    activity, mSuggestedWidth, mSuggestedHeight, null, null);
        }
        return mAdapter.getView(activity, dataID - 1);
    }

    @Override
    public ImageData getImageData(int dataID) {
        if (dataID == 0) {
            return mFirstData;
        }
        return mAdapter.getImageData(dataID - 1);
    }

    @Override
    public void setListener(Listener listener) {
        mListener = listener;
        mAdapter.setListener((listener == null) ? null : this);
        // The first data is always there. Thus, When the listener is set,
        // we should call listener.onDataLoaded().
        if (mListener != null) {
            mListener.onDataLoaded();
        }
    }

    @Override
    public boolean canSwipeInFullScreen(int dataID) {
        if (dataID == 0) {
            return mFirstData.canSwipeInFullScreen();
        }
        return mAdapter.canSwipeInFullScreen(dataID - 1);
    }

    @Override
    public void onDataLoaded() {
        if (mListener == null) {
            return;
        }
        mListener.onDataUpdated(new UpdateReporter() {
            @Override
            public boolean isDataRemoved(int dataID) {
                return false;
            }

            @Override
            public boolean isDataUpdated(int dataID) {
                return (dataID != 0);
            }
        });
    }

    @Override
    public void onDataUpdated(final UpdateReporter reporter) {
        mListener.onDataUpdated(new UpdateReporter() {
            @Override
            public boolean isDataRemoved(int dataID) {
                return (dataID != 0) && reporter.isDataRemoved(dataID - 1);
            }

            @Override
            public boolean isDataUpdated(int dataID) {
                return (dataID != 0) && reporter.isDataUpdated(dataID - 1);
            }
        });
    }

    @Override
    public void onDataInserted(int dataID, ImageData data) {
        mListener.onDataInserted(dataID + 1, data);
    }

    @Override
    public void onDataRemoved(int dataID, ImageData data) {
        mListener.onDataRemoved(dataID + 1, data);
    }
}

