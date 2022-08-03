import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Class responsible for running Buildling the inverted index.
 *
 * @author Ramneet Kaur
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class InvertedIndexBuilder {

	/**
	 *
	 * sets up index based on argument paths
	 *
	 * @param start the path to retrieve all text files from
	 * @param index the index to add parsed content to
	 * @throws IOException if an IO error occurs
	 */
	public static void buildIndex(Path start, InvertedIndex index) throws IOException {
		if (Files.isRegularFile(start)) {
			stemFile(start, index);
		} else {
			List<Path> allTextFilePaths = TextFileFinder.getTextFiles(start);
			for (Path path : allTextFilePaths) {
				stemFile(path, index);
			}
		}
	}

	/**
	 * Reads in all lines of the given file, cleans them using TextFileStemmer, and
	 * adds them to the index.
	 *
	 * @param path  the path to be parsed
	 * @param index the index to add parsed content to
	 * @throws IOException if an error occurs
	 */
	public static void stemFile(Path path, InvertedIndex index) throws IOException {
		int position = 1;

		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);) {
			String line = null;
			String location = path.toString();
			SnowballStemmer stemmer = new SnowballStemmer(TextFileStemmer.ENGLISH);
			while ((line = reader.readLine()) != null) {
				for (String word : TextParser.parse(line)) {
					index.add(stemmer.stem(word).toString(), location, position);
					position++;
				}
			}
		}
	}
}