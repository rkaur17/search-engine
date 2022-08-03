import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;

/**
 * ResultBuilder class to execute queries and store results
 *
 * @author Ramneet Kaur CS 272 Software Development (University of San
 *         Francisco)
 * @version Fall 2021
 */
public class ResultBuilder implements ResultBuilderInterface {
	/**
	 * map of query -> list of results
	 */
	private final TreeMap<String, ArrayList<InvertedIndex.Result>> resultMap;

	/**
	 * index to search
	 */
	private final InvertedIndex index;

	/**
	 * constructor
	 *
	 * @param index to perform queries on
	 */
	public ResultBuilder(InvertedIndex index) {
		this.resultMap = new TreeMap<>();
		this.index = index;
	}

	/**
	 * Writes a JSON object to the output path provided.
	 *
	 * @param path output file path to write pretty JSON version of InvertedIndex
	 * @throws IOException when IOException occurs
	 */
	public void writeSearchResultJsonObject(Path path) throws IOException {
		SimpleJsonWriter.asResultObject(resultMap, path);
	}

	/**
	 * cleans each line using TextFileStemmer calls the search method
	 *
	 * @param line    line to be parsed for queries
	 * @param exact   indicates whether to run exact or partial search
	 * @param stemmer the stemmer to use to stem query words
	 */
	public void executeQuery(String line, boolean exact, Stemmer stemmer) {
		TreeSet<String> queries = (TreeSet<String>) TextFileStemmer.uniqueStems(line, stemmer);
		String cleanedQuery = String.join(" ", queries);

		if (!cleanedQuery.isBlank() && !resultMap.containsKey(cleanedQuery)) {
			resultMap.put(cleanedQuery, index.search(queries, exact));
		}
	}
}