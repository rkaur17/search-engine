import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Class responsible for running building the multithreaded inverted index.
 *
 * @author Ramneet Kaur
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class MultithreadedInvertedIndexBuilder extends InvertedIndexBuilder {
	/**
	 *
	 * sets up index based on argument paths
	 *
	 * @param queue the work queue to use
	 * @param index the index to add to
	 * @param start the path to retrieve all text files from
	 * @throws IOException          if an IO error occurs
	 * @throws InterruptedException if Interrupted error occurs
	 */
	public static void buildIndex(WorkQueue queue, ThreadSafeInvertedIndex index, Path start)
			throws IOException, InterruptedException {
		if (Files.isRegularFile(start)) {
			queue.execute(new Task(start, index));
		} else {
			List<Path> allTextFilePaths = TextFileFinder.getTextFiles(start);
			for (Path path : allTextFilePaths) {
				queue.execute(new Task(path, index));
			}
		}
		queue.finish();
	}

	/**
	 * Task class for WorkQueue
	 *
	 * @author Ramneet Kaur
	 *
	 */
	private static class Task implements Runnable {

		/**
		 * path to traverse to build index
		 */
		private final Path path;

		/**
		 * index to add parsed content
		 */
		private final ThreadSafeInvertedIndex index;

		/**
		 * Task class constructor
		 *
		 * @param path  Path from which to build index
		 * @param index the index to add parsed content to
		 */
		public Task(Path path, ThreadSafeInvertedIndex index) {
			this.path = path;
			this.index = index;
		}

		@Override
		public void run() {
			try {
				InvertedIndex localIndex = new InvertedIndex();
				InvertedIndexBuilder.stemFile(path, localIndex);
				index.combineIndex(localIndex);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

	}
}
