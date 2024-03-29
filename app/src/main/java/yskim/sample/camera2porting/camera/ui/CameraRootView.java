package yskim.sample.camera2porting.camera.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import yskim.sample.camera2porting.camera.util.ApiHelper;
import yskim.sample.camera2porting.camera.util.CameraUtil;
import yskim.sample.camera2porting.camera.util.Debug;

@SuppressLint("NewApi")
public class CameraRootView extends FrameLayout {

    private int mTopMargin = 0;
    private int mBottomMargin = 0;
    private int mLeftMargin = 0;
    private int mRightMargin = 0;
    private final Rect mCurrentInsets = new Rect(0, 0, 0, 0);
    private int mOffset = 0;
    private Object mDisplayListener;

    public CameraRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDisplayListener();
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        return false;
    }

    public void initDisplayListener() {
        if (ApiHelper.HAS_DISPLAY_LISTENER) {
            mDisplayListener = new DisplayListener() {
                @Override
                public void onDisplayAdded(int arg0) {}
                @Override
                public void onDisplayChanged(int arg0) {
                }
                @Override
                public void onDisplayRemoved(int arg0) {}
            };
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (ApiHelper.HAS_DISPLAY_LISTENER) {
            ((DisplayManager) getContext().getSystemService(Context.DISPLAY_SERVICE)).registerDisplayListener((DisplayListener) mDisplayListener, null);
        }
    }

    @Override
    public void onDetachedFromWindow () {
        super.onDetachedFromWindow();
        if (ApiHelper.HAS_DISPLAY_LISTENER) {
            ((DisplayManager) getContext().getSystemService(Context.DISPLAY_SERVICE)).unregisterDisplayListener((DisplayListener) mDisplayListener);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int rotation = CameraUtil.getDisplayRotation((Activity) getContext());
        // all the layout code assumes camera device orientation to be portrait
        // adjust rotation for landscape
        int orientation = getResources().getConfiguration().orientation;
        int camOrientation = (rotation % 180 == 0) ? Configuration.ORIENTATION_PORTRAIT
                : Configuration.ORIENTATION_LANDSCAPE;
        if (camOrientation != orientation) {
            rotation = (rotation + 90) % 360;
        }
        // calculate margins
        mLeftMargin = 0;
        mRightMargin = 0;
        mBottomMargin = 0;
        mTopMargin = 0;

        switch (rotation) {
            case 0:
                mBottomMargin += mOffset;
                break;
            case 90:
                mRightMargin += mOffset;
                break;
            case 180:
                mTopMargin += mOffset;
                break;
            case 270:
                mLeftMargin += mOffset;
                break;
        }
        if (mCurrentInsets != null) {
            if (mCurrentInsets.right > 0) {
                // navigation bar on the right
                mRightMargin = mRightMargin > 0 ? mRightMargin : mCurrentInsets.right;
            } else {
                // navigation bar on the bottom
                mBottomMargin = mBottomMargin > 0 ? mBottomMargin : mCurrentInsets.bottom;
            }
        }
        // make sure all the children are resized
        super.onMeasure(widthMeasureSpec - mLeftMargin - mRightMargin,
                heightMeasureSpec - mTopMargin - mBottomMargin);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        r -= l;
        b -= t;
        l = 0;
        t = 0;
        int orientation = getResources().getConfiguration().orientation;
        Debug.logd(new Exception(), "getChildCount() : " + getChildCount());
        // Lay out children
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            v.layout(l + mLeftMargin, t + mTopMargin, r - mRightMargin, b - mBottomMargin);
        }
    }
}

