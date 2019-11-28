package yskim.sample.camera2porting;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;

import yskim.sample.camera2porting.util.Debug;

public class PhotoUI implements TextureView.SurfaceTextureListener {
    private static final String TAG = "CAM_UI";
    private MainActivity mActivity;
    private PhotoController mController;

    private View mRootView;
    private SurfaceTexture mSurfaceTexture;

    private int mPreviewWidth = 0;
    private int mPreviewHeight = 0;
    private float mSurfaceTextureUncroppedWidth;
    private float mSurfaceTextureUncroppedHeight;

    private TextureView mTextureView;
    private Matrix mMatrix = null;
    private float mAspectRatio = 4f / 3f;
    private final Object mSurfaceTextureLock = new Object();

    private OnLayoutChangeListener mLayoutListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right,
                                   int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            int width = right - left;
            int height = bottom - top;
            if (mPreviewWidth != width || mPreviewHeight != height) {
                mPreviewWidth = width;
                mPreviewHeight = height;
                setTransformMatrix(width, height);
            }
        }
    };

    public PhotoUI(MainActivity activity, PhotoController controller, View parent) {
        mActivity = activity;
        mController = controller;
        mRootView = parent;

        mActivity.getLayoutInflater().inflate(R.layout.photo_module,
                (ViewGroup) mRootView, true);
        // display the view
        mTextureView = (TextureView) mRootView.findViewById(R.id.preview_content);
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.addOnLayoutChangeListener(mLayoutListener);
    }

    public void updatePreviewAspectRatio(float aspectRatio) {
        if (aspectRatio <= 0) {
            Log.e(TAG, "Invalid aspect ratio: " + aspectRatio);
            return;
        }
        if (aspectRatio < 1f) {
            aspectRatio = 1f / aspectRatio;
        }

        if (mAspectRatio != aspectRatio) {
            mAspectRatio = aspectRatio;
            // Update transform matrix with the new aspect ratio.
            if (mPreviewWidth != 0 && mPreviewHeight != 0) {
                setTransformMatrix(mPreviewWidth, mPreviewHeight);
            }
        }
    }

    private void setTransformMatrix(int width, int height) {
        mMatrix = mTextureView.getTransform(mMatrix);
        float scaleX = 1f, scaleY = 1f;
        float scaledTextureWidth, scaledTextureHeight;
        if (width > height) {
            scaledTextureWidth = Math.max(width,
                    (int) (height * mAspectRatio));
            scaledTextureHeight = Math.max(height,
                    (int)(width / mAspectRatio));
        } else {
            scaledTextureWidth = Math.max(width,
                    (int) (height / mAspectRatio));
            scaledTextureHeight = Math.max(height,
                    (int) (width * mAspectRatio));
        }

        if (mSurfaceTextureUncroppedWidth != scaledTextureWidth ||
                mSurfaceTextureUncroppedHeight != scaledTextureHeight) {
            mSurfaceTextureUncroppedWidth = scaledTextureWidth;
            mSurfaceTextureUncroppedHeight = scaledTextureHeight;
        }
        scaleX = scaledTextureWidth / width;
        scaleY = scaledTextureHeight / height;
        mMatrix.setScale(scaleX, scaleY, (float) width / 2, (float) height / 2);
        mTextureView.setTransform(mMatrix);

        // Calculate the new preview rectangle.
        RectF previewRect = new RectF(0, 0, width, height);
        Debug.logd(new Exception(), "width: " + width + ", height: " + height);
        mMatrix.mapRect(previewRect);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        synchronized (mSurfaceTextureLock) {
            Log.v(TAG, "SurfaceTexture ready.");
            mSurfaceTexture = surface;
            mController.onPreviewUIReady();
            // Workaround for b/11168275, see b/10981460 for more details
            if (mPreviewWidth != 0 && mPreviewHeight != 0) {
                // Re-apply transform matrix for new surface texture
                setTransformMatrix(mPreviewWidth, mPreviewHeight);
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        synchronized (mSurfaceTextureLock) {
            mSurfaceTexture = null;
            mController.onPreviewUIDestroyed();
            Log.w(TAG, "SurfaceTexture destroyed");
            return true;
        }
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Make sure preview cover is hidden if preview data is available.
    }

    public View getRootView() {
        return mRootView;
    }

    public void onCameraOpened(Camera.Parameters params) {
    }

    public boolean onBackPressed() {
        // In image capture mode, back button should:
        // 1) if there is any popup, dismiss them, 2) otherwise, get out of
        // image capture
        if (!mController.isCameraIdle()) {
            // ignore backs while we're taking a picture
            return true;
        } else {
            return false;
        }
    }

    public boolean isShutterPressed() {
        return false;
    }

    /**
     * Enables or disables the shutter button.
     */
    public void enableShutter(boolean enabled) {
    }

//    public void pressShutterButton() {}

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public boolean isCountingDown() {
        return false;
    }

//    @Override
//    public boolean hasFaces() {
//        return false;
//    }
//
//    @Override
//    public void clearFocus() {}
//
//    @Override
//    public void setFocusPosition(int x, int y) {}
//
//    @Override
//    public void onFocusStarted() {}
//
//    @Override
//    public void onFocusSucceeded(boolean timeOut) {}
//
//    @Override
//    public void onFocusFailed(boolean timeOut) {}
//
//    @Override
//    public void pauseFaceDetection() {}
//
//    @Override
//    public void resumeFaceDetection() {}
}
