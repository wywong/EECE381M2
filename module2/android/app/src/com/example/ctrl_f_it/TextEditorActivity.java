package com.example.ctrl_f_it;

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
    private int textIndex = 0;
    private String searchTextPrev = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        
        setContentView(R.layout.text_edit);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);

        Button confirmButton = (Button) findViewById(R.id.confirm);
        Button searchButton = (Button) findViewById(R.id.search);
        
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
				int tt;
				boolean found = false;
				
				String searchText = findViewById(R.id.searchString).toString();
				if(searchText != searchTextPrev) {
					searchTextPrev = searchText;
					textIndex = 0;
				}
				
				int searchSize = searchText.length();
				String text = mBodyText.getText().toString();
				
				for(tt = textIndex; tt < text.length() - searchSize; tt++){
					if(text.regionMatches(0, searchText, tt, searchSize)){
						textIndex = tt;
						found = true;
						break;
					}
				}
				
				if(found) {
					mBodyText.setSelection(textIndex, textIndex+searchSize);
				}
				
				if(tt == text.length() - searchSize)
					textIndex = 0;
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
}