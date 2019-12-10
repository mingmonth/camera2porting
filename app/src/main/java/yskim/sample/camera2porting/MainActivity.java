package yskim.sample.camera2porting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import yskim.sample.camera2porting.camera.util.Debug;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button buttonCam1;
    Button buttonCam2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonCam1 = findViewById(R.id.cam1);
        buttonCam2 = findViewById(R.id.cam2);

        buttonCam1.setOnClickListener(this);
        buttonCam2.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.cam1:
                Debug.logd(new Exception(), "");
                Intent intent = new Intent(this, CameraActivity.class);
                startActivity(intent);
                break;
            case R.id.cam2:
                Debug.logd(new Exception(), "");
                Intent intent2 = new Intent(this, CameraActivity2.class);
                startActivity(intent2);
                break;
            default:
                break;
        }
    }
}
