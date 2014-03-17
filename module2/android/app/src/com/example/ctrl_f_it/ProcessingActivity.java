package com.example.ctrl_f_it;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

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
		double[][] theta1 = parseTheta("sdcard/Ctrl_F_It/theta1.csv", HIDDEN_UNITS, INPUT + 1);
		double[][] theta2 = parseTheta("sdcard/Ctrl_F_It/theta2.csv", OUTPUT, HIDDEN_UNITS + 1);

	}

	/* forwardPropagation
	 * Applies feed forward propagation within the neural network
	 * Parameters:
	 * 		double[][] theta1: Each row contains the theta values for one hidden unit and the bias (INPUT + 1 columns)
	 * 		double[][] theta2: Each row contains the theta values for each output character and the bias
	 * 		double[] input: Unrolled vector of INPUT elements, each representing one pixel of the image
	 * Return:
	 * 		int: The (i + 1)th character of the alphabet that has the highest calculated propability
	 */
	public int forwardPropagation(double[][] theta1, double[][] theta2, double[] input) {
		RealMatrix theta1Array = new Array2DRowRealMatrix(theta1);
		RealMatrix theta2Array = new Array2DRowRealMatrix(theta2);
		RealMatrix inputArray = new Array2DRowRealMatrix(INPUT + 1, 1);
		RealMatrix h1Array;
		//Create a column vector with # of hidden units + 1 for the bias element
		RealMatrix h1ArrayWithBias = new Array2DRowRealMatrix(HIDDEN_UNITS + 1, 1);
		RealMatrix outputArray;
		int i;
		double sigmoidVal;
		int bestMatch = 0;

		//Initialize input array to include the bias value
		inputArray.setEntry(0, 0, 1);
		for(i = 1; i < INPUT + 1; i++) {
			inputArray.setEntry(i, 0, input[i - 1]);
		}

		//sigmoid(theta1 * input)
		h1Array = theta1Array.multiply(inputArray);
		h1ArrayWithBias.setEntry(0, 0, 1);
		for(i = 1; i < HIDDEN_UNITS + 1; i++) {
			sigmoidVal = 1 / (1 + Math.exp(-h1Array.getEntry(i - 1, 0)));
			h1ArrayWithBias.setEntry(i, 0, sigmoidVal);
		}

		//sigmoid(theta2 * h1)
		outputArray = theta2Array.multiply(h1ArrayWithBias);
		for(i = 0; i < OUTPUT; i++) {
			sigmoidVal = 1 / (1 + Math.exp(-outputArray.getEntry(i, 0)));
			outputArray.setEntry(1, 0, sigmoidVal);
		}

		for(i = 0; i < OUTPUT; i++) {
			if(outputArray.getEntry(i, 0) > bestMatch) {
				bestMatch = i;
			}
		}

		return bestMatch;
	}

	public double[][] parseTheta(String inFile, int rows, int columns) {
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
