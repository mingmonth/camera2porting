package yskim.sample.camera2porting.camera.data;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;

public class SimpleViewData implements LocalData {

    private final int mWidth;
    private final int mHeight;
    private final View mView;

    public SimpleViewData(
            View v, int width, int height) {
        mView = v;
        mWidth = width;
        mHeight = height;
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

    @Override
    public int getHeight() {
        return mHeight;
    }

    @Override
    public int getOrientation() {
        return 0;
    }

    @Override
    public View getView(Activity activity, int width, int height, Drawable placeHolder,
                        LocalDataAdapter adapter) {
        return mView;
    }

    @Override
    public void prepare() {
    }
}

