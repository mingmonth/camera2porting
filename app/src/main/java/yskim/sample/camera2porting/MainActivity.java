package yskim.sample.camera2porting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick({R.id.cam1, R.id.cam2})
    public void onViewClicked(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.cam1:
                intent = new Intent(this, CameraActivity.class);
                break;
            case R.id.cam2:
                intent = new Intent(this, CameraActivity2.class);
                break;
        }

        if(intent != null) {
            startActivity(intent);
        }
    }
}
