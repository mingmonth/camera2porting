package yskim.sample.camera2porting;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import yskim.sample.camera2porting.data.CameraDataAdapter;
import yskim.sample.camera2porting.data.CameraPreviewData;
import yskim.sample.camera2porting.data.FixedFirstDataAdapter;
import yskim.sample.camera2porting.data.LocalDataAdapter;
import yskim.sample.camera2porting.ui.FilmStripView;
import yskim.sample.camera2porting.util.CameraUtil;
import yskim.sample.camera2porting.util.Debug;

import static yskim.sample.camera2porting.CameraManager.CameraOpenErrorCallback;

public class MainActivity extends Activity {

    private LocalDataAdapter mWrappedDataAdapter;
    private View mCameraModuleRootView;
    private CameraPreviewData mCameraPreviewData;
    private CameraModule mCurrentModule;
    private LocalDataAdapter mDataAdapter;
    private FilmStripView mFilmStripView;
    private FrameLayout mAboveFilmstripControlLayout;

    private CameraOpenErrorCallback mCameraOpenErrorCallback =
            new CameraOpenErrorCallback() {
                @Override
                public void onCameraDisabled(int cameraId) {
                    CameraUtil.showErrorAndFinish(MainActivity.this, R.string.camera_disabled);
                }

                @Override
                public void onDeviceOpenFailure(int cameraId) {
                    CameraUtil.showErrorAndFinish(MainActivity.this, R.string.cannot_connect_camera);
                }

                @Override
                public void onReconnectionFailure(CameraManager mgr) {
                    CameraUtil.showErrorAndFinish(MainActivity.this, R.string.cannot_connect_camera);
                }
            };

    /**
     * Enable/disable swipe-to-filmstrip. Will always disable swipe if in
     * capture intent.
     *
     * @param enable {@code true} to enable swipe.
     */
    public void setSwipingEnabled(boolean enable) {
        if (isCaptureIntent()) {
            mCameraPreviewData.lockPreview(true);
        } else {
            mCameraPreviewData.lockPreview(!enable);
        }
    }

    private boolean isCaptureIntent() {
        if (MediaStore.ACTION_VIDEO_CAPTURE.equals(getIntent().getAction())
                || MediaStore.ACTION_IMAGE_CAPTURE.equals(getIntent().getAction())
                || MediaStore.ACTION_IMAGE_CAPTURE_SECURE.equals(getIntent().getAction())) {
            return true;
        } else {
            return false;
        }
    }

    private void openModule(CameraModule module) {
        module.init(this, mCameraModuleRootView);
        module.onResumeAfterSuper();
    }

    private void closeModule(CameraModule module) {
        module.onPauseBeforeSuper();
        module.onPauseAfterSuper();
        ((ViewGroup) mCameraModuleRootView).removeAllViews();
    }

    public CameraOpenErrorCallback getCameraOpenErrorCallback() {
        return mCameraOpenErrorCallback;
    }

    protected void setResultEx(int resultCode, Intent data) {
        Debug.logd(new Exception(), "resultCode: " + resultCode + ", data: " + data);
        setResult(resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_filmstrip);

        mAboveFilmstripControlLayout = (FrameLayout) findViewById(R.id.camera_above_filmstrip_layout);
        mAboveFilmstripControlLayout.setFitsSystemWindows(true);

        LayoutInflater inflater = getLayoutInflater();
        View rootLayout = inflater.inflate(R.layout.camera, null, false);
        mCameraModuleRootView = rootLayout.findViewById(R.id.camera_app_root);
        mCameraPreviewData = new CameraPreviewData(rootLayout, FilmStripView.ImageData.SIZE_FULL, FilmStripView.ImageData.SIZE_FULL);
        mWrappedDataAdapter = new FixedFirstDataAdapter(new CameraDataAdapter(new ColorDrawable(getResources().getColor(R.color.photo_placeholder))), mCameraPreviewData);
        mFilmStripView = (FilmStripView) findViewById(R.id.filmstrip_view);

        mCurrentModule = new PhotoModule();
        mCurrentModule.init(this, mCameraModuleRootView);

        mDataAdapter = mWrappedDataAdapter;
        mFilmStripView.setDataAdapter(mDataAdapter);
        if (!isCaptureIntent()) {
            mDataAdapter.requestLoad(getContentResolver());
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        openModule(mCurrentModule);
        setSwipingEnabled(true);
        mFilmStripView.getController().goToFirstItem();
        mDataAdapter.requestLoad(getContentResolver());
    }

    @Override
    public void onPause() {
        super.onPause();
        closeModule(mCurrentModule);
    }
}
