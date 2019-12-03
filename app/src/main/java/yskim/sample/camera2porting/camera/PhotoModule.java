package yskim.sample.camera2porting.camera;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.graphics.SurfaceTexture;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.provider.MediaStore;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;

import yskim.sample.camera2porting.CameraActivity;
import yskim.sample.camera2porting.R;
import yskim.sample.camera2porting.camera.CameraManager.CameraProxy;
import yskim.sample.camera2porting.camera.util.CameraUtil;
import yskim.sample.camera2porting.camera.util.Debug;

public class PhotoModule implements CameraModule, PhotoController {

    private static final String TAG = "CAM_PhotoModule";

    private static final int SETUP_PREVIEW = 1;
    private static final int FIRST_TIME_INIT = 2;
    private static final int CLEAR_SCREEN_DELAY = 3;
    private static final int CAMERA_OPEN_DONE = 8;
    private static final int OPEN_CAMERA_FAIL = 9;
    private static final int CAMERA_DISABLED = 10;

    // copied from Camera hierarchy
    private CameraActivity mActivity;
    private CameraProxy mCameraDevice;
    private int mCameraId;
    private Parameters mParameters;
    private boolean mPaused;

    private PhotoUI mUI;

    // The activity is going to switch to the specified camera id. This is
    // needed because texture copy is done in GL thread. -1 means camera is not
    // switching.
    private boolean mOpenCameraFail;
    private boolean mCameraDisabled;

    private static final int SCREEN_DELAY = 2 * 60 * 1000;

    // The degrees of the device rotated clockwise from its natural orientation.
    private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
    private ContentProviderClient mMediaProviderClient;
    private boolean mFirstTimeInitialized;
    private int mCameraState = PREVIEW_STOPPED;
    private ContentResolver mContentResolver;
    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();
    private final Handler mHandler = new MainHandler();

    // True if all the parameters needed to start preview is ready.
    private boolean mCameraPreviewParamsReady = false;

