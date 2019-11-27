package yskim.sample.camera2porting.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import yskim.sample.camera2porting.MainActivity;
import yskim.sample.camera2porting.R;

public class FilmstripBottomControls extends RelativeLayout {

    /**
     * Classes implementing this interface can listen for events on the bottom
     * controls.
     */
    public static interface BottomControlsListener {
        /**
         * Called when the user pressed the "view photosphere" button.
         */
//        public void onViewPhotoSphere();

        /**
         * Called when the user pressed the "edit" button.
         */
//        public void onEdit();

        /**
         * Called when the user pressed the "tiny planet" button.
         */
//        public void onTinyPlanet();
    }

    private BottomControlsListener mListener;
    private ImageButton mEditButton;
    private ImageButton mViewPhotoSphereButton;
    private ImageButton mTinyPlanetButton;

    public FilmstripBottomControls(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mEditButton = (ImageButton)
                findViewById(R.id.filmstrip_bottom_control_edit);
        mEditButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
//                    mListener.onEdit();
                }
            }
        });

        mViewPhotoSphereButton = (ImageButton)
                findViewById(R.id.filmstrip_bottom_control_panorama);
        mViewPhotoSphereButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
//                    mListener.onViewPhotoSphere();
                }
            }
        });

        mTinyPlanetButton = (ImageButton)
                findViewById(R.id.filmstrip_bottom_control_tiny_planet);
        mTinyPlanetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
//                    mListener.onTinyPlanet();
                }
            }
        });
    }

    /**
     * Sets a new or replaces an existing listener for bottom control events.
     */
    public void setListener(BottomControlsListener listener) {
        mListener = listener;
    }

    /**
     * Sets the visibility of the edit button.
     */
    public void setEditButtonVisibility(boolean visible) {
        setVisibility(mEditButton, visible);
    }

    /**
     * Sets the visibility of the view-photosphere button.
     */
    public void setViewPhotoSphereButtonVisibility(boolean visible) {
        setVisibility(mViewPhotoSphereButton, visible);
    }

    /**
     * Sets the visibility of the tiny-planet button.
     */
    public void setTinyPlanetButtonVisibility(final boolean visible) {
        setVisibility(mTinyPlanetButton, visible);
    }

    /**
     * Sets the visibility of the given view.
     */
    private static void setVisibility(final View view, final boolean visible) {
        view.post(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(visible ? View.VISIBLE
                        : View.INVISIBLE);
            }
        });
    }

//    @Override
//    public void onActionBarVisibilityChanged(boolean isVisible) {
//        // TODO: Fade in and out
//        setVisibility(isVisible ? VISIBLE : INVISIBLE);
//    }
}

