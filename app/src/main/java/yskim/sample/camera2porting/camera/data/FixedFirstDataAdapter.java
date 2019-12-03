package yskim.sample.camera2porting.camera.data;

import android.app.Activity;
import android.view.View;

import yskim.sample.camera2porting.camera.ui.FilmStripView.DataAdapter;
import yskim.sample.camera2porting.camera.ui.FilmStripView.ImageData;
import yskim.sample.camera2porting.camera.util.Debug;

public class FixedFirstDataAdapter extends AbstractLocalDataAdapterWrapper implements DataAdapter.Listener {

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
    public FixedFirstDataAdapter(LocalDataAdapter wrappedAdapter, LocalData firstData) {
        super(wrappedAdapter);
        if (firstData == null) {
            throw new AssertionError("data is null");
        }
        mFirstData = firstData;
    }

    @Override
    public int getTotalNumber() {
        return (mAdapter.getTotalNumber() + 1);
    }

    @Override
    public View getView(Activity activity, int dataID) {
        Debug.logd(new Exception(), "dataID: " + dataID);
        if (dataID == 0) {
            Debug.logd(new Exception(), "dataID: " + dataID + ", mSuggestedWidth: " + mSuggestedWidth + ", mSuggestedHeight: " + mSuggestedHeight);
            return mFirstData.getView(activity, mSuggestedWidth, mSuggestedHeight, null, null);
        }
        return mAdapter.getView(activity, dataID - 1);
    }

    @Override
    public ImageData getImageData(int dataID) {
        Debug.logd(new Exception(), "dataID: " + dataID);
        if (dataID == 0) {
            Debug.logd(new Exception(), "dataID: " + dataID);
            return mFirstData;
        }
        return mAdapter.getImageData(dataID - 1);
    }

    @Override
    public void setListener(Listener listener) {
        Debug.logd(new Exception(), "");
        mListener = listener;
        mAdapter.setListener((listener == null) ? null : this);
        // The first data is always there. Thus, When the listener is set,
        // we should call listener.onDataLoaded().
        if (mListener != null) {
            Debug.logd(new Exception(), "");
            mListener.onDataLoaded();
        }
    }

    @Override
    public void onDataLoaded() {
        Debug.logd(new Exception(), "");
        if (mListener == null) {
            return;
        }
        mListener.onDataUpdated(new UpdateReporter() {
            @Override
            public boolean isDataRemoved(int dataID) {
                Debug.logd(new Exception(), "dataID: " + dataID);
                return false;
            }

            @Override
            public boolean isDataUpdated(int dataID) {
                Debug.logd(new Exception(), "dataID: " + dataID);
                return (dataID != 0);
            }
        });
    }

    @Override
    public void onDataUpdated(final UpdateReporter reporter) {
        Debug.logd(new Exception(), "");
        mListener.onDataUpdated(new UpdateReporter() {
            @Override
            public boolean isDataRemoved(int dataID) {
                Debug.logd(new Exception(), "dataID: " + dataID);
                return (dataID != 0) && reporter.isDataRemoved(dataID - 1);
            }

            @Override
            public boolean isDataUpdated(int dataID) {
                Debug.logd(new Exception(), "dataID: " + dataID);
                return (dataID != 0) && reporter.isDataUpdated(dataID - 1);
            }
        });
    }

    @Override
    public void onDataInserted(int dataID, ImageData data) {
        Debug.logd(new Exception(), "dataID: " + dataID + ", data.getWidth: " + data.getWidth() + ", data.getHeight: " + data.getHeight());
        mListener.onDataInserted(dataID + 1, data);
    }

    @Override
    public void onDataRemoved(int dataID, ImageData data) {
        Debug.logd(new Exception(), "dataID: " + dataID + ", data.getWidth: " + data.getWidth() + ", data.getHeight: " + data.getHeight());
        mListener.onDataRemoved(dataID + 1, data);
    }
}

