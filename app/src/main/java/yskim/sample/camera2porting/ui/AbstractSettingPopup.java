package yskim.sample.camera2porting.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import yskim.sample.camera2porting.R;

abstract public class AbstractSettingPopup extends RotateLayout {
    protected ViewGroup mSettingList;
    protected TextView mTitle;

    public AbstractSettingPopup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTitle = (TextView) findViewById(R.id.title);
        mSettingList = (ViewGroup) findViewById(R.id.settingList);
    }

    abstract public void reloadPreference();
}

