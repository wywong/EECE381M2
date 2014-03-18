package com.example.ctrl_f_it;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.ejml.simple.SimpleMatrix;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;


public class ProcessingActivity extends Activity {

	public static final int INPUT_WIDTH = 20;
	public static final int INPUT = 400;
	public static final int OUTPUT = 26;
	public static final int HIDDEN_UNITS = 72;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_processing);
		char matchedChar = predict("sdcard/Ctrl_F_It/A0.bmp");
		Log.d("prediction", Character.toString(matchedChar));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.processing, menu);
		return true;
	}

	/**
	 * Predicts the most likely character from the input image
	 * @param inFile Location of the image file relative to the root directory of the android device
	 * @return The character that has the highest probability of matching the input
	 */
	public char predict(String inFile) {
		double[][] theta1 = parseCSV("sdcard/Ctrl_F_It/theta1.csv", HIDDEN_UNITS, INPUT + 1);
		double[][] theta2 = parseCSV("sdcard/Ctrl_F_It/theta2.csv", OUTPUT, HIDDEN_UNITS + 1);
		double[][] input = inputUnroll(bmpToArray(inFile), INPUT_WIDTH, INPUT_WIDTH);

		return forwardPropagation(theta1, theta2, input);
	}

	/**
	 * Applies feed forward propagation within the neural network
	 * @param theta1 2D array containing the theta values for the hidden layer (size HIDDEN_UNITS x INPUT + 1)
	 * @param theta2 2D array containing the theta values for the hidden layer (size OUTPUT x HIDDEN_UNITS + 1)
	 * @param input Unrolled column vector of values each representing one pixel of the image (size INPUT x 1)
	 * @return The character that has the highest probability of matching the input
	 */
	public char forwardPropagation(double[][] theta1, double[][] theta2, double[][] input) {
		SimpleMatrix theta1Array = new SimpleMatrix(theta1);
		SimpleMatrix theta2Array = new SimpleMatrix(theta2);
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
	 * @param inFile Location of the BMP file relative to the root directory of the android device
	 * @return Array containing the pixel information of the BMP image (size INPUT_WIDTH x INPUT_WIDTH)
	 */
	public double[][] bmpToArray(String inFile) {
		double[][] imageArray = new double[INPUT_WIDTH][INPUT_WIDTH];
		Bitmap image = BitmapFactory.decodeFile(inFile);

		for(int yPixel = 0; yPixel < INPUT_WIDTH; yPixel++) {
			for(int xPixel = 0; xPixel < INPUT_WIDTH; xPixel++) {
				//xPixel => column of the array, yPixel => row of the array
				imageArray[yPixel][xPixel] = rgbToGrayscale(image.getPixel(xPixel, yPixel));
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
}
