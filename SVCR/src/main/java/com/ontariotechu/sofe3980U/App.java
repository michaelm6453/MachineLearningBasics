package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * Evaluate Single Variable Continuous Regression
 */
public class App {

	// function to read CSV data
	public static List<String[]> readCsvData(String filename) {
		List<String[]> allData = new ArrayList<>();
		try (FileReader filereader = new FileReader(filename);
			 CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build()) {
			allData = csvReader.readAll();
		} catch (IOException e) {
			System.out.println("Error reading the CSV file: " + e.getMessage());
		}
		return allData;
	}

	// function to get actual values from csv
	public static float[] getActual(List<String[]> allData) {
		float[] actualValues = new float[allData.size()];
		for (int i = 0; i < allData.size(); i++) {
			actualValues[i] = Float.parseFloat(allData.get(i)[0]);
		}
		return actualValues;
	}

	// function to get predicted values from csv
	public static float[] getPredicted(List<String[]> allData) {
		float[] predictedValues = new float[allData.size()];
		for (int i = 0; i < allData.size(); i++) {
			predictedValues[i] = Float.parseFloat(allData.get(i)[1]);
		}
		return predictedValues;
	}

	// MSE function
	public static float getMSE(float[] predicted, float[] actual) {
		if (predicted.length != actual.length) {
			throw new IllegalArgumentException("Arrays must have the same length.");
		}
		float sum = 0;
		int n = predicted.length;
		for (int i = 0; i < n; i++) {
			float error = predicted[i] - actual[i];
			sum += error * error;
		}
		return sum / n;
	}

	// MAE function
	public static float getMAE(float[] predicted, float[] actual) {
		if (predicted.length != actual.length) {
			throw new IllegalArgumentException("Arrays must have the same length.");
		}
		float sum = 0;
		int n = predicted.length;
		for (int i = 0; i < n; i++) {
			sum += Math.abs(predicted[i] - actual[i]);
		}
		return sum / n;
	}

	// MARE function
	public static float getMARE(float[] predicted, float[] actual) {
		if (predicted.length != actual.length) {
			throw new IllegalArgumentException("Arrays must have the same length.");
		}
		float sum = 0;
		int n = predicted.length;
		for (int i = 0; i < n; i++) {
			if (actual[i] != 0) { // Prevent division by zero
				sum += Math.abs((predicted[i] - actual[i]) / actual[i]);
			}
		}
		return sum / n;
	}

	// Main function to compute and compare MSE, MAE, and MARE
	public static void main(String[] args) {
		String[] filePaths = {"model_1.csv", "model_2.csv", "model_3.csv"};

		float bestMSE = Float.MAX_VALUE, bestMAE = Float.MAX_VALUE, bestMARE = Float.MAX_VALUE;
		String bestMSEModel = "", bestMAEModel = "", bestMAREModel = "";

		for (String filePath : filePaths) {
			List<String[]> allData = readCsvData(filePath);

			if (allData.isEmpty()) {
				System.out.println("No data found in " + filePath);
				continue;
			}

			float[] actual = getActual(allData);
			float[] predicted = getPredicted(allData);

			float mse = getMSE(predicted, actual);
			float mae = getMAE(predicted, actual);
			float mare = getMARE(predicted, actual);

			System.out.println("Results for " + filePath + ":");
			System.out.println("  MSE  = " + mse);
			System.out.println("  MAE  = " + mae);
			System.out.println("  MARE = " + mare);
			System.out.println();

			// determine best model based off smallest vals
			if (mse < bestMSE) {
				bestMSE = mse;
				bestMSEModel = filePath;
			}
			if (mae < bestMAE) {
				bestMAE = mae;
				bestMAEModel = filePath;
			}
			if (mare < bestMARE) {
				bestMARE = mare;
				bestMAREModel = filePath;
			}
		}

		// Output best model according to each metric
		System.out.println("Best models based on error metrics:");
		System.out.println("  Best MSE  Model: " + bestMSEModel + " (MSE = " + bestMSE + ")");
		System.out.println("  Best MAE  Model: " + bestMAEModel + " (MAE = " + bestMAE + ")");
		System.out.println("  Best MARE Model: " + bestMAREModel + " (MARE = " + bestMARE + ")");
	}
}
