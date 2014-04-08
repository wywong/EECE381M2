package com.example.ctrl_f_it;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public static String imageCaptureFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_camera:
                openCamera();
                return true;
            case R.id.action_editor:
                openEditor();
                return true;
            case R.id.action_help:
                openHelp();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            case R.id.action_processing:
            	openGridView();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void openCamera(){
    	Intent intent = new Intent(this, camActivity.class);
    	startActivity(intent);
    }
    
    public void openEditor(){
    	Intent intent =  new Intent(this, TextListActivity.class);
    	startActivity(intent);
    }
    
    public void openHelp(){
    	Intent intent =  new Intent(this, HelpActivity.class);
    	startActivity(intent);
    }
    
    public void openSettings(){
    	Toast t = Toast.makeText(getApplicationContext(), "I would now call the settings function", Toast.LENGTH_LONG);
    	t.show();
    }
    
    public void openProcessing(){
    	Intent intent = new Intent(this, ProcessingActivity.class);
    	startActivity(intent);
    }
    
    public void openGridView(){
    	Intent intent = new Intent(this, GridViewActivity.class);
    	startActivity(intent);
    }
}
