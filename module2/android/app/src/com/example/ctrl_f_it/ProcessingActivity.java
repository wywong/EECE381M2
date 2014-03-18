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
    public Bitmap scaledImage;

    public int beginningCharacterColumn = 0;
    public int lastCharacterColumn = 0;
    public int finalCharacterColumns = 0;
    
    public int beginningCharacterRow = 0;
    public int lastCharacterRow = 0;
    public int finalCharacterRows = 0;
    
    public int height; 
    public int width;
    public int[] characterPixelArray;

    String filePath = "/dev/sentence.bmp";
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
	
	//LOADS THE IMAGE INTO BITMAP FORMAT
    public void loadImage()
    {  
        imageFile = BitmapFactory.decodeFile(filePath);
        
    	height = imageFile.getHeight();
		width = imageFile.getWidth();
		int thresholdValue = Color.LTGRAY;

        ///NEED TO THRESHOLD IMAGES
		finalThresholdImage = imageFile.copy(imageFile.getConfig(), true );

        Threshold(thresholdValue);
        
        //LINE DETECTION
        //int lineVal = 0;
        //while( detectLine() < imageFile.getHeight() ){
        storeCharacter();
        //}
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
    
    public int detectLine(){
    	//scan rows until a black pixel is found
    	//we know that this will be the first row 
    	//don't need to worry about left and right margins as storeCharacters() takes care of it
    	//keep scanning until we find a row with no black pixels
    	//create a new image containing a single line
    	
    	//NEED TO CHANGE IMAGE VARIABLES OF HEIGHT AND WIDTH
    	
    	//returns the last row value 
    	return 0;
    }
    
    
    public void storeCharacter(){
    	//NEED TO STORE WHERE FURTHERS ALONG Y VALUE IS
    	
        int isCharacter = 0;
        int wasBlackPixel = 0;
        String characterName;
        int characterNumber = 0;
        
        int numWhiteColumns = 0;
        Boolean firstCharacter = false;
        
        //go through with columns starting at left most column, then if a black pixel is detected, begin storing columns
        //until we encounter a column with no more black pixels.
        for (int x = 0; x < width; x++){
	        for (int y = 0 ; y < height; y++ ){
	        	if (y == 0){
	        		wasBlackPixel = 0;
	        	}
	        	
	        	int c = finalThresholdImage.getPixel(x, y);
	        	if (c == Color.BLACK){
	        		wasBlackPixel = 1;
	        		if(y > lastCharacterRow){ //this finds the last row containing the letter
	        			lastCharacterRow = y;
	        		}
	        		if(y < beginningCharacterRow){ //this is to mark the top row of the letter
	        			beginningCharacterRow = y;
	        		}
	        		
	        		//WE KNOW THAT THERE IS A LETTER BEGINNING AT THIS COLUMN
	        		if (isCharacter == 0 ){
	        			//if this is the first black pixel, set the flag to store rest of character
	        			isCharacter = 1;
	        			beginningCharacterColumn  = x;
	        			beginningCharacterRow = y;
	
	        			//if the number of white columns is equal or greater than the size of a character (we will have already seen a character)
	        			//we know that it is a space
	        			if (firstCharacter == true && numWhiteColumns >= finalCharacterColumns){
	        				characterName = String.valueOf(characterNumber) + ".bmp";
		        			createSpaceBitmap(characterName);
		        			numWhiteColumns = 0;
		        			characterNumber++;
	        			}
	        		}
	        	}
	        
	        	//once we reach a line with no black, we know its the end of the character so we can store a subset
	        	//of the threshold image as our character image
	        	if (y == height - 1){	        		
	        		if (wasBlackPixel == 0 && isCharacter == 1) {
	        			isCharacter = 0;
	        			
	        			finalCharacterColumns = x - beginningCharacterColumn;
	        			finalCharacterRows = lastCharacterRow - beginningCharacterRow;
	        			
	        			characterName = String.valueOf(characterNumber) + ".bmp";
	        			
	        			createCharacterBitmap(characterName);
	        			characterNumber++;
	        			lastCharacterRow = 0;
	        			firstCharacter = true;
	        		}
	        		else if (wasBlackPixel == 0 && isCharacter == 0 && firstCharacter == true){
	        			numWhiteColumns++;
	        		}
	        	}
	        	
	        	
	        }
        }
    }
    
    public void createCharacterBitmap(String characterName){
    	int whitespaceY;
    	int whitespaceX;
    	int characterDimensions;
    	
    	//create the square based on whether the height or width of the character is bigger
    	if( finalCharacterRows > finalCharacterColumns ){
            whitespaceY = 0;
            whitespaceX = (finalCharacterRows - finalCharacterColumns)/2;
            characterDimensions = finalCharacterRows;
    	}
    	else{
    		whitespaceX = 0;
    		whitespaceY = (finalCharacterColumns - finalCharacterRows)/2;
    		characterDimensions = finalCharacterColumns;
    	}
    	
    	character = Bitmap.createBitmap(finalThresholdImage, beginningCharacterColumn, beginningCharacterRow, finalCharacterColumns, finalCharacterRows );
        
        addWhiteSpace(whitespaceX, whitespaceY, characterDimensions);
        
        scaledImage = Bitmap.createScaledBitmap(charWithWhite, 20, 20, false);
        
        saveCharacterBitmapToFile(characterName);
    }
    
	public void createSpaceBitmap(String characterName){
		//DOESN'T REALLY MATTER THE SIZE, WILL BE RESIZED TO 20X20 ANYWAYS - JUST NEEDS TO BE SQUARE
		
    	//charWithWhite = Bitmap.createBitmap(finalThresholdImage, 0, 0, height, height ); //makes a square character image
    	charWithWhite = Bitmap.createBitmap(finalThresholdImage, 0, 0, finalCharacterRows, finalCharacterRows);
		Canvas whitespace = new Canvas(charWithWhite);
        whitespace.drawRGB(Color.WHITE,Color.WHITE,Color.WHITE);
        whitespace.drawBitmap(character, finalCharacterRows, finalCharacterRows, null);
        
        scaledImage = Bitmap.createScaledBitmap(charWithWhite, 20, 20, false);
        
        saveCharacterBitmapToFile(characterName);
	}
    
    public void addWhiteSpace( int padding_x, int padding_y, int imageDimensions){
        //charWithWhite = Bitmap.createBitmap(character.getWidth() + padding_x, character.getHeight() + padding_y, Bitmap.Config.ALPHA_8);
        
    	//charWithWhite = Bitmap.createBitmap(finalThresholdImage, 0, 0, height, height ); //makes a square character image
    	charWithWhite = Bitmap.createBitmap(finalThresholdImage, 0, 0, imageDimensions, imageDimensions);
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
    	       //charWithWhite.compress(Bitmap.CompressFormat.PNG, 90, out);
    	       scaledImage.compress(Bitmap.CompressFormat.PNG, 90, out);
    	} catch (Exception e) {
    	    e.printStackTrace();
    	} finally {
    	       try{
    	           out.close();
    	       } catch(Throwable ignore) {}
    	}
    }
	
}
