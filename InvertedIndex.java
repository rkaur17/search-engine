import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * InvertedIndex class to store words and their occurrences.
 *
 * @author Ramneet Kaur CS 272 Software Development (University of San
 *         Francisco)
 * @version Fall 2021
 */
public class InvertedIndex {

	/**
	 *
	 * { "platypus": { "input/dangerous/venomous.txt": [ 2, 4 ],
	 * "input/mammals.txt": [ 3, 8 ] } }
	 *
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/**
	 * map of path to count to store total words in each file
	 */
	private final TreeMap<String, Integer> counts;

	/**
	 * map of path to snippet to store snipped of each file
	 */
	private final TreeMap<String, String> htmlSnippets;
	/**
	 * map of path to time crawled
	 */
	private final TreeMap<String, Timestamp> timeCrawled;

	/**
	 * default constructor
	 */
	public InvertedIndex() {
		index = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
		counts = new TreeMap<String, Integer>();
		htmlSnippets = new TreeMap<String, String>();
		timeCrawled = new TreeMap<String, Timestamp>();
	}

	/**
	 * Returns size of index for a particular word
	 *
	 * @param word to retrieve size for
	 * @return int
	 */
	public int size(String word) {
		return contains(word) ? index.get(word).size() : 0;
	}

	/**
	 * Returns number of times word appears in path
	 *
	 * @param word word whose number of occurence we are looking for
	 * @param path file where we are looking for the word
	 * @return int
	 */
	public int size(String word, String path) {
		TreeMap<String, TreeSet<Integer>> paths = index.get(word);
		if (paths != null) {
			if (paths.containsKey(path)) {
				return paths.get(path).size();
			}
		}
		return 0;
	}

	/**
	 * Returns size of index
	 *
	 * @return int
	 */
	public int size() {
		return index.size();
	}

	/**
	 * checks if index contains a specific word.
	 *
	 * @param word value to retrieve from index
	 * @return unmodifiable view of TreeMap for a particular word containing file
	 *         names and positions
	 */
	public Set<String> get(String word) {
		if (index.containsKey(word)) {
			return Collections.unmodifiableSet(index.get(word).keySet());
		}
		return Collections.emptySet();
	}

	/**
	 * checks if index contains a specific word and path.
	 *
	 * @param location word index to retrieve from index
	 * @param path     path value to retrieve from word index
	 * @return unmodifiable view of set of indices for a particular word and path
	 */
	public Set<Integer> get(String location, String path) {
		if (contains(location, path)) {
			return index.get(location).get(path);
		}
		return Collections.emptySet();

	}

	/**
	 * returns the entire index
	 *
	 * @return index
	 */
	public Set<String> get() {
		return Collections.unmodifiableSet(index.keySet());
	}

	/**
	 * checks if index contains a specific word.
	 *
	 * @param word word to look for
	 * @return true if the word is stored in index
	 */

	public boolean contains(String word) {
		return index.containsKey(word);
	}

	/**
	 * Returns true if word and path is stored in the index
	 *
	 * @param word word to check
	 * @param path path to check
	 * @return boolean
	 */

	public boolean contains(String word, String path) {
		return index.containsKey(word) && index.get(word).containsKey(path);
	}

	/**
	 * Returns true if index contains word, path, and position
	 *
	 * @param word     word to check
	 * @param path     path to check
	 * @param position position to look for
	 * @return boolean
	 */

	public boolean contains(String word, String path, int position) {
		return index.containsKey(word) && index.get(word).containsKey(path)
				&& index.get(word).get(path).contains(position);
	}

	/**
	 * returns string representation of the inverted index
	 */
	@Override
	public String toString() {
		return index.toString();
	}

	/**
	 * Writes a JSON object to the output path provided.
	 *
	 * @param path output file path to write pretty JSON version of InvertedIndex
	 * @throws IOException when IOException occurs
	 */
	public void writeJsonObject(Path path) throws IOException {
		SimpleJsonWriter.asObject(index, path);
	}

	/**
	 * Writes a JSON object to the output path provided.
	 *
	 * @param path output file path to write pretty JSON version of InvertedIndex
	 * @throws IOException when IOException occurs
	 */
	public void writeCountsJsonObject(Path path) throws IOException {
		SimpleJsonWriter.asObject(counts, path);
	}

	/**
	 * Adds or updates word's value with provided path/position
	 *
	 * @param word     the word to be added or updated in the index
	 * @param path     the path where the word was found
	 * @param position the position where the word was found in the file
	 */
	public void add(String word, String path, int position) {
		index.putIfAbsent(word, new TreeMap<>());
		index.get(word).putIfAbsent(path, new TreeSet<>());
		boolean modified = index.get(word).get(path).add(position);

		if (modified) {
			counts.put(path, counts.getOrDefault(path, 0) + 1);
		}

	}

	/**
	 * Adds or updates word's value with provided path/position
	 *
	 * @param word        the word to be added or updated in the index
	 * @param path        the path where the word was found
	 * @param position    the position where the word was found in the file
	 * @param htmlSnippet an html snippet of the url that was crawled
	 * @param timestamp   the time at which the page was crawled
	 */
	public void add(String word, String path, int position, String htmlSnippet, Timestamp timestamp) {
		add(word, path, position);
		htmlSnippets.put(path, htmlSnippet);
		// timeCrawled.put(path, timestamp);
	}

	/**
	 * method that calls exact or partial search based on value of exact
	 *
	 * @param queries the query line being searched for in the index
	 * @param exact   whether to perform exact search or partial
	 * @return ArrayList list of results matching the query
	 */
	public ArrayList<Result> search(Set<String> queries, boolean exact) {
		if (!exact) {
			return partialSearch(queries);
		} else {
			return exactSearch(queries);
		}
	}

