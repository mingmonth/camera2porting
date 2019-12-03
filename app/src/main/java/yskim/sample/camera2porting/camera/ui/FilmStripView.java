package yskim.sample.camera2porting.camera.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

import yskim.sample.camera2porting.CameraActivity;
import yskim.sample.camera2porting.camera.data.LocalDataAdapter;
import yskim.sample.camera2porting.camera.util.Debug;

public class FilmStripView extends ViewGroup {

    private CameraActivity mActivity;
    private DataAdapter mDataAdapter;
    private final Rect mDrawArea = new Rect();

    private final int mCurrentItem = 0;
    private float mScale = 1.0f;
    private int mCenterX = -1;
    private ViewItem[] mViewItem = new ViewItem[1];

    private ViewItem buildItemFromData(int dataID) {
        ImageData data = mDataAdapter.getImageData(dataID);
        if (data == null) {
            return null;
        }
        data.prepare();
        View v = mDataAdapter.getView(mActivity, dataID);
        if (v == null) {
            return null;
        }
        ViewItem item = new ViewItem(dataID, v);
        addView(item.getView());
        return item;
    }

    private void layoutViewItems(boolean layoutChanged) {
        if (mViewItem[mCurrentItem] == null ||
                mDrawArea.width() == 0 ||
                mDrawArea.height() == 0) {
            return;
        }

        Debug.logd(new Exception(), "mCurrentItem: " + mCurrentItem);
        Debug.logd(new Exception(), "mViewItem.length: " + mViewItem.length);
        Debug.logd(new Exception(), "layoutChanged: " + layoutChanged);

        // If the layout changed, we need to adjust the current position so
        // that if an item is centered before the change, it's still centered.
        mViewItem[mCurrentItem].setLeftPosition(mCenterX - mViewItem[mCurrentItem].getView().getMeasuredWidth() / 2);
        Debug.logd(new Exception(), "mCenterX - mViewItem[mCurrentItem].getView().getMeasuredWidth() / 2: " + (mCenterX - mViewItem[mCurrentItem].getView().getMeasuredWidth() / 2));
        Debug.logd(new Exception(), "mViewItem[mCurrentItem].getView().getMeasuredWidth(): " + mViewItem[mCurrentItem].getView().getMeasuredWidth());

        /**
         * Transformed scale fraction between 0 and 1. 0 if the scale is
         * {@link FILM_STRIP_SCALE}. 1 if the scale is {@link FULL_SCREEN_SCALE}
         * .
         */
        final float scaleFraction = 1.0f;

        Debug.logd(new Exception(), "scaleFraction: " + scaleFraction);

        // Layout the current ViewItem first.
        final ViewItem currItem = mViewItem[mCurrentItem];
        currItem.layoutIn(mDrawArea, mCenterX, mScale);
        currItem.setTranslationX(0f, mScale);
        currItem.getView().setAlpha(1f);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mDrawArea.left = l;
        mDrawArea.top = t;
        mDrawArea.right = r;
        mDrawArea.bottom = b;
        //mZoomView.layout(mDrawArea.left, mDrawArea.top, mDrawArea.right, mDrawArea.bottom);
        // TODO: Need a more robust solution to decide when to re-layout
        // If in the middle of zooming, only re-layout when the layout has changed.
        layoutViewItems(changed);

    }

    public FilmStripView(Context context) {
        super(context);
        init((CameraActivity) context);
    }

