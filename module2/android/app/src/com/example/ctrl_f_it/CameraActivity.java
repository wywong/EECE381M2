package com.example.ctrl_f_it;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;

public class CameraActivity extends Activity {

	@SuppressLint("NewApi")

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int PORTRAIT = 90;
		int camId = 0;
		Camera cam;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		
		// Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        cam = Camera.open(camId);
        Camera.Parameters param = cam.getParameters();
        cam.setParameters(param);
        cam.setDisplayOrientation(PORTRAIT);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);
		return true;
	}
}
