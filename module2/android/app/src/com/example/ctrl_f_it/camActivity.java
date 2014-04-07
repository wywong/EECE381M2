package com.example.ctrl_f_it;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
	public static final int CROP_IMAGE_REQUEST_CODE = 200;
	public static final int MEDIA_TYPE_IMAGE = 1;

	private Uri fileUri;
	private Uri tempUri;
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
	private Button btnFilter;

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
        btnFilter = (Button) findViewById(R.id.button_filter);

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
        		deleteImage(fileUri);
                captureImage();
            }
        });

        btnRotateLeft.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
                // rotate picture counter clockwise
        		currentRotate -= 1;
        		rotateBitmap();
        		imgPreview.setImageBitmap(rotatedBitmap);
            }
        });

        btnRotateRight.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
                // rotate picture counter clockwise
        		currentRotate += 1;
        		rotateBitmap();
        		imgPreview.setImageBitmap(rotatedBitmap);
            }
        });
        
        btnFilter.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
                //get image from file and filter it to preview before running ocr
        		BitmapFactory.Options options = new BitmapFactory.Options();
        		Bitmap otsu = BitmapFactory.decodeFile(fileUri.getPath(),options);
        		
        		otsuFilter(otsu);
        		
        		imgPreview.setImageBitmap(otsu);
            }
        });
        
	}

	 /*
	  * Capturing Camera Image will launch camera app request image capture
	  */
	 private void captureImage() {
	     Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

	     tempUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

	     intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);

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
	 private void deleteImage(Uri uri) {
		 imgPreview.setVisibility(View.GONE);
		 File file = new File(uri.getPath());
		 file.delete();
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
	             // prompt user to crop image
	             crop();
	         } else if (resultCode == RESULT_CANCELED) {
	             // user cancelled Image capture
	             Toast.makeText(getApplicationContext(), "User cancelled image capture", Toast.LENGTH_SHORT).show();
	         } else {
	             // failed to capture image
	             Toast.makeText(getApplicationContext(), "Sorry! Failed to capture image", Toast.LENGTH_SHORT).show();
	         }
	     }
	     else if(requestCode == CROP_IMAGE_REQUEST_CODE) {
	    	 deleteImage(tempUri);
	    	 previewCapturedImage();
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
            rotatedBitmap = bitmap;

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
	 private void rotateBitmap() {
		 int width = bitmap.getWidth();
		 int height = bitmap.getHeight();
		 Matrix rotationMatrix = new Matrix();

		 rotationMatrix.postRotate(currentRotate, width / 2, height / 2);
		 rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, rotationMatrix, true);
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
	
	/*
	 * prompts the user to crop the image
	 */
	private void crop() {
		try {
			fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
			//call the standard crop action intent (the user device may not support it)
			Intent cropIntent = new Intent("com.android.camera.action.CROP"); 
			//indicate image type and Uri
			cropIntent.setDataAndType(tempUri, "image/*");
			//set crop properties
			cropIntent.putExtra("crop", "true");
			//retrieve data on return
			cropIntent.putExtra("return-data", true);
			cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
			//start the activity - we handle returning in onActivityResult
			startActivityForResult(cropIntent, CROP_IMAGE_REQUEST_CODE);
		}
		catch(ActivityNotFoundException anfe){
		    //display an error message
		    String errorMessage = "Whoops - your device doesn't support the crop action!";
		    Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
		    toast.show();
		}
	}
	
	/**
	 * Applies the   filtering algorithm to binarize the input image
	 * @param image Image to be filtered
	 */
	public void otsuFilter(Bitmap image) {
		int[] histogram = new int[256];
		int grayscaleVal;
		int total = image.getHeight() * image.getWidth();
		double sum = 0;
		double sumBG = 0;
		double weightBG = 0;
		double weightFG = 0;
		double meanBG;
		double meanFG;
		double betweenVariance;
		double maxVariance = 0;
		int threshold = 0;
		
		
		
		//Create a histogram of the number of pixels for each grayscale value
		for(int yPixel = 0; yPixel < image.getHeight(); yPixel++) {
			for(int xPixel = 0; xPixel < image.getWidth(); xPixel++) {
				grayscaleVal = rgbToGrayscale(image.getPixel(xPixel, yPixel));
				histogram[grayscaleVal]++;
			}
		}
		
		//Total grayscale value
		for(int i = 0; i < 256; i++) {
			sum += i * histogram[i];
		}
		
		for(int i = 0; i < 256; i++) {
			//Weight of background, continues to next iteration if 0
			weightBG += histogram[i];
			if(weightBG == 0) continue;
			
			//Weight of foreground, breaks the loop since all later foreground weights will also be 0
			weightFG = total - weightBG;
			if(weightFG == 0) break;
			
			sumBG += i * histogram[i];
			
			//Mean grayscale of background
			meanBG = sumBG / weightBG;
			//Mean grayscale of foreground
			meanFG = (sum - sumBG) / weightFG;
			
			//Between class variance
			betweenVariance = weightBG * weightFG * (meanBG - meanFG) * (meanBG - meanFG);
			
			//Records new threshold value if there is a new maximum between class variance
			if(betweenVariance > maxVariance) {
				maxVariance = betweenVariance;
				threshold = i;
			}
		}
		
		//Actual thresholding of image
		for(int yPixel = 0; yPixel < image.getHeight(); yPixel++) {
			for(int xPixel = 0; xPixel < image.getWidth(); xPixel++) {
				grayscaleVal = rgbToGrayscale(image.getPixel(xPixel, yPixel));

				if(grayscaleVal >= threshold) {
					image.setPixel(xPixel, yPixel, Color.WHITE);
				} else {
					image.setPixel(xPixel, yPixel, Color.BLACK);
				}
			}
		}
	}
	
	/**
	 * Converts a pixel value into grayscale
	 * @param pixel Integer value that contains ARGB information
	 * @return Grayscale value by taking the average of the RGB values
	 */
	public int rgbToGrayscale(int pixel) {
		int R = Color.red(pixel);
		int G = Color.green(pixel);
		int B = Color.blue(pixel);
		double grayscaleVal = (R + G + B) / 3.0;
		
		return (int)grayscaleVal;
	}
	
}
