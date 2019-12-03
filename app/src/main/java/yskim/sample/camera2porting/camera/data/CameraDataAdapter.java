package yskim.sample.camera2porting.camera.data;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;

import yskim.sample.camera2porting.camera.ui.FilmStripView.ImageData;

public class CameraDataAdapter implements LocalDataAdapter {
    @Override
    public ImageData getImageData(int dataID) {
        return null;
    }

    private static final String TAG = "CAM_CameraDataAdapter";

    private static final int DEFAULT_DECODE_SIZE = 1600;

    private Listener mListener;
    private Drawable mPlaceHolder;

    private int mSuggestedWidth = DEFAULT_DECODE_SIZE;
    private int mSuggestedHeight = DEFAULT_DECODE_SIZE;

    public CameraDataAdapter(Drawable placeHolder) {
        mPlaceHolder = placeHolder;
    }

    @Override
    public int getTotalNumber() {
        return 0;
    }

    @Override
    public void suggestViewSizeBound(int w, int h) {
        if (w <= 0 || h <= 0) {
            mSuggestedWidth  = mSuggestedHeight = DEFAULT_DECODE_SIZE;
        } else {
            mSuggestedWidth = (w < DEFAULT_DECODE_SIZE ? w : DEFAULT_DECODE_SIZE);
            mSuggestedHeight = (h < DEFAULT_DECODE_SIZE ? h : DEFAULT_DECODE_SIZE);
        }
    }

    @Override
    public View getView(Activity activity, int dataID) {
        return null;
    }

    @Override
    public void setListener(Listener listener) {
        mListener = listener;
        mListener.onDataLoaded();
    }
}

