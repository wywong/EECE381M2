package com.example.ctrl_f_it;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;

public class ProcessingActivity extends Activity {
	
	public Bitmap imageFile;
	public Bitmap finalThresholdImage;
    public Bitmap character;
    public Bitmap charWithWhite;
    
    public int finalCharacterRows = 0;
    public int beginningCharacterRow = 0;
    public int height; 
    public int width;
    public int[] characterPixelArray;

    String filePath = "/dev/characters.bmp";
    public int startx;
    public int starty = 0;
    


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_processing);
		loadImage();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.processing, menu);
		return true;
	}
	
	//LOADS THE IMAGE INTO 
    public void loadImage()
    {  
        imageFile = BitmapFactory.decodeFile(filePath);
        
    	height = imageFile.getHeight();
		width = imageFile.getWidth();
		int thresholdValue = Color.LTGRAY;

        ///NEED TO THRESHOLD IMAGES
		finalThresholdImage = imageFile.copy(imageFile.getConfig(), true );
        //finalThresholdImage = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
        Threshold(thresholdValue);
        
        storeCharacter();
        //saveCharacterBitmapToFile( "test.bmp" );
    }
    
    public void Threshold(int requiredThresholdValue) {
		
		for (int y = 0; y < height ; y++)
		    {
		        for (int x = 0; x < width ; x++)
		        {
		            int c = imageFile.getPixel(x, y);
		            
					if (c >= Color.DKGRAY){	
		            	finalThresholdImage.setPixel(x, y, Color.WHITE);
					}
					else {
						finalThresholdImage.setPixel(x, y, Color.BLACK);
					}
		        }
		    }
    }
    
    public void storeCharacter(){
        int isCharacter = 0;
        int wasBlackPixel = 0;
        String characterName;
        int characterNumber = 0;

        //go through with columns starting at left most column, then if a black pixel is detected, begin storing columns
        //until we encounter a row with no more black pixels.
        for (int x = 0; x < width; x++){
	        for (int y = 0 ; y < height; y++ ){
	        	if (y == 0){
	        		wasBlackPixel = 0;
	        	}
	        	
	        	int c = finalThresholdImage.getPixel(x, y);
	        	if (c == Color.BLACK){
	        		wasBlackPixel = 1;
	        		
	        		//WE KNOW THAT THERE IS A LETTER BEGINNING AT THIS COLUMN
	        		if (isCharacter == 0 ){
	        			//if this is the first black pixel, set the flag to store rest of character
	        			isCharacter = 1;
	        			beginningCharacterRow  = x;
	        		}
	        	}
	        
	        	//once we reach a line with no black, we know its the end of the character so we can store a subset
	        	//of the threshold image as our character image
	        	if (y == height - 1){	        		
	        		if (wasBlackPixel == 0 && isCharacter == 1) {
	        			isCharacter = 0;
	        			startx = beginningCharacterRow;
	        			finalCharacterRows = x - beginningCharacterRow;
	        			
	        			characterName = "char" + String.valueOf(characterNumber) + ".bmp";
	        			
	        			createCharacterBitmap(characterName);
	        			characterNumber++;
	        		}
	        		
	        	}
	        	
	        	
	        }
        }
    }
    
    public void createCharacterBitmap(String characterName){
    	
        int whitespace = finalCharacterRows/2;
        
        //character = Bitmap.createBitmap(finalCharacterArray, characterBitmapWidth , height, Bitmap.Config.ALPHA_8);
        character = Bitmap.createBitmap(finalThresholdImage, startx, starty, finalCharacterRows, height );
        
        addWhiteSpace(whitespace, 0);
        saveCharacterBitmapToFile(characterName);
    }
    
    public void addWhiteSpace( int padding_x, int padding_y){
        //charWithWhite = Bitmap.createBitmap(character.getWidth() + padding_x, character.getHeight() + padding_y, Bitmap.Config.ALPHA_8);
        
    	charWithWhite = Bitmap.createBitmap(finalThresholdImage, 0, 0, finalCharacterRows*2, height );
    	Canvas whitespace = new Canvas(charWithWhite);
        whitespace.drawRGB(Color.WHITE,Color.WHITE,Color.WHITE); 
        whitespace.drawBitmap(character, padding_x, padding_y, null);
    }
      

    public void saveCharacterBitmapToFile( String name){
    	FileOutputStream out = null;
    	
    	File dir = Environment.getExternalStorageDirectory();
    	
    	File characterFile = new File(dir, name );
    	
    	try {
    	       out = new FileOutputStream(characterFile);
    	       //finalThresholdImage.compress(Bitmap.CompressFormat.PNG, 90, out);
    	       //character.compress(Bitmap.CompressFormat.PNG, 90, out);
    	       charWithWhite.compress(Bitmap.CompressFormat.PNG, 90, out);
    	} catch (Exception e) {
    	    e.printStackTrace();
    	} finally {
    	       try{
    	           out.close();
    	       } catch(Throwable ignore) {}
    	}
    }
	
}
