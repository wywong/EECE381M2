package com.example.ctrl_f_it;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TextEditorActivity extends Activity {

    private EditText mTitleText;
    private EditText mBodyText;
    private Long mRowId;
    private NotesDbAdapter mDbHelper;
    private int byteCount;
    private int bytesToBeSent;
    
    private Button confirmButton;
    private Button searchButton;
    private Button sendButton;
    private Button retrieveButton;
    
    private static char textEditorSendCode = (char)0;
    private static char textEditorRetrCode = (char)1;
    
    
    public int textIndex = 0;
    public String searchTextPrev = null;
    public boolean recieveDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        
        setContentView(R.layout.text_edit);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);

        confirmButton = (Button) findViewById(R.id.confirm);
        searchButton = (Button) findViewById(R.id.search);
        sendButton = (Button) findViewById(R.id.send);
        retrieveButton = (Button) findViewById(R.id.retrieve);
        
        if(savedInstanceState == null)
        	mRowId =  null;
        else
            mRowId = (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null)
            	mRowId = extras.getLong(NotesDbAdapter.KEY_ROWID);
            else
                mRowId = null;
        }

        populateFields();
        
        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });
        
        searchButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				searchBody();
			}
		});
        
        sendButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                sendBody();
            }
        });
        
        retrieveButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	// Set up a timer task.  We will use the timer to check the
         		// input queue every 500 ms
         		TCPReadTimerTask tcp_task = new TCPReadTimerTask();
         		Timer tcp_timer = new Timer();
         		tcp_timer.schedule(tcp_task, 3000, 500);
                retrBody();
            }

        });
    }
    
    @SuppressWarnings("deprecation")
	private void populateFields() {
    	if(mRowId != null) {
    		Cursor note = mDbHelper.fetchNote(mRowId);
    		startManagingCursor(note);
    		mTitleText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
    		mBodyText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
    	}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }
    
    private void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();

        if (title != null) {
        	if (mRowId == null) {
        		long id = mDbHelper.createNote(title, body);
        		if (id > 0) {
        			mRowId = id;
        		}
        	} else {
        		mDbHelper.updateNote(mRowId, title, body);
        	}
        } else {
        	Toast.makeText(getApplicationContext(), "Title cannot be blank", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void searchBody() {
    	int tt;
		boolean found = false;
		
		String searchText = ((EditText)findViewById(R.id.searchString)).getText().toString();
		if(!searchText.equals(searchTextPrev) ) {
			searchTextPrev = searchText;
			textIndex = 0;
		}
		
		String text = mBodyText.getText().toString();
		
		searchText = searchText.toLowerCase(Locale.CANADA);
		text = text.toLowerCase(Locale.CANADA);
		
		int searchSize = searchText.length();
		int bodySize = text.length();
		
		for(tt = textIndex; tt < bodySize - searchSize + 1; tt++){
			if(text.regionMatches(tt, searchText, 0, searchSize)){
				tt++;
				textIndex = tt;
				found = true;
				break;
			}
		}
		
		if(found) {
			mBodyText.setSelection(textIndex - 1, textIndex + searchSize - 1);
		}
		
		found = false;
		
		if(tt >= bodySize - searchSize) {
			textIndex = 0;
			Toast.makeText(getApplicationContext(), "End of File Reached", Toast.LENGTH_SHORT).show();
		}
	}
    
    private void sendBody() {
    	sendMessage(textEditorSendCode);
    	//while(!recieveDone);
    	//recieveDone = false;
    }
    
    private void retrBody() {
    	sendMessage(textEditorRetrCode);
    	//while(!recieveDone);
    	//recieveDone = false;
    }
       
    public void sendMessage(char sendCode) {
		//MyApplication app = (MyApplication) getApplication();
		// Get the message from the box
    	String msg = "";
		if(sendCode == textEditorSendCode)
			msg = ((EditText) findViewById(R.id.body)).getText().toString();			

		// Create an array of bytes.  First byte will be the
		// message length, and the next ones will be the message
		
		byte buf[] = new byte[msg.length() + 3];
		buf[0] = (byte) msg.length();
		buf[1] = (byte) (msg.length() >> 8);
		buf[2] = (byte) sendCode;
		System.arraycopy(msg.getBytes(), 0, buf, 2, msg.length());

		// Now send through the output stream of the socket
		
		OutputStream out;
		try {
			out = MainActivity.sock.getOutputStream();
			try {
				out.write(buf, 0, msg.length() + 3);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public class TCPReadTimerTask extends TimerTask {
		public void run() {
			if (MainActivity.sock != null && MainActivity.sock.isConnected() && !MainActivity.sock.isClosed()) {
				
				try {
					InputStream in = MainActivity.sock.getInputStream();

					// See if any bytes are available from the Middleman
					
					int bytes_avail = in.available();
					if (bytes_avail > 0) {
						
						// If so, read them in and create a string
						
						byte buf[] = new byte[bytes_avail];
						in.read(buf);

						final String s = new String(buf, 0, bytes_avail, "US-ASCII");
		
						//int x = (int) s.charAt(0);
						// As explained in the tutorials, the GUI can not be
						// updated in an asyncrhonous task.  So, update the GUI
						// using the UI thread.
						
						runOnUiThread(new Runnable() {
							public void run() {
								EditText et = (EditText) findViewById(R.id.body);
								et.setText(s);
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