package com.example.ctrl_f_it;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.ejml.simple.SimpleMatrix;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;


public class ProcessingActivity extends Activity {
	
	public Vector<Bitmap> processedCharacters = new Vector<Bitmap>();
	public Vector<Character> text = new Vector<Character>();
	public Bitmap imageFile;
	public Bitmap finalThresholdImage;
    public Bitmap character;
    public Bitmap charWithWhite;
    public Bitmap scaledImage;
    public Bitmap referenceSpaceBitmap;

    public int beginningCharacterColumn = 0;
    public int lastCharacterColumn = 0;
    public int finalCharacterColumns = 0;
    
    public int beginningCharacterRow = 0;
    public int lastCharacterRow = 0;
    public int finalCharacterRows = 0;
    
    public int height; 
    public int width;
    public int[] characterPixelArray;

    String filePath = "sdcard/Pictures/Ctrl_F_It/ALPHA.bmp";
    //String filePath = camActivity.filePath;
    public int startx;
    public int starty = 0;
    
	public static final int INPUT_WIDTH = 20;
	public static final int INPUT = 400;
	public static final int OUTPUT = 26;
	public static final int HIDDEN_UNITS = 72;
	double[][] theta1 = parseCSV("sdcard/Ctrl_F_It/theta1.csv", HIDDEN_UNITS, INPUT + 1);
	double[][] theta2 = parseCSV("sdcard/Ctrl_F_It/theta2.csv", OUTPUT, HIDDEN_UNITS + 1);
	public static final int GRAY_CONSTANT = Color.LTGRAY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_processing);
		
		loadImage();
		filter();
		createReferenceSpace();
		
		bitmapToText();
		
		for(int i = 0; i < text.size(); i++) {
			Log.d("prediction", Character.toString(text.get(i)));
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.processing, menu);
		return true;
	}

	/**
	 * Applies feed forward propagation to predict the character that has the highest probability of matching the input
	 * @param theta1 2D array containing the theta values for the hidden layer (size HIDDEN_UNITS x INPUT + 1)
	 * @param theta2 2D array containing the theta values for the hidden layer (size OUTPUT x HIDDEN_UNITS + 1)
	 * @param character Unfiltered character bitmap
	 * @return The character that has the highest probability of matching the input
	 */
	public char predictChar(double[][] theta1, double[][] theta2, Bitmap character) {
		SimpleMatrix theta1Array = new SimpleMatrix(theta1);
		SimpleMatrix theta2Array = new SimpleMatrix(theta2);
		double[][] input = inputUnroll(bmpToArray(character), INPUT_WIDTH, INPUT_WIDTH);
		SimpleMatrix inputArray = new SimpleMatrix(input);
		SimpleMatrix inputArrayWithBias = new SimpleMatrix(INPUT + 1, 1);
		SimpleMatrix h1Array;
		SimpleMatrix h1ArrayWithBias = new SimpleMatrix(HIDDEN_UNITS + 1, 1);
		SimpleMatrix outputArray;
		double sigmoidVal;
		int bestMatch = 0;

		//Initialize input array to include the bias value
		inputArrayWithBias.set(0, 0, 1);
		inputArrayWithBias.insertIntoThis(1, 0, inputArray);

		//sigmoid(theta1 * input)
		h1Array = theta1Array.mult(inputArrayWithBias);
		h1ArrayWithBias.set(0, 0, 1);
		for(int i = 0; i < HIDDEN_UNITS; i++) {
			sigmoidVal = 1 / (1 + Math.exp(-h1Array.get(i, 0)));
			h1Array.set(i, 0, sigmoidVal);
		}
		h1ArrayWithBias.insertIntoThis(1, 0, h1Array);

		//sigmoid(theta2 * h1)
		outputArray = theta2Array.mult(h1ArrayWithBias);
		for(int i = 0; i < OUTPUT; i++) {
			sigmoidVal = 1 / (1 + Math.exp(-outputArray.get(i, 0)));
			outputArray.set(i, 0, sigmoidVal);
		}

		for(int i = 0; i < OUTPUT; i++) {
			if(outputArray.get(i, 0) > outputArray.get(bestMatch, 0)) {
				bestMatch = i;
			}
		}

		return (char)(65 + bestMatch);
	}

	/**
	 * Parses a csv file and stores the contents into a 2d double array
	 * @param inFile Location of the csv file relative to the root directory of the android device
	 * @param rows Number of rows to be parsed
	 * @param columns Number of columns to be parsed
	 * @return Array containing the contents of the csv file
	 */
	public double[][] parseCSV(String inFile, int rows, int columns) {
		double[][] parsedValues = new double[rows][columns];
		BufferedReader br = null;
		String row;
		String[] separatedRow;
		String cvsDelimiter = ",";

		try {
			br = new BufferedReader(new FileReader(inFile));
			for(int i = 0; i < rows; i++ ) {
				row = br.readLine();
				separatedRow = row.split(cvsDelimiter);
				for(int j = 0; j < columns; j++) {
					parsedValues[i][j] = Double.parseDouble(separatedRow[j]);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return parsedValues;
	}

	/**
	 * Unrolls the 2D input matrix into a single column vector
	 * @param input 2D array of pixel values from the input (size INPUT_WIDTH x INPUT_WIDTH)
	 * @param rows Number of rows in the input array
	 * @param columns Number of columns in the input array
	 * @return Unrolled column vector of the input (size INPUT x 1)
	 */
	public double[][] inputUnroll(double[][] input, int rows, int columns) {
		double[][] unrolledInput = new double[INPUT][1];
		int k = 0;

		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				unrolledInput[k][0] = input[i][j];
				k++;
			}
		}

		return unrolledInput;
	}

	/**
	 * Converts the BMP image into a 2D array
	 * @param image Bitmap image to be converted
	 * @return Array containing the pixel information of the BMP image (size INPUT_WIDTH x INPUT_WIDTH)
	 */
	public double[][] bmpToArray(Bitmap image) {
		double[][] imageArray = new double[INPUT_WIDTH][INPUT_WIDTH];

		for(int yPixel = 0; yPixel < INPUT_WIDTH; yPixel++) {
			for(int xPixel = 0; xPixel < INPUT_WIDTH; xPixel++) {
				//xPixel => column of the array, yPixel => row of the array
				imageArray[yPixel][xPixel] = rgbToGrayscale(image.getPixel(xPixel, yPixel))/255;
			}
		}

		return imageArray;
	}

	/**
	 * Converts a pixel value into grayscale
	 * @param pixel Integer value that contains ARGB information
	 * @return Grayscale value by taking the average of the RGB values
	 */
	public double rgbToGrayscale(int pixel) {
		int R = Color.red(pixel);
		int G = Color.green(pixel);
		int B = Color.blue(pixel);

		return (R + G + B) / 3;
	}
	
	//LOADS THE IMAGE INTO BITMAP FORMAT
    public void loadImage()
    {  
        imageFile = BitmapFactory.decodeFile(filePath);
        
    	height = imageFile.getHeight();
		width = imageFile.getWidth();
		//int thresholdValue = Color.LTGRAY;
		int thresholdValue = 0xff989695;
		//int thresholdValue = 0xff646464;
		
        ///NEED TO THRESHOLD IMAGES
		finalThresholdImage = imageFile.copy(imageFile.getConfig(), true );

        Threshold(thresholdValue);
        
        //LINE DETECTION
        //int lineVal = 0;
        //while( detectLine() < imageFile.getHeight() ){
        storeCharacter();
        //}
        ///saveCharacterBitmapToFile( "test.bmp" );
    }
    
    public void Threshold(int requiredThresholdValue) {
		
		for (int y = 0; y < height ; y++)
		    {
		        for (int x = 0; x < width ; x++)
		        {
		            int c = imageFile.getPixel(x, y);
		            
					if (c >= requiredThresholdValue){	
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
    	
    	int y = 0;
    	int x = 0;
    	int pixel = 0;
    	
    	while (pixel != Color.BLACK){
    		 
    	}
    	
    	//returns the last row value 
    	return 0;
    }
    
    
    public void storeCharacter(){
    	//NEED TO STORE WHERE FURTHERS ALONG Y VALUE IS
    	
        int isCharacter = 0;
        int wasBlackPixel = 0;
        String characterName;
        int characterNumber = 0;
        int largestCharWidth = 0;
        
        int numWhiteColumns = 0;
        Boolean firstCharacter = false;
        
        //go through with columns starting at left most column, then if a black pixel is detected, begin storing columns
        //until we encounter a column with no more black pixels.
        for (int x = 0; x < width; x++){
	        for (int y = 0 ; y < height; y++ ){ 				//NEED TO CHANGE TO LINE HEIGHT 
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
	        			if (firstCharacter == true && numWhiteColumns >= largestCharWidth){
	        				characterName = String.valueOf(characterNumber) + ".bmp";
		        			createSpaceBitmap(characterName);
		        			numWhiteColumns = 0;
		        			characterNumber++;
	        			}
	        		}
	        	}
	        
	        	//once we reach a line with no black, we know its the end of the character so we can store a subset
	        	//of the threshold image as our character image
	        	if (y == height - 1){	        	 				//CHANGE TO LINE HEIGHT	
	        		if (wasBlackPixel == 0 && isCharacter == 1) {
	        			isCharacter = 0;
	        			
	        			finalCharacterColumns = x - beginningCharacterColumn;
	        			
	        			//RECOGNIZE PERIODS IF THEY ARE ALONG THE BOTTOM LINE --- IMPLEMENT
	        			//IF THERE IS MORE THAN 4 SPACES -- ITS A MARGIN
	        			
	        			//only recognizes characters if they are larger than one pixel long
	        			if (finalCharacterColumns > 1){
	        				finalCharacterRows = lastCharacterRow - beginningCharacterRow;
	        				
	        				if (finalCharacterColumns > largestCharWidth){
	        					largestCharWidth = finalCharacterColumns;
	        				}
	        			
	        				characterName = String.valueOf(characterNumber) + ".bmp";
	        			
	        				createCharacterBitmap(characterName);
	        				characterNumber++;
	        				lastCharacterRow = 0;
	        				firstCharacter = true;
	        			}
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
    	
    	//character = Bitmap.createBitmap(finalThresholdImage, beginningCharacterColumn, beginningCharacterRow, finalCharacterColumns, finalCharacterRows );
        character = Bitmap.createBitmap(imageFile, beginningCharacterColumn, beginningCharacterRow, finalCharacterColumns, finalCharacterRows );
        
        addWhiteSpace(whitespaceX, whitespaceY, characterDimensions);
        
        scaledImage = Bitmap.createScaledBitmap(charWithWhite, 20, 20, false);
        
        processedCharacters.add(scaledImage);
    }
    
	public void createSpaceBitmap(String characterName){
		//DOESN'T REALLY MATTER THE SIZE, WILL BE RESIZED TO 20X20 ANYWAYS - JUST NEEDS TO BE SQUARE
		
    	charWithWhite = Bitmap.createBitmap(finalThresholdImage, 0, 0, finalCharacterRows, finalCharacterRows);
		Canvas whitespace = new Canvas(charWithWhite);
        whitespace.drawRGB(Color.WHITE,Color.WHITE,Color.WHITE);
        whitespace.drawBitmap(character, finalCharacterRows, finalCharacterRows, null);
        
        scaledImage = Bitmap.createScaledBitmap(charWithWhite, 20, 20, false);
        
        processedCharacters.add(scaledImage);
	}
	
	public void createReferenceSpace(){
    	charWithWhite = Bitmap.createBitmap(finalThresholdImage, 0, 0, finalCharacterRows, finalCharacterRows);
		Canvas whitespace = new Canvas(charWithWhite);
        whitespace.drawRGB(Color.WHITE,Color.WHITE,Color.WHITE);
        whitespace.drawBitmap(character, finalCharacterRows, finalCharacterRows, null);
        
        referenceSpaceBitmap = Bitmap.createScaledBitmap(charWithWhite, 20, 20, false);	
	}
    
    public void addWhiteSpace( int padding_x, int padding_y, int imageDimensions){        
    	charWithWhite = Bitmap.createBitmap(finalThresholdImage, 0, 0, imageDimensions, imageDimensions);
    	Canvas whitespace = new Canvas(charWithWhite);
        whitespace.drawRGB(Color.WHITE,Color.WHITE,Color.WHITE); 
        whitespace.drawBitmap(character, padding_x, padding_y, null);
    }
    
    public void bitmapToText() {
    	for(int i = 0; i < processedCharacters.size(); i++) {
    		if(referenceSpaceBitmap.sameAs(processedCharacters.get(i))) {
    			text.add(' ');
    		} else {
    			text.add(predictChar(theta1, theta2, processedCharacters.get(i)));
    		}
    	}
    }

    public void filter() {
    	for(int i = 0; i < processedCharacters.size(); i++) {
    		Bitmap img = processedCharacters.get(i);
			for (int y = 0; y < img.getHeight(); y++)
		    {
		        for (int x = 0; x < img.getWidth() ; x++)
		        {
		            int c = img.getPixel(x, y);
		            
					if (c >= GRAY_CONSTANT){	
		            	img.setPixel(x, y, Color.WHITE);
					}
		        }
		    }
    	}
    }
}
