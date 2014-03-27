package com.example.ctrl_f_it;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import org.ejml.simple.SimpleMatrix;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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
    public Bitmap referenceSpace;
    public Bitmap line;
    public Bitmap thresholdBitmap;

    public int beginningCharacterColumn = 0;
    public int lastCharacterColumn = 0;
    public int finalCharacterColumns = 0;
    
    public int beginningCharacterRow = 0;
    public int lastCharacterRow = 0;
    public int finalCharacterRows = 0;
    
    int characterNumber = 0;
    public int height; 
    public int width;
    public int lineHeight;
    public int[] characterPixelArray;

   // String filePath = "sdcard/Pictures/Ctrl_F_It/ALPHA.bmp";
    String filePath = Environment.getExternalStorageDirectory().getPath() + "/twoLines.bmp";
    //String filePath = camActivity.filePath;
    public int startx;
    public int starty = 0;
    
	public static final int INPUT_WIDTH = 12;
	public static final int INPUT = INPUT_WIDTH * INPUT_WIDTH;
	public static final int OUTPUT = 26;
	public static final int HIDDEN_UNITS = 48;
	double[][] theta1 = parseCSV("theta1.csv", HIDDEN_UNITS, INPUT + 1);
	double[][] theta2 = parseCSV("theta2.csv", OUTPUT, HIDDEN_UNITS + 1);
	public static final int GRAY_CONSTANT = 0xFF8C8C8C;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_processing);
		loadImage();
		createReferenceSpace();
		preProcess();
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
	 * Iterates through the processedCharacters vector and applies all of the pre-processing before the actual recognition
	 */
    public void preProcess() {
    	for(int i = 0; i < processedCharacters.size(); i++) {
    		Bitmap img = processedCharacters.get(i);
    		otsuFilter(img);
    		
    		bmpToFile(img, "sdcard/Pictures/Ctrl_F_It/Filter/" + Integer.toString(i) + ".bmp");

    	}
    }
    
    /**
     * Iterates through the processedCharacters vector and applies the recognition algorithm, storing the result into the text vector
     */
    public void bitmapToText() {
    	for(int i = 0; i < processedCharacters.size(); i++) {
    		Bitmap img = processedCharacters.get(i);
    		if(referenceSpace.sameAs(img)) {
    			text.add(' ');
    		} else {
    			text.add(predictChar(theta1, theta2, img));
    		}
    	}
    }

	/**
	 * Applies feed forward propagation to predict the character that has the highest probability of matching the input
	 * @param theta1 2D array containing the theta values for the hidden layer (size HIDDEN_UNITS x INPUT + 1)
	 * @param theta2 2D array containing the theta values for the hidden layer (size OUTPUT x HIDDEN_UNITS + 1)
	 * @param character Filtered character bitmap
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

		for(int i = 0; i < INPUT; i++) {
			Log.d("image", Double.toString(input[i][0]));
		}
		
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
	 * @param inFile Location of the csv file relative to the src directory of the project
	 * @param rows Number of rows to be parsed
	 * @param columns Number of columns to be parsed
	 * @return Array containing the contents of the csv file
	 */
	public double[][] parseCSV(String inFile, int rows, int columns) {
		double[][] parsedValues = new double[rows][columns];
		InputStream inputStream = getClass().getResourceAsStream(inFile);
		BufferedReader br = null;
		String row;
		String[] separatedRow;
		String cvsDelimiter = ",";

		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
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
	 * Unrolls the 2D input matrix column by column into a single column vector
	 * @param input 2D array of pixel values from the input (size INPUT_WIDTH x INPUT_WIDTH)
	 * @param rows Number of rows in the input array
	 * @param columns Number of columns in the input array
	 * @return Unrolled column vector of the input (size INPUT x 1)
	 */
	public double[][] inputUnroll(double[][] input, int rows, int columns) {
		double[][] unrolledInput = new double[INPUT][1];
		int k = 0;

		for(int j = 0; j < columns; j++) {
			for(int i = 0; i < rows; i++) {
				unrolledInput[k][0] = input[i][j];
				k++;
			}
		}

		return unrolledInput;
	}

	/**
	 * Converts the BMP image into a 2D array
	 * @param image Image to be converted
	 * @return Array containing the pixel intensities of the BMP image (size INPUT_WIDTH x INPUT_WIDTH)
	 */
	public double[][] bmpToArray(Bitmap image) {
		double[][] imageArray = new double[image.getHeight()][image.getWidth()];

		for(int yPixel = 0; yPixel < image.getHeight(); yPixel++) {
			for(int xPixel = 0; xPixel < image.getWidth(); xPixel++) {
				//xPixel => column of the array, yPixel => row of the array
				imageArray[yPixel][xPixel] = rgbToGrayscale(image.getPixel(xPixel, yPixel)) / 255;
			}
		}

		return imageArray;
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
	
	/**
	 * Applies the Otsu filtering algorithm to binarize the input image
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
	 * Filters out only the white background and maintains the grayscale of the character
	 * @param image Image to be filtered
	 */
	public void whiteFilter(Bitmap image) {
		for(int yPixel = 0; yPixel < image.getHeight(); yPixel++) {
			for(int xPixel = 0; xPixel < image.getWidth() ; xPixel++) {
				int c = image.getPixel(xPixel, yPixel);
	            
				if (c >= GRAY_CONSTANT){	
					image.setPixel(xPixel, yPixel, Color.WHITE);
				}
			}
		}
	}
	
	/**
	 * Writes the bitmap image to the device's file system
	 * @param image The image to be written, will be saved as a .png
	 * @param filepath Filepath relative to the device's root directory, eg. /sdcard/...
	 */
	public void bmpToFile(Bitmap image, String filepath) {
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

	/**
	 * Loads image from filepath and calls subsequent functions to threshold
	 * and store characters as Bitmap objects
	 */
    public void loadImage()
    {  
        imageFile = BitmapFactory.decodeFile(filePath);
        
    	height = imageFile.getHeight();
		width = imageFile.getWidth();
		int thresholdValue = 0xff7f7a7a;
		
        ///NEED TO THRESHOLD IMAGES
		finalThresholdImage = imageFile.copy(imageFile.getConfig(), true );

        Threshold(thresholdValue);
        
        //LINE DETECTION
        detectLine();
    }
    
    
    /**
	 * scan rows until a black pixel is found
	 * don't need to worry about left and right margins as storeCharacters() takes care of it
	 * keep scanning until we find a row with no black pixels create a new image containing a single line
	 */
    
    public int detectLine(){

    	int pixel = 0;
    	int startLine = 0;
    	int endLine = 0;
    	boolean blackDetected = false;
    	boolean whiteRow = false;
    	int lineNum = 0;
    	boolean isLine = false;
    	
    	for (int y = 0; y < height; y++){
    		blackDetected = false;
    		
    		for (int x = 0; x < width; x++){
    			if ((finalThresholdImage.getPixel(x,y) == Color.BLACK)){
    				blackDetected = true;
    				if (!isLine ){
        				isLine = true;
        				startLine = y;
        			}
    			}
    		}
    		
    		if (!blackDetected && isLine){
    			isLine = false;
    			endLine = y;
    			lineHeight = endLine - startLine;
    			lineNum++;
    			createLineBitmap(startLine, lineNum);
    		}
    		
    	}
    	
    	return 0;
    }
    
    
    /**
   	 * Stores the line based on the dimensions passed in as parameters from the threshold image as well as the original image (for 
   	 * character storage purposes
   	 * @param startY is the beginning row where the line is detected
   	 * @param lineNum is the value of the number of the line detected for storage purposes
   	 */
    public void createLineBitmap(int startY, int lineNum){
    	//need to create threshold bitmap and regular bitmap
        thresholdBitmap = Bitmap.createBitmap(finalThresholdImage, 0, startY, width, lineHeight);
        line = Bitmap.createBitmap(imageFile, 0 , startY, width, lineHeight);

        saveBitmapToFile("thresholdline" + lineNum + ".bmp", thresholdBitmap);
        saveBitmapToFile("line" + lineNum + ".bmp", thresholdBitmap);
        
        storeCharacter();
    }
    
    /**
	 * Assigns the pixels of image loaded from sd card with a value of either white or black
	 * @param requiredThresholdValue the value white designates pixels at either white or black values
	 */
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
    
    /**
   	 * Parses the threshold image file and stores individual characters as a Bitmap object 
   	 * Calls createCharacterBitmap() or createSpaceBitmap(characterName) if a character or space is detected 
   	 */
    public void storeCharacter(){
    	//NEED TO STORE WHERE FURTHERS ALONG Y VALUE IS
    	
        int isCharacter = 0;
        int wasBlackPixel = 0;
        String characterName;
        int largestCharWidth = 0;
        
        int numWhiteColumns = 0;
        Boolean firstCharacter = false;
        
        //go through with columns starting at left most column, then if a black pixel is detected, begin storing columns
        //until we encounter a column with no more black pixels.
        for (int x = 0; x < width; x++){
	        for (int y = 0 ; y < lineHeight; y++ ){ 				
	        	if (y == 0){
	        		wasBlackPixel = 0;
	        	}
	        	
	        	int c = thresholdBitmap.getPixel(x, y);
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
	        	if (y == lineHeight - 1){	        	 				
	        		if (wasBlackPixel == 0 && isCharacter == 1) {
	        			isCharacter = 0;
	        			
	        			finalCharacterColumns = x - beginningCharacterColumn;
	        			
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
    
    /**
  	 * Adds whitespace to image based on which character dimension is smaller (height or width) by calling addWhiteSpace()
  	 * Scales the image with the whitespace based on INPUT_WIDTH
  	 * FOR DEBUGGING PURPOSES: calls saveCharacterBitmapToFile() to view the individual character bitmaps that were detected
  	 * @param characterName name chosen to describe the character when saved to the sd card using the saveCharacterBitmapToFile() function
  	 */
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
    	
        character = Bitmap.createBitmap(line, beginningCharacterColumn, beginningCharacterRow, finalCharacterColumns, finalCharacterRows );

        addWhiteSpace(whitespaceX, whitespaceY, characterDimensions);
        
        scaledImage = Bitmap.createScaledBitmap(charWithWhite, INPUT_WIDTH, INPUT_WIDTH, false);
        
        processedCharacters.add(scaledImage);

        saveBitmapToFile(characterName, scaledImage);
    }
    
    
    /**
  	 * Creates a bitmap object with just white pixels
  	 * Scales the image with the whitespace based on INPUT_WIDTH
  	 * FOR DEBUGGING PURPOSES: calls saveCharacterBitmapToFile() to view the individual character bitmaps that were detected
  	 * @param characterName name chosen to describe the character when saved to the sd card using the saveCharacterBitmapToFile() function
  	 */
	public void createSpaceBitmap(String characterName){
		//DOESN'T REALLY MATTER THE SIZE, WILL BE RESIZED TO INPUT_WIDTHXINPUT_WIDTH ANYWAYS - JUST NEEDS TO BE SQUARE
		
    	charWithWhite = Bitmap.createBitmap(finalThresholdImage, 0, 0, finalCharacterRows, finalCharacterRows);
		Canvas whitespace = new Canvas(charWithWhite);
        whitespace.drawRGB(Color.WHITE,Color.WHITE,Color.WHITE);
        whitespace.drawBitmap(character, finalCharacterRows, finalCharacterRows, null);
        
        scaledImage = Bitmap.createScaledBitmap(charWithWhite, INPUT_WIDTH, INPUT_WIDTH, true);
        
        processedCharacters.add(scaledImage);
        saveBitmapToFile(characterName, scaledImage);
	}
    
	/**
  	 * Creates a Bitmap that is a space character for reference to add to the Vector of characters
  	 */
	public void createReferenceSpace(){
		//DOESN'T REALLY MATTER THE SIZE, WILL BE RESIZED TO INPUT_WIDTHXINPUT_WIDTH ANYWAYS - JUST NEEDS TO BE SQUARE
		
    	charWithWhite = Bitmap.createBitmap(finalThresholdImage, 0, 0, finalCharacterRows, finalCharacterRows);
		Canvas whitespace = new Canvas(charWithWhite);
        whitespace.drawRGB(Color.WHITE,Color.WHITE,Color.WHITE);
        whitespace.drawBitmap(character, finalCharacterRows, finalCharacterRows, null);
        
        referenceSpace = Bitmap.createScaledBitmap(charWithWhite, INPUT_WIDTH, INPUT_WIDTH, false);
	}
	
	
	/**
  	 * Adds whitespace to image to create a square image
  	 * @param padding_x the amount of space to add to the character on the left and right sides of the image
  	 * @param padding_y the amount of space to add to the character on the top and bottom of the image
  	 * @param imageDimensions the length and width of the image - based on the height or width of character (whichever is larger)
  	 */
    public void addWhiteSpace( int padding_x, int padding_y, int imageDimensions){        
    	charWithWhite = Bitmap.createBitmap(finalThresholdImage, 0, 0, imageDimensions, imageDimensions);
    	Canvas whitespace = new Canvas(charWithWhite);
        whitespace.drawRGB(Color.WHITE,Color.WHITE,Color.WHITE); 
        whitespace.drawBitmap(character, padding_x, padding_y, null);
    }

    /**
  	 * Saves the Bitmap object to a .bmp file on the sdCard based on the global variable scaledImage
  	 * @param name the title of the file to be saved to the sd card
  	 * @param image the Bitmap object to be compressed to a file
  	 */
    public void saveBitmapToFile(String name, Bitmap image){
    	FileOutputStream out = null;
    	
    	File dir = Environment.getExternalStorageDirectory();
    	
    	File characterFile = new File(dir, name );
    	
    	try {
    	       out = new FileOutputStream(characterFile);
    	       image.compress(Bitmap.CompressFormat.PNG, 90, out);
    	} catch (Exception e) {
    	    e.printStackTrace();
    	} finally {
    	       try{
    	           out.close();
    	       } catch(Throwable ignore) {}
    	}
    }
}
