package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class App {

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

	public static int[] getActual(List<String[]> allData) {
		int[] actualValues = new int[allData.size()];
		for (int i = 0; i < allData.size(); i++) {
			actualValues[i] = Integer.parseInt(allData.get(i)[0]);
		}
		return actualValues;
	}

	public static float[][] getPredicted(List<String[]> allData) {
		float[][] predictedValues = new float[allData.size()][5];
		for (int i = 0; i < allData.size(); i++) {
			for (int j = 0; j < 5; j++) {
				predictedValues[i][j] = Float.parseFloat(allData.get(i)[j + 1]);
			}
		}
		return predictedValues;
	}

	public static float getCE(float[][] predicted, int[] actual) {
		float sum = 0;
		int n = predicted.length;
		for (int i = 0; i < n; i++) {
			int actualClass = actual[i] - 1; // Convert to 0-based index (subtract 1 from true class)
			sum += Math.log(predicted[i][actualClass]); // Log of the predicted value for the actual class
		}
		return -1 * (sum / n);
	}

	public static int[][] getConfusionMatrix(int[] actual, float[][] predicted) {
		int[][] confusionMatrix = new int[5][5]; // 5 classes
		for (int i = 0; i < actual.length; i++) {
			int predictedClass = 0;
			float maxProb = predicted[i][0];
			for (int j = 1; j < 5; j++) {
				if (predicted[i][j] > maxProb) {
					maxProb = predicted[i][j];
					predictedClass = j;
				}
			}
			int actualClass = actual[i] - 1; // Convert to 0-based index (subtract 1 from true class)
			confusionMatrix[predictedClass][actualClass]++;
		}
		return confusionMatrix;
	}

	public static void main(String[] args) {
		String[] filePaths = {"model.csv"};
		float ce = 0;

		for (String filePath : filePaths) {
			List<String[]> allData = readCsvData(filePath);
			if (allData.isEmpty()) {
				System.out.println("No data found in " + filePath);
				continue;
			}

			int[] actual = getActual(allData);
			float[][] predicted = getPredicted(allData);

			ce = getCE(predicted, actual);

			int[][] confusionMatrix = getConfusionMatrix(actual, predicted);

			System.out.println("Results for " + filePath + ":");
			System.out.println("  CE = " + ce);

			System.out.println("  Confusion matrix");
			System.out.println("                y=1     y=2     y=3     y=4     y=5");
			for (int i = 0; i < 5; i++) {
				System.out.printf("        y^=%d", i + 1);
				for (int j = 0; j < 5; j++) {
					System.out.printf("    %5d", confusionMatrix[i][j]);
				}
				System.out.println(); 
			}
		}
	}
}
