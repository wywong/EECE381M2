package com.example.ctrl_f_it;

import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public static String IPADDRESS;
	public static int PORT;
	public static Socket sock = null;
	private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSave = (Button) findViewById(R.id.button_ip_save);
        
        btnSave.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View v) {
                saveIP();
            }
        });
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
            	openProcessing();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void openCamera() {
    	Intent intent = new Intent(this, camActivity.class);
    	startActivity(intent);
    }
    
    public void openEditor() {
    	Intent intent =  new Intent(this, TextListActivity.class);
    	startActivity(intent);
    }
    
    public void openHelp() {
    	Toast t = Toast.makeText(getApplicationContext(), "I would now call the help function", Toast.LENGTH_LONG);
    	t.show();
    }
    
    public void openSettings() {
    	Toast t = Toast.makeText(getApplicationContext(), "I would now call the settings function", Toast.LENGTH_LONG);
    	t.show();
    }
    
    public void openProcessing() {
    	Intent intent = new Intent(this, ProcessingActivity.class);
    	startActivity(intent);
    }
    
    private void saveIP() {
    	IPADDRESS = getConnectToIP();
    	PORT = getConnectToPort();
    }
    
    // Construct an IP address from the four boxes
 	public String getConnectToIP() {
 		String addr = "";
 		EditText text_ip;
 		text_ip = (EditText) findViewById(R.id.ip1);
 		addr += text_ip.getText().toString();
 		text_ip = (EditText) findViewById(R.id.ip2);
 		addr += "." + text_ip.getText().toString();
 		text_ip = (EditText) findViewById(R.id.ip3);
 		addr += "." + text_ip.getText().toString();
 		text_ip = (EditText) findViewById(R.id.ip4);
 		addr += "." + text_ip.getText().toString();
 		return addr;
 	}
 	
 	// Gets the Port from the appropriate field.
 	public Integer getConnectToPort() {
 		Integer port;
 		EditText text_port;

 		text_port = (EditText) findViewById(R.id.port);
 		port = Integer.parseInt(text_port.getText().toString());

 		return port;
 	}
}
