package com.example.ctrl_f_it;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Vector;
import java.lang.Math;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ejml.simple.SimpleMatrix;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
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
    public int characterWidth;
    
    //String filePath = Environment.getExternalStorageDirectory().getPath() + "/uppercase_bold.bmp";
    String filePath;

    public int startx;
    public int starty = 0;
    
    public static final int SCALEDDIMENSION = 30;
    
	public static final int INPUT_WIDTH = 30;
	public static final int INPUT = INPUT_WIDTH * INPUT_WIDTH + 4*INPUT_WIDTH;
	public static final int OUTPUT = 26;
	public static final int HIDDEN_UNITS = 48;
	double[][] theta1 = parseCSV("theta1.csv", HIDDEN_UNITS, INPUT + 1);
	double[][] theta2 = parseCSV("theta2.csv", OUTPUT, HIDDEN_UNITS + 1);
	public static final int GRAY_CONSTANT = 0xFF8C8C8C;
    
    private Set<String> validWords;
    private String dictionary = "wordlist.txt";
    private ArrayList<String> filters;
    private ArrayList<char[]> replacers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_processing);

		Intent intent = getIntent();
	    filePath = intent.getStringExtra("position");
	    Log.d("Testing", filePath);

		loadImage();
		Log.d("SIZE", "bmpToText" + Integer.toString(processedCharacters.size()));
		initDict();
		createReferenceSpace();
		bitmapToText();
		for(int i = 0; i < text.size(); i++) {
			Log.d("prediction", Character.toString(text.get(i)));
		}

		String joined = "";
		for(char c : text) {
			joined += Character.toString(c);
		}
		String[] words = joined.split("\\s+");
		ArrayList<String> filteredWords = new ArrayList<String>();
		for(String w : words) {
			filteredWords.add(filterWord(w));
		}
		for(String w : filteredWords) {
			Log.d("word", w);
		}

		String body = "";
		for(int i = 0; i < filteredWords.size(); i++) {
			body += filteredWords.get(i) + " ";
		}
		
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String title = "NOTE_" + timeStamp;
		NotesDbAdapter mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();
		mDbHelper.createNote(title, body);
		finish();
	}
	
	public boolean isWord(String s) {
		return validWords.contains(s.toLowerCase());
	}
	
	public String filterWord(String s) {
		String corStr = new String(s);
		if(!isWord(corStr)) {
			int numFilters = filters.size();
			for(int ii = 0; ii < numFilters; ++ii) {
				String pattern = "(" + filters.get( ii ) + ")";
				Pattern r = Pattern.compile(pattern);
				Matcher m = r.matcher(corStr);
				if(m.find()) {
					String match = m.group(0);
					for(char c : replacers.get(ii)) {
						String rStr = corStr.replaceAll("[" + match + "]",
											Character.toString(c));
						if(isWord(rStr)) {
							corStr = new String(rStr);
							break;
						}
					}
				}
			}
		}
		return corStr;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.processing, menu);
		return true;
	}

    /**
     * Iterates through the processedCharacters vector and applies the recognition algorithm, storing the result into the text vector
     */
    public void bitmapToText() {
    	Log.d("SIZE", "bmpToText" + Integer.toString(processedCharacters.size()));
    	for(int i = 0; i < processedCharacters.size(); i++) {
    		Bitmap img = processedCharacters.get(i);
    		if(referenceSpace.sameAs(img)) {
    			text.add(' ');	
    		//DETECT IF PERIOD
    		} else if(isPeriod(img)) {
    			text.add('.');
    		} else {
    			text.add(predictChar(theta1, theta2, img));
    		}
    	}
    }
    
    public void initDict() {
		filters = new ArrayList<String>();
		replacers = new ArrayList<char[]>();

		filters.add("[IL]");
		replacers.add(new char[]{ 'I', 'L' });
		filters.add("[DO]");
		replacers.add(new char[]{ 'D', 'O' });
		filters.add("[MX]");
		replacers.add(new char[]{ 'M', 'X' });

		// Load up dictionary words into hashset
		validWords = new HashSet();
		InputStream inputStream = getClass().getResourceAsStream(dictionary);
		BufferedReader br = null;
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			while((line = br.readLine()) != null) {
				validWords.add(line);
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
    }
    
    public double[][] prepareInput(double [][] imageArray) {
    	double[][] unrolled = inputUnroll(imageArray, INPUT_WIDTH, INPUT_WIDTH);
    	double[] C_L = new double[INPUT_WIDTH];
    	double[] C_R = new double[INPUT_WIDTH];
    	double[] C_T = new double[INPUT_WIDTH];
    	double[] C_B = new double[INPUT_WIDTH];
    	double[][] result = new double[INPUT_WIDTH*INPUT_WIDTH + 4*INPUT_WIDTH][1];

    	double threshold = 0.94;
    	for(int ii=0; ii < INPUT_WIDTH; ++ii) {
    		for(int jj = 0; jj < INPUT_WIDTH; ++jj) {
    			if(imageArray[ii][jj] > threshold) {
    				C_L[ii] += 1;
    			} else {
    				break;
    			}
    		}
    		for(int jj = INPUT_WIDTH-1; jj >= 0; --jj) {
    			if(imageArray[ii][jj] > threshold) {
    				C_L[ii] += 1;
    			} else {
    				break;
    			}
    		}
    		C_L[ii] /= INPUT_WIDTH;
    		C_R[ii] /= INPUT_WIDTH;
    	}
    	for(int ii=0; ii < INPUT_WIDTH; ++ii) {
    		for(int jj = 0; jj < INPUT_WIDTH; ++jj) {
    			if(imageArray[ii][jj] > threshold) {
    				C_T[ii] += 1;
    			} else {
    				break;
    			}
    		}
    		for(int jj = INPUT_WIDTH-1; jj >= 0; --jj) {
    			if(imageArray[ii][jj] > threshold) {
    				C_B[ii] += 1;
    			} else {
    				break;
    			}
    		}
    		C_T[ii] /= INPUT_WIDTH;
    		C_B[ii] /= INPUT_WIDTH;
    	}
    	for(int ii = 0; ii < INPUT_WIDTH*INPUT_WIDTH; ++ii) {
    		result[ii][0] = unrolled[ii][0];
    	}
    	int offset = INPUT_WIDTH * INPUT_WIDTH;
    	for(int ii = 0; ii < INPUT_WIDTH; ++ii) {
    		result[offset+ii][0] = C_L[ii];
    	}
    	offset += INPUT_WIDTH;
    	for(int ii = 0; ii < INPUT_WIDTH; ++ii) {
    		result[offset+ii][0] = C_R[ii];
    	}
    	offset += INPUT_WIDTH;
    	for(int ii = 0; ii < INPUT_WIDTH; ++ii) {
    		result[offset+ii][0] = C_T[ii];
    	}
    	offset += INPUT_WIDTH;
    	for(int ii = 0; ii < INPUT_WIDTH; ++ii) {
    		result[offset+ii][0] = C_B[ii];
    	}
    	return result;
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
		double[][] input = prepareInput(bmpToArray(character));
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
		double[][] unrolledInput = new double[INPUT_WIDTH*INPUT_WIDTH][1];
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
	public static int rgbToGrayscale(int pixel) {
		int R = Color.red(pixel);
		int G = Color.green(pixel);
		int B = Color.blue(pixel);
		double grayscaleVal = (R + G + B) / 3.0;
		
		return (int)grayscaleVal;
	}
	
	/**
	 * Applies the   filtering algorithm to binarize the input image
	 * @param image Image to be filtered
	 */
	public static void otsuFilter(Bitmap image, int thresholdConstant) {
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
				threshold = i + thresholdConstant;
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
        
        //setAlpha(imageFile);
        
       // saveBitmapToFile("rotate.bmp", finalThresholdImage);
        
        
        
    	height = imageFile.getHeight();
		width = imageFile.getWidth();
		
       ///NEED TO THRESHOLD IMAGES
		finalThresholdImage = imageFile.copy(imageFile.getConfig(), true );
		
		//DETECT BOX
		int noFilter = 0;
		
		otsuFilter(finalThresholdImage, noFilter );
		saveBitmapToFile("otsu.bmp", finalThresholdImage);

        //LINE DETECTION
        detectLine();
    }
    
    /**
     * Sets the alpha values of all pixels to full opacity in case of rotation
     * @param img the Bitmap we want to set alpha values of 
     */
    public void setAlpha(Bitmap img){
    	int opaque = 255;
    	int pixel ;
    	
    	
    	Canvas alph = new Canvas(img);
    	
    	for (int i = 0; i < img.getWidth(); i++){
    		for (int j = 0 ; j< img.getHeight(); j++){
    			pixel = img.getPixel(i,j);
    			//alph.drawARGB(opaque, null , Color.green(pixel), Color.blue(pixel));
    		}
    	}
    	
    	
    	
    }
    
    
    public boolean isPeriod(Bitmap img){
    	
    	double numBlackPixels = 0.0;
    	
    	for (int i = 0; i < img.getWidth(); i++){
    		for (int j = 0; j< img.getHeight(); j++){
    			if (img.getPixel(i,j) == Color.BLACK){
    				numBlackPixels ++;
    			}
    		}
    	}
    	
    	double imgSize = img.getWidth()*img.getHeight();
    	
    	double percentBlack = numBlackPixels/imgSize;
    	
    	//Log.d("period", "Percent Black: "+ percentBlack);
    	
    	if( percentBlack <= 0.072 && percentBlack >= 0.056){
    		Log.d("period", "IS PERIOD");
    		return true;
    	}
    	
    	return false;
    }
    
    /**
	 * scan rows until a black pixel is found
	 * don't need to worry about left and right margins as storeCharacters() takes care of it
	 * keep scanning until we find a row with no black pixels create a new image containing a single line
	 */
    
    public int detectLine(){

    	int startLine = 0;
    	int endLine = 0;
    	boolean blackDetected = false;
    	int lineNum = 0;
    	boolean isLine = false;
    	
    	boolean firstLine = true;
    	
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
    			endLine = y-1;
    			lineHeight = endLine - startLine;
    			if (lineHeight > 5){
    				lineNum++;
    				createLineBitmap(startLine, lineNum, firstLine);
    				
    				if(firstLine){
    					firstLine = false;
    				}
    				
    			}
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
    public void createLineBitmap(int startY, int lineNum, boolean firstChar){
    	//need to create threshold bitmap and regular bitmap
        thresholdBitmap = Bitmap.createBitmap(finalThresholdImage, 0, startY, width, lineHeight);
        line = Bitmap.createBitmap(imageFile, 0 , startY, width, lineHeight);

        saveBitmapToFile("thresholdline" + lineNum + ".bmp", thresholdBitmap);
        saveBitmapToFile("line" + lineNum + ".bmp", line);
        
       if(firstChar){
    		findCharWidth(lineNum);
    		storeCharacter();
		} else {     
        	storeCharacter();
        }
    }
    
  /**
   * Goes through the line Bitmap that was created before the function was called
   * Creates a histogram of the character widths and their frequencies and selects the most common character width (within 4 pixels) to be
   * the reference character width  
   */
    
  public void findCharWidth(int lineNum){  
	  int isCharacter = 0;
      int wasBlackPixel = 0;
      int thresholdVal = 0;
      
      //second pass of the isolated line through otsu filter with increased threshold
      Bitmap otsuLine = line.copy(line.getConfig(), true );
      otsuFilter(otsuLine, thresholdVal);
      saveBitmapToFile("ostuLine" + lineNum + ".bmp", otsuLine);
      
      List<Integer> charFreq = new ArrayList<Integer>();
      List<Integer> charWidth = new ArrayList<Integer>();

      //go through with columns starting at left most column, then if a black pixel is detected, begin storing columns
      //until we encounter a column with no more black pixels.
      for (int x = 0; x < width; x++){
	        for (int y = 0 ; y < lineHeight; y++ ){ 				
	        	if (y == 0){
	        		wasBlackPixel = 0;
	        	}
	        	
	        	//int c = thresholdBitmap.getPixel(x, y);
	        	int c = otsuLine.getPixel(x, y);

	        	if (c == Color.BLACK){
	        		wasBlackPixel = 1;
	        		if(y >= lastCharacterRow){ //this finds the last row containing the letter
	        			lastCharacterRow = y;
	        		}
	        		if(y < beginningCharacterRow){ //this is to mark the top row of the letter
	        			beginningCharacterRow = y;
	        		}
	        		
	        		//if isCharacter is 0 we know that this is the first black pixel of the character
	        		if (isCharacter == 0 ){
	        			//if this is the first black pixel, set the flag to store rest of character
	        			isCharacter = 1;
	        			beginningCharacterColumn  = x;
	        			beginningCharacterRow = y;
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
		        				finalCharacterRows = lastCharacterRow - beginningCharacterRow + 1;
		        				
		        				//KEEP TRACK OF CHARACTER WIDTHS AND FREQUENCY
		        				Integer temp = 0;
		        				
		        				if ( charWidth.contains(finalCharacterColumns) ){
		        					
		        					//.out.println("character not in vector");
		        					
		        					int i = charWidth.indexOf(finalCharacterColumns);
		        					temp = charFreq.get(i);
		        					
		        					charFreq.set(i, ++temp);
		        					
		        				}else{
		        					
		        					charWidth.add(finalCharacterColumns);
		        					charFreq.add(1);
		        					temp = 1;
		        				}
		        				
		        				Log.d("CHARACTER", "WIDTH: " + finalCharacterColumns);
		        				Log.d("CHARACTER","FREQ: " + temp);
		        				
		        				lastCharacterRow = 0;
		        			}
	        		}
	        	}
	        	
	        }
      }
      
      //go through list of character widths and frequencies and find most common frequency
      int totalFreq = 0;
      int largestFreq = 0;
      int largestWidth = 0;
      
      ///NEED TO CHECK FOR RIDICULOUSLY LARGE CHARACTER WIDTHS
      
      //remove extremely large and small values
      //compute average and if values lie outside standard deviation, remove them
      double mean; 
      double sum = 0.0;
      double sumSquares = 0.0;
      double standardDev;
      
      for (int i = 0; i< charWidth.size(); i++){
    	  sum += charWidth.get(i)*charFreq.get(i);
    	  sumSquares += Math.pow((double)charWidth.get(i)*charFreq.get(i), (double)2);
      }
      
      mean = sum/charWidth.size();
      standardDev = sumSquares/((double)charWidth.size()) - mean;
      
      for (int i = 0; i < charWidth.size(); i++){
    	  if (charWidth.get(i)>(mean + standardDev) || charWidth.get(i) < (mean - standardDev)){
    		  charWidth.remove(i);
    		  charFreq.remove(i);
    	  }
      }
      
      for (int j = 0 ; j < charWidth.size()-3;  j++ ){
    	  int k = j;
    	  int beginningWidthNum, totalWidthNum;
    	  
    	  if (j > charWidth.size()-4){
    		  while (j < charWidth.size()){
    			  if(charWidth.get(j)<(2*largestWidth)){
    				  totalFreq += charFreq.get(j);  
    			  }  
    			  j++;
    		  }
    		  if (totalFreq > largestFreq ){
        		  largestFreq = totalFreq;
        		  beginningWidthNum = k;
        		  totalWidthNum = k;
        		  
        		  while( k < charWidth.size()){
        			  if(charWidth.get(j)<(2*largestWidth)){
	        			  largestWidth += charWidth.get(k);
	        			  totalWidthNum++;
        			  }
        			  k++;
        		  }
        		  largestWidth = largestWidth/(totalWidthNum - beginningWidthNum);
        	  }
    	  }
    	  else{
    	  
	    	  totalFreq = charFreq.get(j) + charFreq.get(++j) + charFreq.get(++j) + charFreq.get(++j);
	    	  
	    	  if (totalFreq > largestFreq ){
	    		  largestFreq = totalFreq;
	    		  largestWidth = (charWidth.get(k) + charWidth.get(++k) + charWidth.get(++k) + charWidth.get(++k))/4;
	    	  }
    	  }
      }
      
      characterWidth = largestWidth;

      Log.d("CHARACTER", "CHARACTERWIDTH: " + characterWidth);
      
  }
    
    /**
   	 * Parses the threshold image file and stores individual characters as a Bitmap object 
   	 * Calls createCharacterBitmap() or createSpaceBitmap(characterName) if a character or space is detected 
   	 */
    public void storeCharacter(){
    	
        int isCharacter = 0;
        int wasBlackPixel = 0;
        String characterName;
        int largestCharWidth = 0;
        
        int numWhiteColumns = 0;
        Boolean firstCharacter = false;
        double buffer = ((double)characterWidth)/4.0;
        
        int tempCharColumns;
        int tempCharRows;
        
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
	        		
	        		
	        		if(y >= lastCharacterRow){ //this finds the last row containing the letter
	        			lastCharacterRow = y;
	        		}
	        		if(y < beginningCharacterRow){ //this is to mark the top row of the letter
	        			beginningCharacterRow = y;
	        		}
	        		
	        		//if isCharacter is 0 we know that this is the first black pixel of the character
	        		if (isCharacter == 0 ){
	        			//if this is the first black pixel, set the flag to store rest of character
	        			isCharacter = 1;
	        			beginningCharacterColumn  = x;
	        			beginningCharacterRow = y;
	
	        			//if the number of white columns is equal or greater than the size of a character (we will have already seen a character)
	        			//we know that it is a space
	        			if (firstCharacter == true && numWhiteColumns >= largestCharWidth){
	        				
	        				characterName = String.format("%04d", characterNumber)  + ".bmp";
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
	        			
	        			tempCharColumns = x - beginningCharacterColumn;
	        			
	        			tempCharRows = lastCharacterRow - beginningCharacterRow + 1;
	        			
	        			
	        			//if (tempCharColumns > largestCharWidth*2/3){
		        		if (tempCharColumns > characterWidth*3.0/4.0){

		        			isCharacter = 0;
		        			
		        			finalCharacterColumns = x - beginningCharacterColumn;
		        			
		        			//Log.d("CHARACTER", "characterwidth + buffer = " + (characterWidth + buffer));
		        			//Log.d("CHARACTER", "character columns = " + (finalCharacterColumns));
		        			
		        			//if (finalCharacterColumns > (2*characterWidth - buffer)){
		        			if (((double)finalCharacterColumns) > (characterWidth + buffer)){
		        				
		        				
		        				//System.out.println("finalCharacterColumns > characterWidth");
		        				finalCharacterRows = lastCharacterRow - beginningCharacterRow + 1;

		        				//create large bmp and if rows are all black - split in half, otherwise split where white
		        				characterName = "largeChar" + String.valueOf(characterNumber) + ".bmp";
		        				createCharacterBitmap(characterName);
		        				
		        				//pop bmp from vector
		        				processedCharacters.remove(processedCharacters.size()-1);
		        				
		        				boolean hasWhiteColumn = true;
		        				int whiteColNum = 0;
		        				
		        				for (int i = 0; i < character.getWidth(); i++){
		        					for (int j = 0; j < character.getHeight(); j++){
		        						if (character.getPixel(i, j) == Color.BLACK ){
		        							hasWhiteColumn = false;
		        						}
		        					}
		        					if (hasWhiteColumn){
		        						whiteColNum = i;
		        					}
		        				}
		        				
		        				//split into two characters
		        				
		        				//if large bmp has a white column, we split at the white column otherwise, split in half
		        				if(hasWhiteColumn){
		        					characterName =  String.format("%04d", characterNumber) + ".bmp";
			        				Log.d("CHARACTER", characterNumber + ".bmp");
			        				Log.d("CHARACTER", "full width: " + finalCharacterColumns);
	
			        				finalCharacterColumns = whiteColNum;
			        					
			        				createCharacterBitmap(characterName);
			        				characterNumber++;
			        				
			        				//second character
			        				characterName =  String.format("%04d", characterNumber) + ".bmp";
			        				Log.d("CHARACTER",characterNumber + ".bmp");
	
			        				beginningCharacterColumn += whiteColNum;
			        					
			        				createCharacterBitmap(characterName);
			        				characterNumber++;
			        				
			        				lastCharacterRow = 0;
			        				firstCharacter = true;
		        				}
		        				else{
		        					int numChars = 0;
		        					
		        					//check if two, three or 4 characters stuck together
		        					if(finalCharacterColumns > (3*characterWidth + 2*buffer)){   //4 characters together
		        						numChars = 4;
		        						
		        					}else if(finalCharacterColumns > (2*characterWidth + 2*buffer)){   //3 characters together
		        						numChars = 3;
		        					}else{  				//2 characters together
		        					
		        						numChars = 2;
		        					}
		        					
		        					
	        						finalCharacterColumns = finalCharacterColumns/numChars;
	        						
	        						for (int chars = 0; chars < numChars; chars++){
	        							characterName =  String.format("%04d", characterNumber) + ".bmp";
	        							Log.d("CHARACTER",characterNumber + ".bmp");
	        							Log.d("CHARACTER","full width: " + finalCharacterColumns);
		
				        				createCharacterBitmap(characterName);
				        				characterNumber++;
				        				
				        				beginningCharacterColumn += finalCharacterColumns;
	        						}
	        						
			        				lastCharacterRow = 0;
			        				firstCharacter = true;
			        				
		        				}
		        			}

		        			//only recognizes characters if they are larger than one pixel long
		        			else if (finalCharacterColumns > 1){
		        				finalCharacterRows = lastCharacterRow - beginningCharacterRow + 1;
		        				
		        				if (finalCharacterColumns > largestCharWidth){
		        					largestCharWidth = finalCharacterColumns;
		        				}
		        			
		        				characterName =  String.format("%04d", characterNumber) + ".bmp";
		        			
		        				createCharacterBitmap(characterName);
		        				characterNumber++;
		        				lastCharacterRow = 0;
		        				firstCharacter = true;
		        			}
		        		}
		        		
	        		}
	        		else if (wasBlackPixel == 0 && isCharacter == 0 && firstCharacter == true){
	        			numWhiteColumns++;
	        		}
	        	}
	        	
	        	
	        }
        }
        
        characterName = String.format("%04d", characterNumber)  + ".bmp";
		createSpaceBitmap(characterName);
		characterNumber++;
        
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
    	
        character = Bitmap.createBitmap(thresholdBitmap, beginningCharacterColumn, beginningCharacterRow, finalCharacterColumns, finalCharacterRows );

        addWhiteSpace(whitespaceX, whitespaceY, characterDimensions);
        
        scaledImage = Bitmap.createScaledBitmap(charWithWhite, SCALEDDIMENSION, SCALEDDIMENSION, false);
        
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
		
    	charWithWhite = Bitmap.createBitmap(createWhiteBitmap(), 0, 0, finalCharacterRows, finalCharacterRows);
		Canvas whitespace = new Canvas(charWithWhite);
        whitespace.drawRGB(Color.WHITE,Color.WHITE,Color.WHITE);
        whitespace.drawBitmap(character, finalCharacterRows, finalCharacterRows, null);
        
        scaledImage = Bitmap.createScaledBitmap(charWithWhite, SCALEDDIMENSION, SCALEDDIMENSION, true);
        
        processedCharacters.add(scaledImage);
        saveBitmapToFile(characterName, scaledImage);
	}
    
	/**
	 * Creates a white Bitmap object from white.bmp in file system
	 * @return whiteImage - Bitmap object of only white pixels
	 */
	public Bitmap createWhiteBitmap(){
		Bitmap whiteImage = BitmapFactory.decodeFile("/sdcard/Pictures/Ctrl_F_It/white.bmp");
		
		return whiteImage;
	}
	
	/**
  	 * Creates a Bitmap that is a space character for reference to add to the Vector of characters
  	 */
	public void createReferenceSpace(){
		//DOESN'T REALLY MATTER THE SIZE, WILL BE RESIZED TO INPUT_WIDTHXINPUT_WIDTH ANYWAYS - JUST NEEDS TO BE SQUARE
		
    	charWithWhite = Bitmap.createBitmap(createWhiteBitmap(), 0, 0, finalCharacterRows, finalCharacterRows);
		Canvas whitespace = new Canvas(charWithWhite);
        whitespace.drawRGB(Color.WHITE,Color.WHITE,Color.WHITE);
        whitespace.drawBitmap(character, finalCharacterRows, finalCharacterRows, null);
        
        referenceSpace = Bitmap.createScaledBitmap(charWithWhite, SCALEDDIMENSION, SCALEDDIMENSION, false);
	}
	
	/**
  	 * Adds whitespace to image to create a square image
  	 * @param padding_x the amount of space to add to the character on the left and right sides of the image
  	 * @param padding_y the amount of space to add to the character on the top and bottom of the image
  	 * @param imageDimensions the length and width of the image - based on the height or width of character (whichever is larger)
  	 */
    public void addWhiteSpace( int padding_x, int padding_y, int imageDimensions){    
    	DisplayMetrics display = new DisplayMetrics();
    	
    	charWithWhite = Bitmap.createBitmap(createWhiteBitmap(), 0, 0, imageDimensions, imageDimensions);
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
    	
    	//File dir = Environment.getExternalStorageDirectory();
    	File dir = new File("/sdcard/Pictures/Ctrl_F_It/Filter/");
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


