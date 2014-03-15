package com.example.ctrl_f_it;

import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class camActivity extends Activity {
	public static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;
	
	private Uri fileUri;
	
	private ImageView imgPreview;
	private Button btnCapturePicture;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
 
        imgPreview = (ImageView) findViewById(R.id.camera_preview);
        btnCapturePicture = (Button) findViewById(R.id.button_capture);
 
        /**
         * Capture image button click event
         * */
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View v) {
                // capture picture
                captureImage();
            }
        });
	}
	 
	 /*
	  * Capturing Camera Image will launch camera app request image capture
	  */
	 private void captureImage() {
	     Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	  
	     fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
	  
	     intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
	  
	     // start the image capture Intent
	     startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
	 }
	 
	 /**
	  * Receiving activity result method will be called after closing the camera
	  * */
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     // if the result is capturing Image
	     if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
	         if (resultCode == RESULT_OK) {
	             // successfully captured the image
	             // display it in image view
	             previewCapturedImage();
	         } else if (resultCode == RESULT_CANCELED) {
	             // user cancelled Image capture
	             Toast.makeText(getApplicationContext(), "User cancelled image capture", Toast.LENGTH_SHORT).show();
	         } else {
	             // failed to capture image
	             Toast.makeText(getApplicationContext(), "Sorry! Failed to capture image", Toast.LENGTH_SHORT).show();
	         }
	     }
	 }
	 
	 /*
     * Display image from a path to ImageView
     */
	 private void previewCapturedImage() {
        try { 
            imgPreview.setVisibility(View.VISIBLE);
 
            // bitmap factory
            BitmapFactory.Options options = new BitmapFactory.Options();
 
            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;
 
            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),options);
 
            imgPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
	 
	 
	 /* File store helper functions */
	 /**
	  * Creating file uri to store image/video
	  */
	 public Uri getOutputMediaFileUri(int type) {
	     return Uri.fromFile(getOutputMediaFile(type));
	 }
	  
	 /*
	  * returning image / video
	  */
	 private static File getOutputMediaFile(int type) {
	  
	     // External sdcard location
	     File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), 
	    		 "Ctrl_F_It");
	  
	     // Create the storage directory if it does not exist
	     if (!mediaStorageDir.exists()) {
	         if (!mediaStorageDir.mkdirs()) {
	             Log.d("Ctrl_F_It", "Oops! Failed create " + "Ctrl_F_It" + " directory");
	             return null;
	         }
	     }
	  
	     // Create a media file name
	     String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	     File mediaFile;
	     if (type == MEDIA_TYPE_IMAGE) {
	         mediaFile = new File(mediaStorageDir.getPath() + File.separator
	                 + "IMG_" + timeStamp + ".bmp");
	     } else {
	         return null;
	     }
	  
	     return mediaFile;
	 }
}
