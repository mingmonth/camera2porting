package yskim.sample.camera2porting;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import yskim.sample.camera2porting.data.CameraDataAdapter;
import yskim.sample.camera2porting.data.CameraPreviewData;
import yskim.sample.camera2porting.data.FixedFirstDataAdapter;
import yskim.sample.camera2porting.data.LocalData;
import yskim.sample.camera2porting.data.LocalDataAdapter;
import yskim.sample.camera2porting.ui.FilmStripView;
import yskim.sample.camera2porting.util.CameraUtil;
import yskim.sample.camera2porting.util.Debug;

import static android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE;
import static yskim.sample.camera2porting.CameraManager.CameraOpenErrorCallback;

public class MainActivity extends Activity {

    public static final String ACTION_IMAGE_CAPTURE_SECURE = "android.media.action.IMAGE_CAPTURE_SECURE";

    /**
     * Request code from an activity we started that indicated that we do not
     * want to reset the view to the preview in onResume.
     */
    public static final int REQ_CODE_DONT_SWITCH_TO_PREVIEW = 142;

    private static final int HIDE_ACTION_BAR = 1;

    /** Whether onResume should reset the view to the preview. */
    private boolean mResetToPreviewOnResume = true;

    private LocalDataAdapter mWrappedDataAdapter;
    private int mCurrentModuleIndex;
    private View mCameraModuleRootView;
    private CameraPreviewData mCameraPreviewData;
    private CameraModule mCurrentModule;
    private LocalDataAdapter mDataAdapter;
    private FilmStripView mFilmStripView;
    private boolean mSecureCamera;
    private OnActionBarVisibilityListener mOnActionBarVisibilityListener = null;
    private FrameLayout mAboveFilmstripControlLayout;
    private static boolean sFirstStartAfterScreenOn = true;

    public static final String SECURE_CAMERA_EXTRA = "secure_camera";

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

    public interface OnActionBarVisibilityListener {
        public void onActionBarVisibilityChanged(boolean isVisible);
    }

    public void setOnActionBarVisibilityListener(OnActionBarVisibilityListener listener) {
        mOnActionBarVisibilityListener = listener;
    }

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

    /**
     * Launches an ACTION_EDIT intent for the given local data item.
     */
    public void launchEditor(LocalData data) {
        Intent intent = new Intent(Intent.ACTION_EDIT)
                .setDataAndType(data.getContentUri(), data.getMimeType())
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(intent, REQ_CODE_DONT_SWITCH_TO_PREVIEW);
        } catch (ActivityNotFoundException e) {
            startActivityForResult(Intent.createChooser(intent, null),
                    REQ_CODE_DONT_SWITCH_TO_PREVIEW);
        }
    }

    public boolean isSecureCamera() {
        return mSecureCamera;
    }

    private void openModule(CameraModule module) {
        module.init(this, mCameraModuleRootView);
        module.onResumeBeforeSuper();
        module.onResumeAfterSuper();
    }

    private void closeModule(CameraModule module) {
        module.onPauseBeforeSuper();
        module.onPauseAfterSuper();
        ((ViewGroup) mCameraModuleRootView).removeAllViews();
    }

//    private void setModuleFromIndex(int moduleIndex) {
//        mCurrentModuleIndex = moduleIndex;
//        mCurrentModule = new PhotoModule();
//    }

    public CameraOpenErrorCallback getCameraOpenErrorCallback() {
        return mCameraOpenErrorCallback;
    }

    protected void setResultEx(int resultCode, Intent data) {
        Debug.logd(new Exception(), "resultCode: " + resultCode + ", data: " + data);
        setResult(resultCode, data);
    }

    public static boolean isFirstStartAfterScreenOn() {
        return sFirstStartAfterScreenOn;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.camera_filmstrip);

        Intent intent = getIntent();
        String action = intent.getAction();
        if (INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(action)
                || ACTION_IMAGE_CAPTURE_SECURE.equals(action)) {
            mSecureCamera = true;
        } else {
            mSecureCamera = intent.getBooleanExtra(SECURE_CAMERA_EXTRA, false);
        }

        Debug.logd(new Exception(), "mSecureCamera : " + mSecureCamera);

        mAboveFilmstripControlLayout = (FrameLayout) findViewById(R.id.camera_above_filmstrip_layout);
        mAboveFilmstripControlLayout.setFitsSystemWindows(true);

        LayoutInflater inflater = getLayoutInflater();
        View rootLayout = inflater.inflate(R.layout.camera, null, false);
        mCameraModuleRootView = rootLayout.findViewById(R.id.camera_app_root);
        mCameraPreviewData = new CameraPreviewData(rootLayout, FilmStripView.ImageData.SIZE_FULL, FilmStripView.ImageData.SIZE_FULL);
        //mCameraPreviewData = new CameraPreviewData(rootLayout, 800, 480);   // landscape
        //mCameraPreviewData = new CameraPreviewData(rootLayout, 480, 800);   // portrait
        mWrappedDataAdapter = new FixedFirstDataAdapter(new CameraDataAdapter(new ColorDrawable(getResources().getColor(R.color.photo_placeholder))), mCameraPreviewData);
        mFilmStripView = (FilmStripView) findViewById(R.id.filmstrip_view);
        //mFilmStripView.setViewGap(getResources().getDimensionPixelSize(R.dimen.camera_film_strip_gap));

        mCurrentModuleIndex = 0;    //PHOTO_MODULE_INDEX
        mCurrentModule = new PhotoModule();
        mCurrentModule.init(this, mCameraModuleRootView);

        if (!mSecureCamera) {
            mDataAdapter = mWrappedDataAdapter;
            mFilmStripView.setDataAdapter(mDataAdapter);
            if (!isCaptureIntent()) {
                mDataAdapter.requestLoad(getContentResolver());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        openModule(mCurrentModule);
        setSwipingEnabled(true);

        if (mResetToPreviewOnResume) {
            // Go to the preview on resume.
            mFilmStripView.getController().goToFirstItem();
        }

        mResetToPreviewOnResume = true;

        if (!mSecureCamera) {
            // If it's secure camera, requestLoad() should not be called
            // as it will load all the data.
            mDataAdapter.requestLoad(getContentResolver());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        closeModule(mCurrentModule);
    }
}
