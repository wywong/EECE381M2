/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ctrl_f_it;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class TextListActivity extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    
    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    

    private NotesDbAdapter mDbHelper;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads()
    			.detectDiskWrites().detectNetwork().penaltyLog().build());
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_list);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
        
	    // Set up a timer task.  We will use the timer to check the
		// input queue every 500 ms
        
        Button connectButton = (Button) findViewById(R.id.button_connect);
        Button disconnectButton = (Button) findViewById(R.id.button_disconnect);
        Button syncButton = (Button) findViewById(R.id.button_sync);
		
		TCPReadTimerTask tcp_task = new TCPReadTimerTask();
		Timer tcp_timer = new Timer();
		tcp_timer.schedule(tcp_task, 3000, 500);
		
		connectButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	openSocket();
            }

        });
        
		disconnectButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				closeSocket();
			}
		});
		
		syncButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				syncDatabase();
			}
		});
    }
    
    @SuppressWarnings("deprecation")
	private void fillData() {
        // Get all of the rows from the database and create the item list
        Cursor notesCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(notesCursor);
        
        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{NotesDbAdapter.KEY_TITLE};
        
        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};
        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = 
        	    new SimpleCursorAdapter(this, R.layout.text_row, notesCursor, from, to);
        setListAdapter(notes);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case INSERT_ID:
            createNote();
            return true;
        }
        
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
        case DELETE_ID:
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            mDbHelper.deleteNote(info.id);
            fillData();
            return true;
        }
        return super.onContextItemSelected(item);
	}

    private void createNote() {
        Intent i = new Intent(this, TextEditorActivity.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, TextEditorActivity.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT); 
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
    
    public void syncDatabase() {
    	Cursor cursor = mDbHelper.fetchAllNotes();
    }

    public void openSocket() {
		//MyApplication app = (MyApplication) getApplication();

		// Make sure the socket is not already opened 		
		if (MainActivity.sock != null && MainActivity.sock.isConnected() && !MainActivity.sock.isClosed()) {
			Log.d("SOCKET", "Socket already open");
			
			//LOG CAT ERROR MESSAGE
			return;
		}
		
		// open the socket.  SocketConnect is a new subclass
	    // (defined below).  This creates an instance of the subclass
		// and executes the code in it.		
		new SocketConnect().execute((Void) null);
	}
    
    public class SocketConnect extends AsyncTask<Void, Void, Socket> {

		// The main parcel of work for this thread.  Opens a socket
		// to connect to the specified IP.
		
		protected Socket doInBackground(Void... voids) {
			Socket s = null;
			String ip = MainActivity.IPADDRESS;
			Integer port = MainActivity.PORT;

			try {
				s = new Socket(ip, port);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return s;
		}

		// After executing the doInBackground method, this is 
		// automatically called, in the UI (main) thread to store
		// the socket in this app's persistent storage
		
		protected void onPostExecute(Socket s) {
			//MyApplication myApp = (MyApplication) TextListActivity.this.getApplication();
			MainActivity.sock = s;
		}
	}
    
    // Called when the user closes a socket
 	public void closeSocket() {
 		//MyApplication app = (MyApplication) getApplication();
 		Socket s = MainActivity.sock;
 		try {
 			s.getOutputStream().close();
 			s.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void sendMessage(char sendCode) {
		//MyApplication app = (MyApplication) getApplication();
		// Get the message from the box
		
		String msg = ((EditText) findViewById(R.id.body)).getText().toString();

		// Create an array of bytes.  First byte will be the
		// message length, and the next ones will be the message
		
		byte buf[] = new byte[msg.length() + 2];
		buf[0] = (byte) msg.length();
		buf[1] = (byte) sendCode;
		System.arraycopy(msg.getBytes(), 0, buf, 2, msg.length());

		// Now send through the output stream of the socket
		
		OutputStream out;
		try {
			out = MainActivity.sock.getOutputStream();
			try {
				out.write(buf, 0, msg.length() + 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
 	
 	public class TCPReadTimerTask extends TimerTask {
		public void run() {
			//MyApplication app = (MyApplication) getApplication();
			if (MainActivity.sock != null && MainActivity.sock.isConnected() && !MainActivity.sock.isClosed()) {
				
				try {
					InputStream in = MainActivity.sock.getInputStream();

					// See if any bytes are available from the Middleman
					
					int bytes_avail = in.available();
					if (bytes_avail > 0) {
						
						// If so, read them in and create a sring
						
						byte buf[] = new byte[bytes_avail];
						in.read(buf);

						final String s = new String(buf, 0, bytes_avail, "US-ASCII");
		
						// As explained in the tutorials, the GUI can not be
						// updated in an asyncrhonous task.  So, update the GUI
						// using the UI thread.
						
						runOnUiThread(new Runnable() {
							public void run() {
								//EditText et = (EditText) findViewById(R.id.RecvdMessage);
								//et.setText(s);
							}
						});
						
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
