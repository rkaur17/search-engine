import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * ResultBuilder class to execute queries and store results
 *
 * @author Ramneet Kaur CS 272 Software Development (University of San
 *         Francisco)
 * @version Fall 2021
 */
public class MultithreadedResultBuilder implements ResultBuilderInterface {
	/**
	 * map of query -> list of results
	 */
	private final TreeMap<String, ArrayList<InvertedIndex.Result>> resultMap;

	/**
	 * index to search
	 */
	private final ThreadSafeInvertedIndex index;
	/**
	 * workqueue
	 */
	private final WorkQueue queue;
	/**
	 * The lock used to protect concurrent access to the underlying index.
	 */
	private final SimpleReadWriteLock lock;

	/**
	 * constructor
	 *
	 * @param index to perform queries on
	 * @param queue the work queue to be used
	 */
	public MultithreadedResultBuilder(ThreadSafeInvertedIndex index, WorkQueue queue) {
		this.resultMap = new TreeMap<>();
		this.index = index;
		this.queue = queue;
		lock = new SimpleReadWriteLock();
	}

	/**
	 * Writes a JSON object to the output path provided.
	 *
	 * @param path output file path to write pretty JSON version of InvertedIndex
	 * @throws IOException when IOException occurs
	 */
	public void writeSearchResultJsonObject(Path path) throws IOException {
		lock.readLock().lock();
		SimpleJsonWriter.asResultObject(this.resultMap, path);
		lock.readLock().unlock();
	}

	/**
	 * cleans each line using TextFileStemmer calls the search method
	 *
	 * @param line    line to be parsed for queries
	 * @param exact   indicates whether to run exact or partial search
	 * @param stemmer the stemmer to use to stem query words
	 */
	@Override
	public void executeQuery(String line, boolean exact, Stemmer stemmer) {
		queue.execute(new QueryTask(line, exact));
	}

	/**
	 * Reads in all lines of query file and executes the queries on each line
	 *
	 * @param path  path to be parsed for queries
	 * @param exact indicates whether to run exact or partial search
	 * @throws IOException when IO exception occurs
	 */
	@Override
	public void executeQuery(Path path, boolean exact) throws IOException {
		ResultBuilderInterface.super.executeQuery(path, exact);
		queue.finish();
	}

	/**
	 * get unmodifiable keyset for result map
	 * 
	 * @return unmodifiable keyset for result map
	 */
	public Set<String> getResultMapKeySet() {
		return Collections.unmodifiableSet(resultMap.keySet());
	}

	/**
	 * get unmodifiable list of results for query
	 * 
	 * @param query the query who's results to return
	 * @return list of resuls for query specified
	 */
	public List<InvertedIndex.Result> getResultForQuery(String query) {
		if (resultMap.containsKey(query)) {
			return Collections.unmodifiableList(resultMap.get(query));
		}
		return Collections.emptyList();
	}

	/**
	 * Task class for WorkQueue
	 *
	 * @author Ramneet Kaur
	 *
	 */
	private class QueryTask implements Runnable {

		/**
		 * query line to execute
		 */
		private final String line;
		/**
		 * indicates whether to perform exact or partial search
		 */
		private final boolean exact;

		/**
		 * Task class constructor
		 *
		 * @param line  the query line to execute
		 * @param exact indicates whether to perform exact or partial search
		 */
		public QueryTask(String line, boolean exact) {
			this.line = line;
			this.exact = exact;
		}

		@Override
		public void run() {
			SnowballStemmer stemmer = new SnowballStemmer(TextFileStemmer.ENGLISH);
			TreeSet<String> queries = (TreeSet<String>) TextFileStemmer.uniqueStems(line, stemmer);
			String cleanedQuery = String.join(" ", queries);
			System.out.println(cleanedQuery);

			if (!cleanedQuery.isBlank()) {
				synchronized (resultMap) {
					if (resultMap.containsKey(cleanedQuery)) {
						return;
					}
				}
				ArrayList<InvertedIndex.Result> results = index.search(queries, exact);
				synchronized (resultMap) {
					resultMap.put(cleanedQuery, results);
				}
			}

		}

	}

}