	/**
	 * Exact search method that returns list of results matching input query exactly
	 *
	 * @param query         the word or query being searched for in the index
	 * @param results       the list to store results
	 * @param searchResults map of results seen so far
	 */
	private void searchHelper(String query, ArrayList<Result> results, HashMap<String, Result> searchResults) {
		TreeMap<String, TreeSet<Integer>> locationMap = index.get(query);

		for (String path : locationMap.keySet()) {
			Result result = searchResults.get(path);

			if (result == null) {
				result = new Result(path,
						htmlSnippets.get(path) != null ? htmlSnippets.get(path) : "No preview available",
						timeCrawled.get(path) != null ? timeCrawled.get(path).toString() : "unknown");
				searchResults.put(path, result);
				results.add(result);
			}

			result.update(query);
		}
	}

	/**
	 * Exact search method that returns list of results matching input query exactly
	 *
	 * @param queries the query line being searched for in the index
	 * @return ArrayList list of results matching the query
	 */
	public ArrayList<Result> exactSearch(Set<String> queries) {
		ArrayList<Result> results = new ArrayList<Result>();
		HashMap<String, Result> searchResults = new HashMap<String, Result>();

		for (String query : queries) {
			if (index.containsKey(query)) {
				searchHelper(query, results, searchResults);
			}
		}

		Collections.sort(results);
		return results;
	}

	/**
	 * finds all words in index that are partial matches and adds them to results
	 * output
	 *
	 * @param queries the set of words searched for in the index
	 * @return ArrayList list of results matching the query
	 */
	public ArrayList<Result> partialSearch(Set<String> queries) {
		ArrayList<Result> results = new ArrayList<Result>();
		HashMap<String, Result> searchResults = new HashMap<String, Result>();

		for (String queryLine : queries) {
			for (String query : index.tailMap(queryLine).keySet()) {
				if (!query.startsWith(queryLine)) {
					break;
				}
				searchHelper(query, results, searchResults);
			}
		}

		Collections.sort(results);
		return results;

	}

	/**
	 * Takes the index generated by a worker queue and combines it with overall
	 * index
	 *
	 * @param other the index to add to current index
	 */
	public void combineIndex(InvertedIndex other) {
		for (String word : other.index.keySet()) {
			TreeMap<String, TreeSet<Integer>> inner = other.index.get(word);

			if (this.index.containsKey(word)) {
				for (String path : inner.keySet()) {
					if (this.index.get(word).containsKey(path)) {
						this.index.get(word).get(path).addAll(other.index.get(word).get(path));
					} else {
						this.index.get(word).put(path, other.index.get(word).get(path));
					}
				}
			} else {
				this.index.put(word, inner);
			}
		}

		for (String location : other.counts.keySet()) {
			if (counts.containsKey(location)) {
				if (counts.get(location) < other.counts.get(location)) {
					counts.put(location, other.counts.get(location));
				}
			} else {
				counts.put(location, other.counts.get(location));
			}
		}

		for (String path : other.htmlSnippets.keySet()) {
			htmlSnippets.put(path, other.htmlSnippets.get(path));
			timeCrawled.put(path, other.timeCrawled.get(path));
		}
	}

	/**
	 * Data structure to store/score results of queries
	 *
	 * @author CS 272 Software Development (University of San Francisco)
	 * @version Fall 2021
	 */
	public class Result implements Comparable<Result> {
		/**
		 * count number of times query word appears in result path
		 */
		private int count;
		/**
		 * score that will help rank results (totalMatches in file / size of file)
		 */
		private double score;
		/**
		 * the path where word was found
		 */
		private final String where;
		/**
		 * snippet of the webpage if it exists
		 */
		private final String snippet;
		/**
		 * time page was cawled
		 */
		private final String timestamp;

		/**
		 * constructor
		 *
		 * @param where     the path where word was found
		 * @param snippet   snippet of the page that was crawled
		 * @param timestamp timestamp that the page was crawled at
		 */
		public Result(String where, String snippet, String timestamp) {
			this.count = 0;
			this.score = 0;
			this.where = where;
			this.snippet = snippet;
			this.timestamp = timestamp;
		}

		/**
		 * compare to helps sort these search results by ranking by score, then count,
		 * then comparing paths
		 *
		 * @param o the Result being compared to
		 * @return int result of comparing
		 */
		@Override
		public int compareTo(Result o) {
			if (Double.compare(this.score, o.score) == 0) {

				if (Integer.compare(this.count, o.count) == 0) {

					return this.where.compareToIgnoreCase(o.where);
				} else {
					return Integer.compare(o.count, this.count);
				}
			} else {
				return Double.compare(o.score, this.score);
			}
		}

		/**
		 * used to update the score of the Result
		 *
		 * @param word word of results to update
		 */
		private void update(String word) {
			this.count += index.get(word).get(where).size();
			this.score = (double) this.count / counts.get(where);
		}

		/**
		 *
		 * @return count for result
		 */
		public int getCount() {
			return count;
		}

		/**
		 *
		 * @return score for result
		 */
		public double getScore() {
			return score;
		}

		/**
		 *
		 * @return where for result
		 */
		public String getWhere() {
			return where;
		}

		/**
		 *
		 * @return snippet for result
		 */
		public String getSnippet() {
			return snippet;
		}

		/**
		 *
		 * @return timestamp result was crawled
		 */
		public String getTimestamp() {
			return timestamp;
		}

	}
}