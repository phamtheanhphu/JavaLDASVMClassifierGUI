package main.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BinaryOperator;

import adpater.file.TextFileAdapter;

public class KLDivergenceTest {

	static int numberOfTestingDoc = 90;
	static boolean enableDetailLogging = false;

	public static void main(String[] args) {

		// TODO Auto-generated method stub

		TextFileAdapter textFileAdapter = new TextFileAdapter();

		List<String> testingTopicList = new ArrayList<>();
		testingTopicList.add("artificial_intelligence");
		testingTopicList.add("bioinformatics");
		testingTopicList.add("computer_hardware");
		//testingTopicList.add("data_mining");
		testingTopicList.add("information_retrieval");

		for (String testingTopic : testingTopicList) {

			String expectedTopicName = testingTopic;

			String currentUserDirPath = System.getProperty("user.dir");

			String docTermWeightDataRootPath = currentUserDirPath + "/data/test/" + "kl_divergence/test_docs/"
					+ expectedTopicName + "_KL.txt";

			List<Map<String, Double>> testDocTermWeightsMaps = loadTestDocument(docTermWeightDataRootPath);

			// String docTermWeightDataFilePath = currentUserDirPath +
			// "/data/test/kl_divergence/doc.txt";

			double correctCount = 0;
			int docCount = 1;

			Map<String, Integer> predictedResultMap = new HashMap<>();

			for (Map<String, Double> testDocTermWeightsMap : testDocTermWeightsMaps) {
				
				if(docCount>=numberOfTestingDoc) {
					break;
				}

				String topicTermWeightDataFileRootPath = currentUserDirPath + "/data/"
						+ "test/kl_divergence/topics/20_terms";

				//File[] topicFiles = new File(topicTermWeightDataFileRootPath).listFiles();

				String maxKLDivergenceTopic = "";
				double maxKLDivergenceValue = 0;

				for (String rootTopicKeywords : testingTopicList) {
					File topicFile = new File(topicTermWeightDataFileRootPath + "/" + rootTopicKeywords + ".txt");
					
					if (topicFile.isFile()) {

						// System.out.println("Topic -> [" + topicFile.getName()
						// +
						// "]");

						String topicTermWeightDataFilePath = topicTermWeightDataFileRootPath + "/"
								+ topicFile.getName();

						List<String> topicTermWeights = textFileAdapter
								.parseSingleFileToListString(topicTermWeightDataFilePath);

						Map<String, Double> topicTermsWeightMap = new HashMap<String, Double>();
						for (String pair : topicTermWeights) {
							String[] splits = pair.split("=");
							topicTermsWeightMap.put(splits[0].trim(), Double.parseDouble(splits[1]));
						}

						// union(topicTermsWeightMap, docTermsWeightMap);
						String topicName = topicFile.getName().replaceAll(".txt", "");
						double klDivergence = calcKLDivergence(topicName, topicTermsWeightMap, testDocTermWeightsMap);
						if (klDivergence > maxKLDivergenceValue) {
							maxKLDivergenceValue = klDivergence;
							maxKLDivergenceTopic = topicName;
						}

					}

				}

				if (expectedTopicName.equalsIgnoreCase(maxKLDivergenceTopic)) {
					correctCount++;
				}

				if(maxKLDivergenceTopic!="") {
					int count = predictedResultMap.containsKey(maxKLDivergenceTopic)
							? predictedResultMap.get(maxKLDivergenceTopic) : 1;
					predictedResultMap.put(maxKLDivergenceTopic, count + 1);
				}
				
				if (enableDetailLogging) {
					System.out.println(
							"Doc [" + docCount + "] -> [" + maxKLDivergenceTopic + "][" + maxKLDivergenceValue + "]");
					System.out.println();
				}

				docCount++;

			}

			System.out.println(
					"Accuracy rate -> [" + testingTopic + "][" + (correctCount / (double) docCount) * 100 + "]");

			printMapInteger(predictedResultMap);

		}

	}

