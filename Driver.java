import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Ramneet Kaur
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class Driver {
	/**
	 *
	 * text key in argument map, value specifies the file or directory to read from
	 *
	 */
	private static final String textFlag = "-text";
	/**
	 *
	 * index key in argument map, value specifies the file or directory to write
	 * index to
	 *
	 */
	private static final String indexFlag = "-index";
	/**
	 *
	 * query key in argument map, value specifies the file or directory to query
	 * from
	 *
	 */
	private static final String queryFlag = "-query";
	/**
	 *
	 * exact key in argument map, value specifies whether to perform exact or
	 * partial search
	 *
	 */
	private static final String exactFlag = "-exact";
	/**
	 *
	 * results key in argument map, value specifies the file or directory to write
	 * query results to
	 *
	 */
	private static final String resultsFlag = "-results";
	/**
	 *
	 * counts key in argument map, value specifies the file or directory to write
	 * file counts to
	 *
	 */
	private static final String countsFlag = "-counts";
	/**
	 *
	 * threads key in argument map, value specifies the number of threads to use
	 *
	 */
	private static final String threadsFlag = "-threads";
	/**
	 *
	 * html key in argument map, value specifies the url to crawl to build the
	 * inverted index
	 *
	 */
	private static final String htmlFlag = "-html";
	/**
	 *
	 * max key in argument map, value specifies the total number of urls to crawl
	 *
	 */
	private static final String maxFlag = "-max";
	/**
	 *
	 * server key in argument map, value specifies the port number to use
	 *
	 */
	private static final String serverFlag = "-server";

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {

		ArgumentMap map = new ArgumentMap(args);

		InvertedIndex index = null;
		ResultBuilderInterface resultBuilder = null;
		ThreadSafeInvertedIndex safe = null;
		WorkQueue queue = null;

		if (map.hasFlag(threadsFlag) || map.hasFlag(htmlFlag) || map.hasFlag(serverFlag)) {
			safe = new ThreadSafeInvertedIndex();
			index = safe;

			int threads = map.getInteger(threadsFlag, 5);

			if (threads < 1) {
				threads = 5;
			}

			queue = new WorkQueue(threads);
			resultBuilder = new MultithreadedResultBuilder(safe, queue);
			if (map.hasFlag(htmlFlag)) {
				int totalUrls = 1;
				if (map.hasFlag(maxFlag) && map.getString(maxFlag) != null) {
					totalUrls = map.getInteger(maxFlag);
				}
				String urlPath = map.getString(htmlFlag);
				try {
					URL url = new URL(urlPath);
					WebCrawler crawler = new WebCrawler(totalUrls, queue, safe);
					crawler.startBuild(url);
				} catch (MalformedURLException e) {
					System.out.println("Invalid url provided: " + urlPath.toString());
				}
			}

		} else {

			index = new InvertedIndex();

			resultBuilder = new ResultBuilder(index);
		}

		if (map.hasFlag(textFlag) && map.getString(textFlag) != null) {
			Path path = map.getPath(textFlag);

			try {
				if (queue != null && safe != null) {
					MultithreadedInvertedIndexBuilder.buildIndex(queue, safe, path);
				} else {
					InvertedIndexBuilder.buildIndex(path, index);
				}

			} catch (IOException | InterruptedException e) {
				System.out.println("Unable to build the inverted index from path: " + path.toString());
			}
		}

		if (map.hasFlag(indexFlag)) {
			Path output = map.getPath(indexFlag, Path.of("index.json"));

			try {
				index.writeJsonObject(output);
			} catch (IOException e) {
				System.out.println(
						"There was a problem writing your InvertedIndex to an output file: " + output.toString());
			}
		}
		if (map.hasFlag(countsFlag)) {
			Path outPutFileName = map.getString(countsFlag) != null ? map.getPath(countsFlag) : Path.of("counts.json");
			try {
				index.writeCountsJsonObject(outPutFileName);
			} catch (IOException e) {
				System.out.println("Unable to read write counts to path: " + outPutFileName.toString());
			}

		}
		if (map.hasFlag(queryFlag)) {
			if (map.getString(queryFlag) != null) {
				Path path = map.getPath(queryFlag);
				boolean exact = map.hasFlag(exactFlag);

				try {
					resultBuilder.executeQuery(path, exact);
				} catch (IOException e) {
					System.out.println("Unable to read query from path: " + path.toString());
				}
			}
		}
		if (map.hasFlag(resultsFlag)) {
			Path outPutFileName = map.getString(resultsFlag) != null ? map.getPath(resultsFlag)
					: Path.of("results.json");
			try {
				resultBuilder.writeSearchResultJsonObject(outPutFileName);
			} catch (IOException e) {
				System.out.println("Unable to write results to path: " + outPutFileName.toString());
			}

		}
		if (map.hasFlag(serverFlag)) {
			int portNumber = map.getInteger(serverFlag, 8080);
			ServletContextHandler handler = new ServletContextHandler();
			ServletHolder holder;
			holder = new ServletHolder(new QueryServlet(safe));
			handler.addServlet(holder, "/search");

			Server server = new Server(portNumber);
			server.setHandler(handler);
			try {
				server.start();
				server.join();
			} catch (Exception e) {
				System.out.println("Unable to start server with port number: " + portNumber);
			}
		}
		if (queue != null) {
			queue.shutdown();
		}

	}

}