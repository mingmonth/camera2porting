package yskim.sample.camera2porting;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import yskim.sample.camera2porting.data.CameraDataAdapter;
import yskim.sample.camera2porting.data.CameraPreviewData;
import yskim.sample.camera2porting.data.FixedFirstDataAdapter;
import yskim.sample.camera2porting.ui.FilmStripView;
import yskim.sample.camera2porting.util.CameraUtil;
import yskim.sample.camera2porting.util.Debug;

import static yskim.sample.camera2porting.CameraManager.CameraOpenErrorCallback;

public class MainActivity extends Activity implements View.OnClickListener, ViewGroup.OnTouchListener {

    PopupWindow mPopupWindow;
    private View mCameraModuleRootView;
    private CameraPreviewData mCameraPreviewData;
    private CameraModule mCurrentModule;
    private FilmStripView mFilmStripView;
    private FrameLayout mAboveFilmstripControlLayout;
    Button mTestButton;
    private FilmStripView.DataAdapter mWrappedDataAdapter;

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

        mFilmStripView.setOnTouchListener(this);

        mTestButton = (Button) findViewById(R.id.test_button);
        mTestButton.setOnClickListener(this);

        mCurrentModule = new PhotoModule();
        mCurrentModule.init(this, mCameraModuleRootView);

        mFilmStripView.setDataAdapter(mWrappedDataAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        openModule(mCurrentModule);
    }

    @Override
    public void onPause() {
        super.onPause();
        closeModule(mCurrentModule);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test_button:
                Debug.logd(new Exception(), "TEST BUTTON CLICKED!!!");
                createTestPopup();
                break;
            default:
                Debug.logd(new Exception(), "DEFAULT!!!");
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.filmstrip_view:
                Debug.logd(new Exception(), "MAIN TOUCH EVENT!!!");
                break;
            default:
                Debug.logd(new Exception(), "MAIN TOUCH DEFAULT!!!");
                break;
        }
        return false;
    }

    private void createTestPopup() {
        View popupView = getLayoutInflater().inflate(R.layout.dialog_layout, null);
        mPopupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        Button cancel = (Button) popupView.findViewById(R.id.Cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });

        Button ok = (Button) popupView.findViewById(R.id.Ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Debug.logd(new Exception(), "OKAY BUTTON CLICKED!!!");
            }
        });
    }
}
