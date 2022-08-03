import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Set;

/**
 * ThreadSafeInvertedIndex class to store words and their occurrences.
 *
 * @author Ramneet Kaur CS 272 Software Development (University of San
 *         Francisco)
 * @version Fall 2021
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {

	/** The lock used to protect concurrent access to the underlying index. */
	private final SimpleReadWriteLock lock;

	/**
	 * initializes a thread safe inverted index
	 */
	public ThreadSafeInvertedIndex() {
		super();
		lock = new SimpleReadWriteLock();
	}

	@Override
	public int size(String word) {
		lock.readLock().lock();
		try {
			return super.size(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int size(String word, String path) {
		lock.readLock().lock();
		try {
			return super.size(word, path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int size() {
		lock.readLock().lock();
		try {
			return super.size();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> get(String word) {
		lock.readLock().lock();
		try {
			return super.get(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<Integer> get(String location, String path) {
		lock.readLock().lock();
		try {
			return super.get(location, path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> get() {
		lock.readLock().lock();
		try {
			return super.get();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String word) {
		lock.readLock().lock();
		try {
			return super.contains(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String word, String path) {
		lock.readLock().lock();
		try {
			return super.contains(word, path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String word, String path, int position) {
		lock.readLock().lock();
		try {
			return super.contains(word, path, position);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void writeJsonObject(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.writeJsonObject(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void writeCountsJsonObject(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.writeCountsJsonObject(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void add(String word, String path, int position) {
		lock.writeLock().lock();
		try {
			super.add(word, path, position);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void add(String word, String path, int position, String htmlSnippet, Timestamp timestamp) {
		lock.writeLock().lock();
		try {
			super.add(word, path, position, htmlSnippet, timestamp);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public ArrayList<Result> exactSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.exactSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public ArrayList<Result> partialSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.partialSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void combineIndex(InvertedIndex localIndex) {
		lock.writeLock().lock();
		try {
			super.combineIndex(localIndex);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			return super.toString();
		} finally {
			lock.readLock().unlock();
		}
	}

}
