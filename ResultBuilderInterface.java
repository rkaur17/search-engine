import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * ResultBuilder interface that declares common methods used by sublcasses
 *
 * @author Ramneet Kaur CS 272 Software Development (University of San
 *         Francisco)
 * @version Fall 2021
 */
public interface ResultBuilderInterface {

	/**
	 * Writes a JSON object to the output path provided.
	 *
	 * @param path output file path to write pretty JSON version of InvertedIndex
	 * @throws IOException when IOException occurs
	 */
	public void writeSearchResultJsonObject(Path path) throws IOException;

	/**
	 * cleans each line using TextFileStemmer calls the search method
	 *
	 * @param line    line to be parsed for queries
	 * @param exact   indicates whether to run exact or partial search
	 * @param stemmer the stemmer to use to stem query words
	 */
	public void executeQuery(String line, boolean exact, Stemmer stemmer);

	/**
	 * Reads in all lines of query file and executes the queries on each line
	 *
	 * @param path  path to be parsed for queries
	 * @param exact indicates whether to run exact or partial search
	 * @throws IOException when IO exception occurs
	 */
	public default void executeQuery(Path path, boolean exact) throws IOException {

		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);) {
			String line = null;
			SnowballStemmer stemmer = new SnowballStemmer(TextFileStemmer.ENGLISH);

			while ((line = reader.readLine()) != null) {
				executeQuery(line, exact, stemmer);
			}
		}
	}
}