	// calcKLDivergence
	private static double calcKLDivergence(String topicName, Map<String, Double> topicMap, Map<String, Double> docMap) {

		Map<String, Double> unitedMap = union(topicMap, docMap);
		Iterator mapIterator = unitedMap.entrySet().iterator();
		// Iterator mapIterator = topicMap.entrySet().iterator();

		double klDivergenceAcc = 0;

		if (topicMap.size() > 0) {

			while (mapIterator.hasNext()) {

				Map.Entry<String, Double> checkedEntry = (Entry<java.lang.String, java.lang.Double>) mapIterator.next();

				double topicTermWeight = topicMap.get(checkedEntry.getKey()).doubleValue();
				double docTermWeight = docMap.get(checkedEntry.getKey()).doubleValue();

				double piTopicDoc = topicTermWeight / (topicTermWeight + docTermWeight);
				double piDocTopic = docTermWeight / (topicTermWeight + docTermWeight);

				// w(t)
				double generalTermWeight = (piTopicDoc * topicTermWeight) + (piDocTopic * docTermWeight);

				double klDivergenceTopicDoc = topicTermWeight * Math.log(topicTermWeight / generalTermWeight);
				double klDivergenceDocTopic = docTermWeight * Math.log(docTermWeight / generalTermWeight);

				double klDivergenceDistance = ((piTopicDoc * klDivergenceTopicDoc))
						+ ((piDocTopic * klDivergenceDocTopic));
				klDivergenceAcc += klDivergenceDistance;

				// System.out.println(checkedEntry.getKey() + " " +
				// klDivergenceAcc);

			}

		}

		if (enableDetailLogging) {
			System.out.println(topicName + " -> [" + klDivergenceAcc + "]");
		}

		return klDivergenceAcc;

	}

	// loadTestDocument
	private static List<Map<String, Double>> loadTestDocument(String dataFilePath) {

		TextFileAdapter textFileAdapter = new TextFileAdapter();
		List<Map<String, Double>> documents = new ArrayList<>();
		List<String> lines = (List<String>) textFileAdapter
				.parseSingleFileToListString((java.lang.String) dataFilePath);

		if (lines.size() > 0) {

			Map<String, Double> document = null;

			for (String line : lines) {

				if (line.equals("---")) {

					if (document != null && document.keySet().size() > 0) {
						documents.add(document);
					}
					document = new HashMap<>();
					continue;

				}

				String splits[] = line.split("=");
				document.put(splits[0].trim(), Double.parseDouble(splits[1]));

			}

			// System.out.println("Total testing document: -> [" +
			// documents.size() + "]");

			return documents;

		}

		return null;
	}

	public static <K, V> void merge(Map<K, V> map1, Map<K, V> map2) {
		Map<K, V> mergedMap = new HashMap<>();

		Set set = new HashSet<>();

		set.addAll(map1.keySet());
		set.addAll(map2.keySet());

		/*
		 * mergedMap.putAll(map1); mergedMap.putAll(map2);
		 */

		printSet(set);

	}

	public static <String, Double> Map<String, Double> union(Map<String, Double> map1, Map<String, Double> map2) {

		Map<String, Double> unitedMap = new HashMap<>();

		Iterator mapIterator = map1.entrySet().iterator();

		while (mapIterator.hasNext()) {
			Map.Entry<String, Double> entry = (Entry<String, Double>) mapIterator.next();
			if (map2.get(entry.getKey()) != null) {
				unitedMap.put(entry.getKey(), entry.getValue());
			}

		}

		return unitedMap;

	}

	private static void printMap(Map<String, Double> dataMap) {

		Iterator mapIterator = dataMap.entrySet().iterator();
		while (mapIterator.hasNext()) {
			Map.Entry<String, Double> entry = (Entry<String, Double>) mapIterator.next();
			System.out.println(entry.getKey() + " - " + entry.getValue());
		}
		System.out.println();

	}

	private static void printMapInteger(Map<String, Integer> dataMap) {

		Iterator mapIterator = dataMap.entrySet().iterator();
		while (mapIterator.hasNext()) {
			Map.Entry<String, Double> entry = (Entry<String, Double>) mapIterator.next();
			System.out.println(entry.getKey() + " - " + entry.getValue());
		}
		System.out.println();

	}

	private static void printSet(Set dataSet) {

		Iterator setIterator = dataSet.iterator();
		while (setIterator.hasNext()) {
			String term = setIterator.next().toString();
			System.out.println(term);
		}
		System.out.println();

	}

}
