package yskim.sample.camera2porting;

import android.animation.Animator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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

    private static final String TAG = "CAM_Activity";
    public static final String ACTION_IMAGE_CAPTURE_SECURE = "android.media.action.IMAGE_CAPTURE_SECURE";
    public static final String ACTION_TRIM_VIDEO = "com.android.camera.action.TRIM";

    /**
     * Request code from an activity we started that indicated that we do not
     * want to reset the view to the preview in onResume.
     */
    public static final int REQ_CODE_DONT_SWITCH_TO_PREVIEW = 142;

    public static final int REQ_CODE_GCAM_DEBUG_POSTCAPTURE = 999;

    private static final int HIDE_ACTION_BAR = 1;
    private static final long SHOW_ACTION_BAR_TIMEOUT_MS = 3000;

    /** Whether onResume should reset the view to the preview. */
    private boolean mResetToPreviewOnResume = true;

    // Supported operations at FilmStripView. Different data has different
    // set of supported operations.
    private static final int SUPPORT_DELETE = 1 << 0;
    private static final int SUPPORT_ROTATE = 1 << 1;
    private static final int SUPPORT_INFO = 1 << 2;
    private static final int SUPPORT_CROP = 1 << 3;
    private static final int SUPPORT_SETAS = 1 << 4;
    private static final int SUPPORT_EDIT = 1 << 5;
    private static final int SUPPORT_TRIM = 1 << 6;
    private static final int SUPPORT_SHARE = 1 << 7;
    private static final int SUPPORT_SHARE_PANORAMA360 = 1 << 8;
    private static final int SUPPORT_SHOW_ON_MAP = 1 << 9;
    private static final int SUPPORT_ALL = 0xffffffff;

    //private PanoramaStitchingManager mPanoramaManager;
    private LocalDataAdapter mWrappedDataAdapter;
    private int mCurrentModuleIndex;
    private View mCameraModuleRootView;
    private CameraPreviewData mCameraPreviewData;
    private Menu mActionBarMenu;
    private CameraModule mCurrentModule;
    private LocalDataAdapter mDataAdapter;
    private FilmStripView mFilmStripView;
    private boolean mSecureCamera;
    private OnActionBarVisibilityListener mOnActionBarVisibilityListener = null;
    private Handler mMainHandler;
    private ActionBar mActionBar;
    private View mPanoStitchingPanel;
    private ProgressBar mBottomProgress;
    private FrameLayout mAboveFilmstripControlLayout;
    private boolean mPendingDeletion = false;
    private ViewGroup mUndoDeletionBar;
    private boolean mIsUndoingDeletion = false;
    private int mResultCodeForTesting;
    private Intent mResultDataForTesting;
    private static boolean sFirstStartAfterScreenOn = true;

    public static final String SECURE_CAMERA_EXTRA = "secure_camera";
    private final int DEFAULT_SYSTEM_UI_VISIBILITY = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN;

    private CameraOpenErrorCallback mCameraOpenErrorCallback =
            new CameraOpenErrorCallback() {
                @Override
                public void onCameraDisabled(int cameraId) {
//                    UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
//                            UsageStatistics.ACTION_OPEN_FAIL, "security");

                    CameraUtil.showErrorAndFinish(MainActivity.this,
                            R.string.camera_disabled);
                }

                @Override
                public void onDeviceOpenFailure(int cameraId) {
//                    UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
//                            UsageStatistics.ACTION_OPEN_FAIL, "open");

                    CameraUtil.showErrorAndFinish(MainActivity.this,
                            R.string.cannot_connect_camera);
                }

                @Override
                public void onReconnectionFailure(CameraManager mgr) {
//                    UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
//                            UsageStatistics.ACTION_OPEN_FAIL, "reconnect");

                    CameraUtil.showErrorAndFinish(MainActivity.this,
                            R.string.cannot_connect_camera);
                }
            };

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HIDE_ACTION_BAR) {
                removeMessages(HIDE_ACTION_BAR);
                //MainActivity.this.setSystemBarsVisibility(false);
            }
        }
    }

    public interface OnActionBarVisibilityListener {
        public void onActionBarVisibilityChanged(boolean isVisible);
    }

    public void setOnActionBarVisibilityListener(OnActionBarVisibilityListener listener) {
        mOnActionBarVisibilityListener = listener;
    }

    private FilmStripView.Listener mFilmStripListener =
            new FilmStripView.Listener() {
                @Override
                public void onDataPromoted(int dataID) {
//                    UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
//                            UsageStatistics.ACTION_DELETE, "promoted", 0,
//                            UsageStatistics.hashFileName(fileNameFromDataID(dataID)));

                    removeData(dataID);
                }

                @Override
                public void onDataDemoted(int dataID) {
//                    UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
//                            UsageStatistics.ACTION_DELETE, "demoted", 0,
//                            UsageStatistics.hashFileName(fileNameFromDataID(dataID)));

                    removeData(dataID);
                }

                @Override
                public void onDataFullScreenChange(int dataID, boolean full) {
                    boolean isCameraID = isCameraPreview(dataID);
                    if (!isCameraID) {
                        if (!full) {
                            // Always show action bar in filmstrip mode
                            //MainActivity.this.setSystemBarsVisibility(true, false);
                        } else if (mActionBar.isShowing()) {
                            // Hide action bar after time out in full screen mode
                            mMainHandler.sendEmptyMessageDelayed(HIDE_ACTION_BAR,
                                    SHOW_ACTION_BAR_TIMEOUT_MS);
                        }
                    }
                }

                /**
                 * Check if the local data corresponding to dataID is the camera
                 * preview.
                 *
                 * @param dataID the ID of the local data
                 * @return true if the local data is not null and it is the
                 *         camera preview.
                 */
                private boolean isCameraPreview(int dataID) {
                    LocalData localData = mDataAdapter.getLocalData(dataID);
                    if (localData == null) {
                        Log.w(TAG, "Current data ID not found.");
                        return false;
                    }
                    return localData.getLocalDataType() == LocalData.LOCAL_CAMERA_PREVIEW;
                }

                @Override
                public void onReload() {
                    setPreviewControlsVisibility(true);
                    //MainActivity.this.setSystemBarsVisibility(false);
                }

                @Override
                public void onCurrentDataCentered(int dataID) {
                    if (dataID != 0 && !mFilmStripView.isCameraPreview()) {
                        // For now, We ignore all items that are not the camera preview.
                        return;
                    }

                    if(!arePreviewControlsVisible()) {
                        setPreviewControlsVisibility(true);
                        //MainActivity.this.setSystemBarsVisibility(false);
                    }
                }

                @Override
                public void onCurrentDataOffCentered(int dataID) {
                    if (dataID != 0 && !mFilmStripView.isCameraPreview()) {
                        // For now, We ignore all items that are not the camera preview.
                        return;
                    }

                    if (arePreviewControlsVisible()) {
                        setPreviewControlsVisibility(false);
                    }
                }

                @Override
                public void onDataFocusChanged(final int dataID, final boolean focused) {
                    // Delay hiding action bar if there is any user interaction
                    if (mMainHandler.hasMessages(HIDE_ACTION_BAR)) {
                        mMainHandler.removeMessages(HIDE_ACTION_BAR);
                        mMainHandler.sendEmptyMessageDelayed(HIDE_ACTION_BAR,
                                SHOW_ACTION_BAR_TIMEOUT_MS);
                    }
                    // TODO: This callback is UI event callback, should always
                    // happen on UI thread. Find the reason for this
                    // runOnUiThread() and fix it.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LocalData currentData = mDataAdapter.getLocalData(dataID);
                            if (currentData == null) {
                                Log.w(TAG, "Current data ID not found.");
                                hidePanoStitchingProgress();
                                return;
                            }
                            boolean isCameraID = currentData.getLocalDataType() ==
                                    LocalData.LOCAL_CAMERA_PREVIEW;
                            if (!focused) {
                                if (isCameraID) {
                                    mCurrentModule.onPreviewFocusChanged(false);
                                    //MainActivity.this.setSystemBarsVisibility(true);
                                }
                                hidePanoStitchingProgress();
                            } else {
                                if (isCameraID) {
                                    // Don't show the action bar in Camera
                                    // preview.
                                    //MainActivity.this.setSystemBarsVisibility(false);

                                    if (mPendingDeletion) {
                                        performDeletion();
                                    }
                                } else {
                                    updateActionBarMenu(dataID);
                                }

                                Uri contentUri = currentData.getContentUri();
                                if (contentUri == null) {
                                    hidePanoStitchingProgress();
                                    return;
                                }
//                                int panoStitchingProgress = mPanoramaManager.getTaskProgress(
//                                        contentUri);
//                                if (panoStitchingProgress < 0) {
//                                    hidePanoStitchingProgress();
//                                    return;
//                                }
//                                showPanoStitchingProgress();
//                                updateStitchingProgress(panoStitchingProgress);
                            }
                        }
                    });
                }

                @Override
                public void onToggleSystemDecorsVisibility(int dataID) {
                    // If action bar is showing, hide it immediately, otherwise
                    // show action bar and hide it later
//                    if (mActionBar.isShowing()) {
//                        MainActivity.this.setSystemBarsVisibility(false);
//                    } else {
//                        // Don't show the action bar if that is the camera preview.
//                        boolean isCameraID = isCameraPreview(dataID);
//                        if (!isCameraID) {
//                            MainActivity.this.setSystemBarsVisibility(true, true);
//                        }
//                    }
                }

                @Override
                public void setSystemDecorsVisibility(boolean visible) {
                    //MainActivity.this.setSystemBarsVisibility(visible);
                }
            };

    private void updateActionBarMenu(int dataID) {
        LocalData currentData = mDataAdapter.getLocalData(dataID);
        if (currentData == null) {
            return;
        }
        int type = currentData.getLocalDataType();

        if (mActionBarMenu == null) {
            return;
        }

        int supported = 0;

        switch (type) {
            case LocalData.LOCAL_IMAGE:
                supported |= SUPPORT_DELETE | SUPPORT_ROTATE | SUPPORT_INFO
                        | SUPPORT_CROP | SUPPORT_SETAS | SUPPORT_EDIT
                        | SUPPORT_SHARE | SUPPORT_SHOW_ON_MAP;
                break;
            case LocalData.LOCAL_VIDEO:
                supported |= SUPPORT_DELETE | SUPPORT_INFO | SUPPORT_TRIM
                        | SUPPORT_SHARE;
                break;
            case LocalData.LOCAL_PHOTO_SPHERE:
                supported |= SUPPORT_DELETE | SUPPORT_ROTATE | SUPPORT_INFO
                        | SUPPORT_CROP | SUPPORT_SETAS | SUPPORT_EDIT
                        | SUPPORT_SHARE | SUPPORT_SHOW_ON_MAP;
                break;
            case LocalData.LOCAL_360_PHOTO_SPHERE:
                supported |= SUPPORT_DELETE | SUPPORT_ROTATE | SUPPORT_INFO
                        | SUPPORT_CROP | SUPPORT_SETAS | SUPPORT_EDIT
                        | SUPPORT_SHARE | SUPPORT_SHARE_PANORAMA360
                        | SUPPORT_SHOW_ON_MAP;
                break;
            default:
                break;
        }

        // In secure camera mode, we only support delete operation.
        if (isSecureCamera()) {
            supported &= SUPPORT_DELETE;
        }

        setMenuItemVisible(mActionBarMenu, R.id.action_delete,
                (supported & SUPPORT_DELETE) != 0);
        setMenuItemVisible(mActionBarMenu, R.id.action_rotate_ccw,
                (supported & SUPPORT_ROTATE) != 0);
        setMenuItemVisible(mActionBarMenu, R.id.action_rotate_cw,
                (supported & SUPPORT_ROTATE) != 0);
        setMenuItemVisible(mActionBarMenu, R.id.action_details,
                (supported & SUPPORT_INFO) != 0);
        setMenuItemVisible(mActionBarMenu, R.id.action_crop,
                (supported & SUPPORT_CROP) != 0);
        setMenuItemVisible(mActionBarMenu, R.id.action_setas,
                (supported & SUPPORT_SETAS) != 0);
        setMenuItemVisible(mActionBarMenu, R.id.action_edit,
                (supported & SUPPORT_EDIT) != 0);
        setMenuItemVisible(mActionBarMenu, R.id.action_trim,
                (supported & SUPPORT_TRIM) != 0);

        boolean standardShare = (supported & SUPPORT_SHARE) != 0;
        boolean panoramaShare = (supported & SUPPORT_SHARE_PANORAMA360) != 0;
        setMenuItemVisible(mActionBarMenu, R.id.action_share, standardShare);
        setMenuItemVisible(mActionBarMenu, R.id.action_share_panorama, panoramaShare);

        if (panoramaShare) {
            // For 360 PhotoSphere, relegate standard share to the overflow menu
            MenuItem item = mActionBarMenu.findItem(R.id.action_share);
            if (item != null) {
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                item.setTitle(getResources().getString(R.string.share_as_photo));
            }
            // And, promote "share as panorama" to action bar
            item = mActionBarMenu.findItem(R.id.action_share_panorama);
            if (item != null) {
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
            //setPanoramaShareIntent(currentData.getContentUri());
        }
        if (standardShare) {
            if (!panoramaShare) {
                MenuItem item = mActionBarMenu.findItem(R.id.action_share);
                if (item != null) {
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                    item.setTitle(getResources().getString(R.string.share));
                }
            }
//            setStandardShareIntent(currentData.getContentUri(), currentData.getMimeType());
//            setNfcBeamPushUri(currentData.getContentUri());
        }

        boolean itemHasLocation = currentData.getLatLong() != null;
        setMenuItemVisible(mActionBarMenu, R.id.action_show_on_map,
                itemHasLocation && (supported & SUPPORT_SHOW_ON_MAP) != 0);
    }

    private void setMenuItemVisible(Menu menu, int itemId, boolean visible) {
        MenuItem item = menu.findItem(itemId);
        if (item != null)
            item.setVisible(visible);
    }

    public void setSystemBarsVisibility(boolean visible) {
//    	visible = true;
        //setSystemBarsVisibility(visible, false);
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


    /**
     * Check whether camera controls are visible.
     *
     * @return whether controls are visible.
     */
    private boolean arePreviewControlsVisible() {
        return mCurrentModule.arePreviewControlsVisible();
    }

    private void setPreviewControlsVisibility(boolean showControls) {
        mCurrentModule.onPreviewFocusChanged(showControls);
    }

    /**
     * If {@param visible} is false, this hides the action bar and switches the
     * system UI to lights-out mode. If {@param hideLater} is true, a delayed message
     * will be sent after a timeout to hide the action bar.
     */
    private void setSystemBarsVisibility(boolean visible, boolean hideLater) {
        mMainHandler.removeMessages(HIDE_ACTION_BAR);

        int currentSystemUIVisibility = mAboveFilmstripControlLayout.getSystemUiVisibility();
        int newSystemUIVisibility = DEFAULT_SYSTEM_UI_VISIBILITY |
                (visible ? View.SYSTEM_UI_FLAG_VISIBLE :
                        View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN);
        if (newSystemUIVisibility != currentSystemUIVisibility) {
            mAboveFilmstripControlLayout.setSystemUiVisibility(newSystemUIVisibility);
        }

        boolean currentActionBarVisibility = mActionBar.isShowing();
        if (visible != currentActionBarVisibility) {
            if (visible) {
                mActionBar.show();
            } else {
                mActionBar.hide();
            }
            if (mOnActionBarVisibilityListener != null) {
                mOnActionBarVisibilityListener.onActionBarVisibilityChanged(visible);
            }
        }

        // Now delay hiding the bars
        if (visible && hideLater) {
            mMainHandler.sendEmptyMessageDelayed(HIDE_ACTION_BAR, SHOW_ACTION_BAR_TIMEOUT_MS);
        }
    }

    private void hidePanoStitchingProgress() {
        mPanoStitchingPanel.setVisibility(View.GONE);
    }

    private void showPanoStitchingProgress() {
        mPanoStitchingPanel.setVisibility(View.VISIBLE);
    }

    private void updateStitchingProgress(int progress) {
        mBottomProgress.setProgress(progress);
    }

    private void removeData(int dataID) {
        mDataAdapter.removeData(MainActivity.this, dataID);
        if (mDataAdapter.getTotalNumber() > 1) {
            //2013.12.19 swhwang, issue : undo bar and action bar show bug
            if(mFilmStripView.isCameraPreview()){
                mPendingDeletion = true;
                performDeletion();
            } else {
                showUndoDeletionBar();
            }

        } else {
            // If camera preview is the only view left in filmstrip,
            // no need to show undo bar.
            mPendingDeletion = true;
            performDeletion();
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

    private void performDeletion() {
        if (!mPendingDeletion) {
            return;
        }
        hideUndoDeletionBar(false);
        mDataAdapter.executeDeletion(MainActivity.this);

        int currentId = mFilmStripView.getCurrentId();
        updateActionBarMenu(currentId);
        mFilmStripListener.onCurrentDataCentered(currentId);
    }

    public void showUndoDeletionBar() {
        if (mPendingDeletion) {
            performDeletion();
        }
        Log.v(TAG, "showing undo bar");
        mPendingDeletion = true;
        if (mUndoDeletionBar == null) {
            ViewGroup v = (ViewGroup) getLayoutInflater().inflate(
                    R.layout.undo_bar, mAboveFilmstripControlLayout, true);
            mUndoDeletionBar = (ViewGroup) v.findViewById(R.id.camera_undo_deletion_bar);
            View button = mUndoDeletionBar.findViewById(R.id.camera_undo_deletion_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDataAdapter.undoDataRemoval();
                    hideUndoDeletionBar(true);
                }
            });
            // Setting undo bar clickable to avoid touch events going through
            // the bar to the buttons (eg. edit button, etc) underneath the bar.
            mUndoDeletionBar.setClickable(true);
            // When there is user interaction going on with the undo button, we
            // do not want to hide the undo bar.
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        mIsUndoingDeletion = true;
                    } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                        mIsUndoingDeletion =false;
                    }
                    return false;
                }
            });
        }
        mUndoDeletionBar.setAlpha(0f);
        mUndoDeletionBar.setVisibility(View.VISIBLE);
        mUndoDeletionBar.animate().setDuration(200).alpha(1f).setListener(null).start();
    }

    private void hideUndoDeletionBar(boolean withAnimation) {
        Log.v(TAG, "Hiding undo deletion bar");
        mPendingDeletion = false;
        if (mUndoDeletionBar != null) {
            if (withAnimation) {
                mUndoDeletionBar.animate()
                        .setDuration(200)
                        .alpha(0f)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                // Do nothing.
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mUndoDeletionBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                // Do nothing.
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                                // Do nothing.
                            }
                        })
                        .start();
            } else {
                mUndoDeletionBar.setVisibility(View.GONE);
            }
        }
    }

    private void setModuleFromIndex(int moduleIndex) {
        mCurrentModuleIndex = moduleIndex;
        mCurrentModule = new PhotoModule();
    }

    public CameraOpenErrorCallback getCameraOpenErrorCallback() {
        return mCameraOpenErrorCallback;
    }

    protected void setResultEx(int resultCode, Intent data) {
        mResultCodeForTesting = resultCode;
        mResultDataForTesting = data;
        setResult(resultCode, data);
    }

    public static boolean isFirstStartAfterScreenOn() {
        return sFirstStartAfterScreenOn;
    }

    public static void resetFirstStartAfterScreenOn() {
        sFirstStartAfterScreenOn = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.camera_filmstrip);
        //mActionBar = getActionBar();
        //mActionBar.addOnMenuVisibilityListener(this);
        mMainHandler = new MainHandler(getMainLooper());

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
        //this.setSystemBarsVisibility(false);

        LayoutInflater inflater = getLayoutInflater();
        View rootLayout = inflater.inflate(R.layout.camera, null, false);
        mCameraModuleRootView = rootLayout.findViewById(R.id.camera_app_root);
        mCameraPreviewData = new CameraPreviewData(rootLayout, FilmStripView.ImageData.SIZE_FULL, FilmStripView.ImageData.SIZE_FULL);
        mWrappedDataAdapter = new FixedFirstDataAdapter(new CameraDataAdapter(new ColorDrawable(getResources().getColor(R.color.photo_placeholder))), mCameraPreviewData);
        mFilmStripView = (FilmStripView) findViewById(R.id.filmstrip_view);
        mFilmStripView.setViewGap(getResources().getDimensionPixelSize(R.dimen.camera_film_strip_gap));
        mFilmStripView.setListener(mFilmStripListener);

        int moduleIndex = 0;    //PHOTO_MODULE_INDEX
        setModuleFromIndex(moduleIndex);
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
        //mOrientationListener.enable();
        mCurrentModule.onResumeBeforeSuper();
        super.onResume();
        mCurrentModule.onResumeAfterSuper();

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
        // Delete photos that are pending deletion
        performDeletion();
        //mOrientationListener.disable();
        mCurrentModule.onPauseBeforeSuper();
        super.onPause();
        mCurrentModule.onPauseAfterSuper();

        //mLocalImagesObserver.setActivityPaused(true);
        //mLocalVideosObserver.setActivityPaused(true);
    }
}
