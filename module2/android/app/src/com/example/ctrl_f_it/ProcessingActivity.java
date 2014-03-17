package com.example.ctrl_f_it;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.ejml.simple.SimpleMatrix;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;


public class ProcessingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_processing);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.processing, menu);
		return true;
	}

	public static final int OUTPUT = 26;
	public static final int HIDDEN_UNITS = 85;
	// 32x32 size character image
	public static final int INPUT = 1024;

	public void charRecognition() {
		double[][] theta1 = parseCSV("sdcard/Ctrl_F_It/theta1.csv", HIDDEN_UNITS, INPUT + 1);
		double[][] theta2 = parseCSV("sdcard/Ctrl_F_It/theta2.csv", OUTPUT, HIDDEN_UNITS + 1);

	}
	
	/* forwardPropagation
	 * Applies feed forward propagation within the neural network
	 * Parameters:
	 * 		double[][] theta1: Each row contains the theta values for one hidden unit and the bias (INPUT + 1 columns)
	 * 		double[][] theta2: Each row contains the theta values for each output character and the bias
	 * 		double[] input: Unrolled vector of INPUT elements, each representing one pixel of the image
	 * Return:
	 * 		int: The ith character of the alphabet that has the highest calculated propability
	 */
	public int forwardPropagation(double[][] theta1, double[][] theta2, double[] input) {
		SimpleMatrix theta1Array = new SimpleMatrix(theta1);
		SimpleMatrix theta2Array = new SimpleMatrix(theta2);
		SimpleMatrix inputArray = new SimpleMatrix(INPUT, 1);
		SimpleMatrix inputArrayWithBias = new SimpleMatrix(INPUT + 1, 1);
		SimpleMatrix h1Array;
		SimpleMatrix h1ArrayWithBias = new SimpleMatrix(HIDDEN_UNITS + 1, 1);
		SimpleMatrix outputArray;
		int i;
		double sigmoidVal;
		int bestMatch = 0;

		//Initialize input array to include the bias value
		inputArrayWithBias.set(0, 0, 1);
		inputArrayWithBias.insertIntoThis(1, 0, inputArray);

		//sigmoid(theta1 * input)
		h1Array = theta1Array.mult(inputArrayWithBias);
		h1ArrayWithBias.set(0, 0, 1);
		for(i = 0; i < HIDDEN_UNITS; i++) {
			sigmoidVal = 1 / (1 + Math.exp(-h1Array.get(i, 0)));
			h1Array.set(i, 0, sigmoidVal);
		}
		h1ArrayWithBias.insertIntoThis(1, 0, h1Array);

		//sigmoid(theta2 * h1)
		outputArray = theta2Array.mult(h1ArrayWithBias);
		for(i = 0; i < OUTPUT; i++) {
			sigmoidVal = 1 / (1 + Math.exp(-outputArray.get(i, 0)));
			outputArray.set(1, 0, sigmoidVal);
		}

		for(i = 0; i < OUTPUT; i++) {
			if(outputArray.get(i, 0) > bestMatch) {
				bestMatch = i;
			}
		}

		return bestMatch + 1;
	}
	
	/* parseCSV
	 * Parses a csv file and stores the contents into a 2d double array
	 * Parameters:
	 * 		String inFile: Location of the csv file relative to the root directory of the android device
	 * 		int rows: Number of rows to be parsed
	 * 		int columns: Number of columns to be parsed
	 * Return:
	 * 		double[][]: Array containing the contents of the csv file
	 */
	public double[][] parseCSV(String inFile, int rows, int columns) {
		double[][] theta = new double[rows][columns];
		BufferedReader br = null;
		String row;
		String[] separatedRow;
		String cvsDelimiter = ",";
		int i, j;

		try {
			br = new BufferedReader(new FileReader(inFile));
			for(i = 0; i < rows; i++ ) {
				row = br.readLine();
				separatedRow = row.split(cvsDelimiter);
				for(j = 0; j < columns; j++) {
					theta[i][j] = Double.parseDouble(separatedRow[j]);
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
		return theta;
	}
}
