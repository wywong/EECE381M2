package com.example.ctrl_f_it;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
	private float currentRotate;
	// Original bitmap from camera
	private Bitmap bitmap;
	// New bitmap after rotation, this is what's saved after confirm is pressed
	private Bitmap rotatedBitmap;

	private ImageView imgPreview;
	private Button btnCapturePicture;
	private Button btnConfirm;
	private Button btnRetry;
	private Button btnRotateLeft;
	private Button btnRotateRight;

	public static String filePath;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imgPreview = (ImageView) findViewById(R.id.camera_preview);
        btnCapturePicture = (Button) findViewById(R.id.button_capture);
        btnConfirm = (Button) findViewById(R.id.button_confirm);
        btnRetry = (Button) findViewById(R.id.button_retry);
        btnRotateLeft = (Button) findViewById(R.id.button_rotateleft);
        btnRotateRight = (Button) findViewById(R.id.button_rotateright);

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

        btnConfirm.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
                // capture picture
                saveImage();
                finish();
            }
        });

        btnRetry.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
                // capture picture
        		deleteImage();
                captureImage();
            }
        });

        btnRotateLeft.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
                // rotate picture counter clockwise
        		currentRotate -= 1;
        		rotatedBitmap = rotateBitmap();
        		imgPreview.setImageBitmap(rotatedBitmap);
            }
        });

        btnRotateRight.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
                // rotate picture counter clockwise
        		currentRotate += 1;
        		rotatedBitmap = rotateBitmap();
        		imgPreview.setImageBitmap(rotatedBitmap);
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

	 /*
	  * Save image captured and set public string to be most recent picture path
	  */
	 private void saveImage() {
	     filePath = fileUri.getPath();
	     bmpToFile(rotatedBitmap, filePath);
	 }

	 /*
	  * Delete most recently captured image
	  */
	 private void deleteImage() {
		 imgPreview.setVisibility(View.GONE);
		 File file = new File(fileUri.getPath());
		 boolean deleted = file.delete();
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
            btnConfirm.setVisibility(View.VISIBLE);
            btnRetry.setVisibility(View.VISIBLE);
            btnRotateLeft.setVisibility(View.VISIBLE);
            btnRotateRight.setVisibility(View.VISIBLE);

            // bitmap factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            bitmap = BitmapFactory.decodeFile(fileUri.getPath(),options);

            imgPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        finally{
        	Toast.makeText(getApplicationContext(),
        	"If text matches given lines click confirm otherwise click retry to try again", Toast.LENGTH_LONG).show();
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

	 /*
	  * draw new bitmap with rotation
	  */
	 private Bitmap rotateBitmap() {
		 int width = bitmap.getWidth();
		 int height = bitmap.getHeight();
		 Matrix rotationMatrix = new Matrix();
		 Bitmap rotatedBitmap;

		 rotationMatrix.postRotate(currentRotate, width / 2, height / 2);
		 return rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, rotationMatrix, true);
	 }

	/*
	 * write bitmap to file
	 */
	private void bmpToFile(Bitmap image, String filepath) {
		FileOutputStream out = null;
		File fileName = new File(filepath);
		try {
			out = new FileOutputStream(fileName);
			image.compress(Bitmap.CompressFormat.PNG, 100, out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{
				out.close();
			} catch(Throwable ignore) {}
		}
	}
}
