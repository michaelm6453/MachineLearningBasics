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

	public static float[] getActual(List<String[]> allData) {
		float[] actualValues = new float[allData.size()];
		for (int i = 0; i < allData.size(); i++) {
			actualValues[i] = Float.parseFloat(allData.get(i)[0]);
		}
		return actualValues;
	}

	public static float[] getPredicted(List<String[]> allData) {
		float[] predictedValues = new float[allData.size()];
		for (int i = 0; i < allData.size(); i++) {
			predictedValues[i] = Float.parseFloat(allData.get(i)[1]);
		}
		return predictedValues;
	}

	public static float getBCE(float[] predicted, float[] actual) {
		if (predicted.length != actual.length) {
			throw new IllegalArgumentException("Arrays must have the same length.");
		}
		float sum = 0;
		int n = predicted.length;
		for (int i = 0; i < n; i++) {
			sum += actual[i] * Math.log(predicted[i]) + (1 - actual[i]) * Math.log(1 - predicted[i]);
		}
		return (-1) * (sum / n);
	}

	public static int[][] getConfusionMatrix(float[] predicted, float[] actual, float threshold) {
		int TP = 0, FP = 0, FN = 0, TN = 0;
		for (int i = 0; i < actual.length; i++) {
			boolean predictedClass = predicted[i] >= threshold;
			boolean actualClass = actual[i] == 1;
			if (predictedClass && actualClass) TP++;
			else if (predictedClass && !actualClass) FP++;
			else if (!predictedClass && actualClass) FN++;
			else TN++;
		}
		return new int[][]{{TP, FP}, {FN, TN}};
	}

	public static float getAccuracy(int TP, int FP, int FN, int TN) {
		return (float) (TP + TN) / (TP + FP + FN + TN);
	}

	public static float getPrecision(int TP, int FP) {
		return TP / (float) (TP + FP);
	}

	public static float getRecall(int TP, int FN) {
		return TP / (float) (TP + FN);
	}

	public static float getF1Score(float precision, float recall) {
		return 2 * (precision * recall) / (precision + recall);
	}

	public static float getAurRoc(float[] predicted, float[] actual) {
		float[] thresholds = new float[101];
		float[] tpr = new float[101];
		float[] fpr = new float[101];
		int numPositives = 0, numNegatives = 0;

		for (int i = 0; i < actual.length; i++) {
			if (actual[i] == 1) {
				numPositives++;
			} else {
				numNegatives++;
			}
		}

		// 0.00 to 1.00
		for (int i = 0; i <= 100; i++) {
			float threshold = i / 100.0f;
			thresholds[i] = threshold;

			int TP = 0, FP = 0, FN = 0, TN = 0;
			for (int j = 0; j < actual.length; j++) {
				boolean predictedClass = predicted[j] >= threshold;
				boolean actualClass = actual[j] == 1;

				if (predictedClass && actualClass) TP++;
				else if (predictedClass && !actualClass) FP++;
				else if (!predictedClass && actualClass) FN++;
				else TN++;
			}

			tpr[i] = (float) TP / numPositives;
			fpr[i] = (float) FP / numNegatives;
		}

		float aucRocValue = 0;
		for (int i = 1; i <= 100; i++) {
			float deltaX = fpr[i] - fpr[i - 1];
			aucRocValue += (tpr[i - 1] + tpr[i]) * deltaX / 2.0f;
		}

		return Math.abs(aucRocValue);
	}
	public static void main(String[] args) {
		String[] filePaths = {"model_1.csv", "model_2.csv", "model_3.csv"};
		float bestBCE = Float.MAX_VALUE;
		String bestBCEModel = "";
		float bestAccuracy = 0, bestPrecision = 0, bestRecall = 0, bestF1 = 0, bestAurRoc =0;
		String bestAccuracyModel = "", bestPrecisionModel = "", bestRecallModel = "", bestF1Model = "", bestAurRocModel = "";

		for (String filePath : filePaths) {
			List<String[]> allData = readCsvData(filePath);
			if (allData.isEmpty()) {
				System.out.println("No data found in " + filePath);
				continue;
			}

			float[] actual = getActual(allData);
			float[] predicted = getPredicted(allData);
			float bce = getBCE(predicted, actual);
			int[][] confusionMatrix = getConfusionMatrix(predicted, actual, 0.5f);
			int TP = confusionMatrix[0][0];
			int FP = confusionMatrix[0][1];
			int FN = confusionMatrix[1][0];
			int TN = confusionMatrix[1][1];

			float accuracy = getAccuracy(TP, FP, FN, TN);
			float precision = getPrecision(TP, FP);
			float recall = getRecall(TP, FN);
			float f1 = getF1Score(precision, recall);
			float aurRoc = getAurRoc(predicted, actual);

			System.out.println("Results for " + filePath + ":");
			System.out.println("  BCE = " + bce);
			System.out.println("  Confusion matrix");
			System.out.println("                y=1      y=0");
			System.out.println("        y^=1    " + TP + "    " + FP);
			System.out.println("        y^=0    " + FN + "    " + TN);
			System.out.println("  Accuracy = " + accuracy);
			System.out.println("  Precision = " + precision);
			System.out.println("  Recall = " + recall);
			System.out.println("  F1 score = " + f1);
			System.out.println("  AUR ROC = " + aurRoc);
			System.out.println();

			if (bce < bestBCE) {
				bestBCE = bce;
				bestBCEModel = filePath;
			}
			if (accuracy > bestAccuracy) {
				bestAccuracy = accuracy;
				bestAccuracyModel = filePath;
			}
			if (precision > bestPrecision) {
				bestPrecision = precision;
				bestPrecisionModel = filePath;
			}
			if (recall > bestRecall) {
				bestRecall = recall;
				bestRecallModel = filePath;
			}
			if (f1 > bestF1) {
				bestF1 = f1;
				bestF1Model = filePath;
			}
			if (aurRoc > bestAurRoc){
				bestAurRoc = aurRoc;
				bestAurRocModel = filePath;
			}

		}

		System.out.println("Best models:");
		System.out.println("  Best BCE Model: " + bestBCEModel);
		System.out.println("  Best Accuracy Model: " + bestAccuracyModel);
		System.out.println("  Best Precision Model: " + bestPrecisionModel);
		System.out.println("  Best Recall Model: " + bestRecallModel);
		System.out.println("  Best F1 Score Model: " + bestF1Model);
		System.out.println("Best AUR ROC Model: " + bestAurRocModel);
	}
}