    /**
     * Constructor.
     */
    public FilmStripView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init((CameraActivity) context);
    }

    /**
     * Constructor.
     */
    public FilmStripView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init((CameraActivity) context);
    }

    private void init(CameraActivity cameraActivity) {
        setWillNotDraw(false);
        mActivity = cameraActivity;
        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
    }

    /**
     * Returns [width, height] preserving image aspect ratio.
     */
    private int[] calculateChildDimension(
            int imageWidth, int imageHeight, int imageOrientation,
            int boundWidth, int boundHeight) {
        if (imageOrientation == 90 || imageOrientation == 270) {
            // Swap width and height.
            int savedWidth = imageWidth;
            imageWidth = imageHeight;
            imageHeight = savedWidth;
        }
        if (imageWidth == ImageData.SIZE_FULL
                || imageHeight == ImageData.SIZE_FULL) {
            imageWidth = boundWidth;
            imageHeight = boundHeight;
        }

        int[] ret = new int[2];
        ret[0] = boundWidth;
        ret[1] = boundHeight;

        if (imageWidth * ret[1] > ret[0] * imageHeight) {
            ret[1] = imageHeight * ret[0] / imageWidth;
        } else {
            ret[0] = imageWidth * ret[1] / imageHeight;
        }

        return ret;
    }

    private void measureViewItem(ViewItem item, int boundWidth, int boundHeight) {
        int id = item.getId();
        ImageData imageData = mDataAdapter.getImageData(id);
        if (imageData == null) {
            return;
        }

        int[] dim = calculateChildDimension(imageData.getWidth(), imageData.getHeight(),
                imageData.getOrientation(), boundWidth, boundHeight);

        item.getView().measure(
                MeasureSpec.makeMeasureSpec(
                        dim[0], MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(
                        dim[1], MeasureSpec.EXACTLY));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int boundWidth = MeasureSpec.getSize(widthMeasureSpec);
        int boundHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (boundWidth == 0 || boundHeight == 0) {
            // Either width or height is unknown, can't measure children yet.
            return;
        }

        if (mDataAdapter != null) {
            mDataAdapter.suggestViewSizeBound(boundWidth / 2, boundHeight / 2);
        }

        for (ViewItem item : mViewItem) {
            if (item != null) {
                measureViewItem(item, boundWidth, boundHeight);
            }
        }
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        return false;
    }

    public void setDataAdapter(DataAdapter adapter) {
        mDataAdapter = adapter;
        mDataAdapter.suggestViewSizeBound(getMeasuredWidth(), getMeasuredHeight());
        Debug.logd(new Exception(), "getMeasuredWidth():" + getMeasuredWidth());
        Debug.logd(new Exception(), "getMeasuredHeight():" + getMeasuredHeight());
        mDataAdapter.setListener(new DataAdapter.Listener() {
            @Override
            public void onDataLoaded() {
                Debug.logd(new Exception(), "");
                reload();
            }

            @Override
            public void onDataUpdated(DataAdapter.UpdateReporter reporter) {
                Debug.logd(new Exception(), "");
            }

            @Override
            public void onDataInserted(int dataID, ImageData data) {
                Debug.logd(new Exception(), "");
                if (mViewItem[mCurrentItem] == null) {
                    Debug.logd(new Exception(), "");
                    // empty now, simply do a reload.
                    reload();
                    return;
                }
                Debug.logd(new Exception(), "");
            }

            @Override
            public void onDataRemoved(int dataID, ImageData data) {
                Debug.logd(new Exception(), "");
            }
        });
    }

    /**
     * The whole data might be totally different. Flush all and load from the
     * start. Filmstrip will be centered on the first item, i.e. the camera
     * preview.
     */
    private void reload() {
        // Clear out the mViewItems and rebuild with camera in the center.
        Arrays.fill(mViewItem, null);
        int dataNumber = mDataAdapter.getTotalNumber();
        Debug.logd(new Exception(), "dataNumber: " + dataNumber);
        if (dataNumber == 0) {
            Debug.logd(new Exception(), "");
            return;
        }

        mViewItem[mCurrentItem] = buildItemFromData(0);
        if (mViewItem[mCurrentItem] == null) {
            Debug.logd(new Exception(), "");
            return;
        }

        mCenterX = -1;
        invalidate();
    }

    @Override
    public void onDraw(Canvas c) {
        // TODO: remove layoutViewItems() here.
        layoutViewItems(false);
        super.onDraw(c);
    }

    /**
     * Common interface for all images in the filmstrip.
     */
    public interface ImageData {

        // View types.
        int VIEW_TYPE_NONE = 0;
        int VIEW_TYPE_STICKY = 1;
        int VIEW_TYPE_REMOVABLE = 2;
        /**
         * SIZE_FULL can be returned by {@link ImageData#getWidth()} and
         * {@link ImageData#getHeight()}. When SIZE_FULL is returned for
         * width/height, it means the the width or height will be disregarded
         * when deciding the view size of this ImageData, just use full screen
         * size.
         */
        int SIZE_FULL = -2;

        View getView(Activity a, int width, int height, Drawable placeHolder, LocalDataAdapter adapter);

        /**
         * Returns the width of the image before orientation applied.
         * The final layout of the view returned by
         * {@link DataAdapter#getView(android.app.Activity, int)} will
         * preserve the aspect ratio of
         * {@link yskim.sample.camera2porting.camera.ui.FilmStripView.ImageData#getWidth()} and
         * {@link yskim.sample.camera2porting.camera.ui.FilmStripView.ImageData#getHeight()}.
         */
        int getWidth();

        /**
         * Returns the height of the image before orientation applied.
         * The final layout of the view returned by
         * {@link DataAdapter#getView(android.app.Activity, int)} will
         * preserve the aspect ratio of
         * {@link yskim.sample.camera2porting.camera.ui.FilmStripView.ImageData#getWidth()} and
         * {@link yskim.sample.camera2porting.camera.ui.FilmStripView.ImageData#getHeight()}.
         */
        int getHeight();

        /**
         * Returns the orientation of the image.
         */
        int getOrientation();

        /**
         * Gives the data a hint when its view is going to be displayed.
         * {@code FilmStripView} should always call this function before showing
         * its corresponding view every time.
         */
        void prepare();
    }

    /**
     * An interfaces which defines the interactions between the
     * {@link ImageData} and the {@link FilmStripView}.
     */
    public interface DataAdapter {
        /** Returns the total number of image data */
        int getTotalNumber();

        /**
         * Returns the view to visually present the image data.
         *
         * @param activity The {@link Activity} context to create the view.
         * @param dataID The ID of the image data to be presented.
         * @return The view representing the image data. Null if unavailable or
         *         the {@code dataID} is out of range.
         */
        View getView(Activity activity, int dataID);

        /**
         * Returns the {@link ImageData} specified by the ID.
         *
         * @param dataID The ID of the {@link ImageData}.
         * @return The specified {@link ImageData}. Null if not available.
         */
        ImageData getImageData(int dataID);

        /**
         * Suggests the data adapter the maximum possible size of the layout so
         * the {@link DataAdapter} can optimize the view returned for the
         * {@link ImageData}.
         *
         * @param w Maximum width.
         * @param h Maximum height.
         */
        void suggestViewSizeBound(int w, int h);

        /**
         * Sets the listener for data events over the ImageData.
         *
         * @param listener The listener to use.
         */
        void setListener(Listener listener);

        interface UpdateReporter {
            /**
             * Checks if the data of dataID is removed.
             */
            boolean isDataRemoved(int dataID);

            /**
             * Checks if the data of dataID is updated.
             */
            boolean isDataUpdated(int dataID);
        }

        /**
         * An interface which defines the listener for data events over
         * {@link ImageData}. Usually {@link FilmStripView} itself.
         */
        interface Listener {
            // Called when the whole data loading is done. No any assumption
            // on previous data.
            void onDataLoaded();

            // Only some of the data is changed. The listener should check
            // if any thing needs to be updated.
            void onDataUpdated(UpdateReporter reporter);

            void onDataInserted(int dataID, ImageData data);

            void onDataRemoved(int dataID, ImageData data);
        }
    }

    /**
     * A helper class to tract and calculate the view coordination.
     */
    private static class ViewItem {
        private int mDataId;
        /** The position of the left of the view in the whole filmstrip. */
        private int mLeftPosition;
        private View mView;
        private RectF mViewArea;

        /**
         * Constructor.
         *
         * @param id The id of the data from {@link DataAdapter}.
         * @param v The {@code View} representing the data.
         */
        public ViewItem(
                int id, View v) {
            v.setPivotX(0f);
            v.setPivotY(0f);
            mDataId = id;
            mView = v;
            mLeftPosition = -1;
            mViewArea = new RectF();
        }

        /** Returns the data id from {@link DataAdapter}. */
        public int getId() {
            return mDataId;
        }

        /** Sets the data id from {@link DataAdapter}. */
        public void setId(int id) {
            mDataId = id;
        }

        /** Sets the left position of the view in the whole filmstrip. */
        public void setLeftPosition(int pos) {
            mLeftPosition = pos;
        }

        /** Returns the left position of the view in the whole filmstrip. */
        public int getLeftPosition() {
            return mLeftPosition;
        }

        /** Returns the translation of X regarding the view scale. */
        public float getScaledTranslationX(float scale) {
            return mView.getTranslationX() / scale;
        }

        /** Sets the translation of X regarding the view scale. */
        public void setTranslationX(float transX, float scale) {
            mView.setTranslationX(transX * scale);
        }

        public int getCenterX() {
            return mLeftPosition + mView.getMeasuredWidth() / 2;
        }

        /** Gets the view representing the data. */
        public View getView() {
            return mView;
        }

        private void layoutAt(int left, int top) {
            mView.layout(left, top, left + mView.getMeasuredWidth(),
                    top + mView.getMeasuredHeight());
        }

        /**
         * Layouts the view in the area assuming the center of the area is at a
         * specific point of the whole filmstrip.
         *
         * @param drawArea The area when filmstrip will show in.
         * @param refCenter The absolute X coordination in the whole filmstrip
         *            of the center of {@code drawArea}.
         * @param scale The current scale of the filmstrip.
         */
        public void layoutIn(Rect drawArea, int refCenter, float scale) {
            final float translationX = 0f;
            int left = (int) (drawArea.centerX() + (mLeftPosition - refCenter + translationX) * scale);
            int top = (int) (drawArea.centerY() - (mView.getMeasuredHeight() / 2) * scale);
            layoutAt(left, top);
            mView.setScaleX(scale);
            mView.setScaleY(scale);

            // update mViewArea for touch detection.
            int l = mView.getLeft();
            int t = mView.getTop();
            mViewArea.set(l, t,
                    l + mView.getMeasuredWidth() * scale,
                    t + mView.getMeasuredHeight() * scale);
        }

        /**
         * Return the width of the view.
         */
        public int getWidth() {
            return mView.getWidth();
        }

        @Override
        public String toString() {
            return "DataID = " + mDataId + "\n\t left = " + mLeftPosition
                    + "\n\t viewArea = " + mViewArea
                    + "\n\t centerX = " + getCenterX()
                    + "\n\t view MeasuredSize = "
                    + mView.getMeasuredWidth() + ',' + mView.getMeasuredHeight()
                    + "\n\t view Size = " + mView.getWidth() + ',' + mView.getHeight()
                    + "\n\t view scale = " + mView.getScaleX();
        }
    }
}