    /**
     * This Handler is used to post message back onto the main thread of the
     * application
     */
    private class MainHandler extends Handler {
        public MainHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CLEAR_SCREEN_DELAY: {
                    mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
                }
                case FIRST_TIME_INIT: {
                    initializeFirstTime();
                    break;
                }
                case CAMERA_OPEN_DONE: {
                    onCameraOpened();
                    break;
                }
                case OPEN_CAMERA_FAIL: {
                    mOpenCameraFail = true;
                    CameraUtil.showErrorAndFinish(mActivity, R.string.cannot_connect_camera);
                    break;
                }
                case CAMERA_DISABLED: {
                    mCameraDisabled = true;
                    CameraUtil.showErrorAndFinish(mActivity, R.string.camera_disabled);
                    break;
                }
            }
        }
    }

    @Override
    public void init(CameraActivity activity, View parent) {
        mActivity = activity;
        mUI = new PhotoUI(activity, this, parent);
        mCameraId = 0;
        Debug.logd(new Exception(), "mCameraId : " + mCameraId);
        mContentResolver = mActivity.getContentResolver();
    }

    @Override
    public void onPreviewUIReady() {
        startPreview();
    }

    @Override
    public void onPreviewUIDestroyed() {
        if (mCameraDevice == null) {
            return;
        }
        mCameraDevice.setPreviewTexture(null);
        stopPreview();
    }

    private void onCameraOpened() {
        // These depend on camera parameters.
        openCameraCommon();
    }

    // either open a new camera or switch cameras
    private void openCameraCommon() {
        mUI.onCameraOpened(mParameters);
    }

    private void keepMediaProviderInstance() {
        // We want to keep a reference to MediaProvider in camera's lifecycle.
        // TODO: Utilize mMediaProviderClient instance to replace
        // ContentResolver calls.
        if (mMediaProviderClient == null) {
            mMediaProviderClient = mContentResolver.acquireContentProviderClient(MediaStore.AUTHORITY);
        }
    }

    // Snapshots can only be taken after this is called. It should be called
    // once only. We could have done these things in onCreate() but we want to
    // make preview screen appear as soon as possible.
    private void initializeFirstTime() {
        if (mFirstTimeInitialized || mPaused) {
            return;
        }
        keepMediaProviderInstance();
        mFirstTimeInitialized = true;
        addIdleHandler();
    }

    // If the activity is paused and resumed, this method will be called in
    // onResume.
    private void initializeSecondTime() {
        keepMediaProviderInstance();
    }

    private void addIdleHandler() {
        MessageQueue queue = Looper.myQueue();
        queue.addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                return false;
            }
        });
    }

    private void setCameraState(int state) {
        mCameraState = state;
        switch (state) {
            case PhotoController.PREVIEW_STOPPED:
            case PhotoController.SNAPSHOT_IN_PROGRESS:
            case PhotoController.SWITCHING_CAMERA:
                break;
            case PhotoController.IDLE:
                break;
        }
    }

    @Override
    public void onOrientationChanged(int orientation) {
        // We keep the last known orientation. So if the user first orient
        // the camera then point the camera to floor or sky, we still have
        // the correct orientation.
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return;
        mOrientation = CameraUtil.roundOrientation(orientation, mOrientation);
    }

    @Override
    public void onStop() {
        if (mMediaProviderClient != null) {
            mMediaProviderClient.release();
            mMediaProviderClient = null;
        }
    }

    private boolean prepareCamera() {
        // We need to check whether the activity is paused before long
        // operations to ensure that onPause() can be done ASAP.
        mCameraDevice = CameraUtil.openCamera(
                mActivity, mCameraId, mHandler,
                mActivity.getCameraOpenErrorCallback());
        if (mCameraDevice == null) {
            Log.e(TAG, "Failed to open camera:" + mCameraId);
            return false;
        }
        mParameters = mCameraDevice.getParameters();
        mHandler.sendEmptyMessage(CAMERA_OPEN_DONE);
        mCameraPreviewParamsReady = true;
        startPreview();
        return true;
    }

    @Override
    public void onResumeAfterSuper() {
        onResumeTasks();
    }

    private void onResumeTasks() {
        if (mOpenCameraFail || mCameraDisabled) return;
        if (!prepareCamera()) {
            // Camera failure.
            return;
        }
        // If first time initialization is not finished, put it in the
        // message queue.
        if (!mFirstTimeInitialized) {
            mHandler.sendEmptyMessage(FIRST_TIME_INIT);
        } else {
            initializeSecondTime();
        }
        keepScreenOnAwhile();
    }

    @Override
    public void onPauseBeforeSuper() {
        mPaused = true;
    }

    @Override
    public void onPauseAfterSuper() {
        // Reset the focus first. Camera CTS does not guarantee that
        // cancelAutoFocus is allowed after preview stops.
        if (mCameraDevice != null && mCameraState != PREVIEW_STOPPED) {
            mCameraDevice.cancelAutoFocus();
        }
        // If the camera has not been opened asynchronously yet,
        // and startPreview hasn't been called, then this is a no-op.
        // (e.g. onResume -> onPause -> onResume).
        stopPreview();
        // Remove the messages and runnables in the queue.
        mHandler.removeCallbacksAndMessages(null);
        closeCamera();
        resetScreenOn();
    }

    @Override
    public boolean onBackPressed() {
        return mUI.onBackPressed();
    }

    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.setZoomChangeListener(null);
            mCameraDevice.setErrorCallback(null);
            CameraHolder.instance().release();
            mCameraDevice = null;
            setCameraState(PREVIEW_STOPPED);
        }
    }

    /** This can run on a background thread, post any view updates to MainHandler. */
    private void startPreview() {
        if (mPaused || mCameraDevice == null) {
            return;
        }

        // Any decisions we make based on the surface texture state
        // need to be protected.
        SurfaceTexture st = mUI.getSurfaceTexture();
        if (st == null) {
            Log.w(TAG, "startPreview: surfaceTexture is not ready.");
            return;
        }

        if (!mCameraPreviewParamsReady) {
            Log.w(TAG, "startPreview: parameters for preview is not ready.");
            return;
        }
        mCameraDevice.setErrorCallback(mErrorCallback);
        // ICS camera frameworks has a bug. Face detection state is not cleared 1589
        // after taking a picture. Stop the preview to work around it. The bug
        // was fixed in JB.
        if (mCameraState != PREVIEW_STOPPED) {
            stopPreview();
        }

        // Let UI set its expected aspect ratio
        mCameraDevice.setPreviewTexture(st);

        Debug.logd(new Exception(), "startPreview");
        mCameraDevice.startPreview();
    }

    @Override
    public void stopPreview() {
        if (mCameraDevice != null && mCameraState != PREVIEW_STOPPED) {
            Log.v(TAG, "stopPreview");
            mCameraDevice.stopPreview();
        }
        setCameraState(PREVIEW_STOPPED);
    }

    @Override
    public void onUserInteraction() {
        if (!mActivity.isFinishing()) keepScreenOnAwhile();
    }

    private void resetScreenOn() {
        mHandler.removeMessages(CLEAR_SCREEN_DELAY);
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void keepScreenOnAwhile() {
        mHandler.removeMessages(CLEAR_SCREEN_DELAY);
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler.sendEmptyMessageDelayed(CLEAR_SCREEN_DELAY, SCREEN_DELAY);
    }
}
