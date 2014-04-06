package com.example.ctrl_f_it;

import java.io.File;
import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class NotepadActivity extends ListActivity {
	private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    
    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    
    private ArrayList<String> textFiles = new ArrayList<String>();
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_view);
        fillData();
        registerForContextMenu(getListView());
    }
    
    public ArrayList<String> getFilePaths() {
		ArrayList<String> filePaths = new ArrayList<String>();
		
		File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"Ctrl_F_It/Text");
		// Create the storage directory if it does not exist
		if (!directory.exists()) {
		    if (!directory.mkdirs()) {
		        Log.d("Ctrl_F_It", "Oops! Failed create " + "Ctrl_F_It" + " directory");
		        return null;
		    }
		}
		
		File[] listFiles = directory.listFiles();
		// check for count
		if(listFiles.length > 0) {
			
			// loop through all files
			for(int i = 0; i < listFiles.length; i++) {
				// get file path
				String filePath = listFiles[i].getAbsolutePath();
				
				filePaths.add(filePath);
			}
		} 

    	return filePaths;
    }
    
    private void fillData() {
    	File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"Ctrl_F_It/Text");
    	
    	textFiles = getFilePaths();    	
    	// Create an array to specify the fields we want to display in the list (only TITLE)
        String[] files = new String[]{directory.listFiles().toString()};
        
    	
    	setListAdapter(new ArrayAdapter<String>(this, R.layout.text_rows, files));
    	
    	ListView listView = getListView();
    	listView.setTextFilterEnabled(true);
    	
    }
}
