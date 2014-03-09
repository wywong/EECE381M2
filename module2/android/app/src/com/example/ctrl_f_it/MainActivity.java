package com.example.ctrl_f_it;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void openCamera(){
    	Toast t = Toast.makeText(getApplicationContext(), "I would now call the camera function", Toast.LENGTH_LONG);
    	t.show();
    }
    public void openEditor(){
    	Toast t = Toast.makeText(getApplicationContext(), "I would now call the editor function", Toast.LENGTH_LONG);
    	t.show();
    }
    public void openHelp(){
    	Toast t = Toast.makeText(getApplicationContext(), "I would now call the help function", Toast.LENGTH_LONG);
    	t.show();
    }
    public void openSettings(){
    	Toast t = Toast.makeText(getApplicationContext(), "I would now call the settings function", Toast.LENGTH_LONG);
    	t.show();
    }
}
