package com.example.ctrl_f_it;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class ProcessingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_processing);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.processing, menu);
		return true;
	}
}
