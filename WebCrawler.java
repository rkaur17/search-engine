import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Class responsible for crawling URLs to build multithreaded inverted index
 *
 * @author Ramneet Kaur
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class WebCrawler {
	/**
	 * the max number of redirects to parse
	 */
	int total;
	/**
	 * set of visited urls
	 */
	Set<URL> visitedUrls;
	/**
	 * the work queue to use to exevute tasks
	 */
	WorkQueue wq;
	/**
	 * the index to add to
	 */
	ThreadSafeInvertedIndex index;

	/**
	 * constructor for webcrawler
	 * 
	 * @param total the max number of urls to parse
	 * @param wq    the work queue to use
	 * @param index the index to add to
	 */
	public WebCrawler(int total, WorkQueue wq, ThreadSafeInvertedIndex index) {
		this.total = total;
		this.visitedUrls = new HashSet<>();
		this.wq = wq;
		this.index = index;
	}

	/**
	 * build function that starts crawling the url
	 * 
	 * @param start the seed url
	 */
	public void startBuild(URL start) {
		visitedUrls.add(start);
		wq.execute(new Task(start));
		wq.finish();
	}

	/**
	 * Task class for WorkQueue
	 *
	 * @author Ramneet Kaur
	 *
	 */
	private class Task implements Runnable {

		/**
		 * the url to parse
		 */
		private final URL url;

		/**
		 * Task class constructor
		 *
		 * @param url url from which to build index
		 */
		public Task(URL url) {
			this.url = url;
		}

		@Override
		public void run() {
			ThreadSafeInvertedIndex localIndex = new ThreadSafeInvertedIndex();
			String html = HtmlFetcher.fetch(url, 3);
			if (html == null) {
				return;
			}
			// some sort of link parsing from clean html to get list of links from the page
			List<URL> parsedUrls = new ArrayList<>();

			try {
				parsedUrls = LinkParser.getValidLinks(url, HtmlCleaner.stripBlockElements(html));
			} catch (MalformedURLException | URISyntaxException e) {
				System.out.println("Could not start server at specified port number.");
			}

			// for each url returned by the link parser
			// IFF we havent hit our total urls limit yet then
			// execute a task with the wq if the link has not already been parsed.. so need
			// to make a set of parsed links
			synchronized (visitedUrls) {
				for (URL parsed : parsedUrls) {
					if (!visitedUrls.contains(parsed) && visitedUrls.size() < total) {
						visitedUrls.add(parsed);
						wq.execute(new Task(parsed));
					}

				}
			}

			String cleanedHtml = HtmlCleaner.stripHtml(html);
			SnowballStemmer stemmer = new SnowballStemmer(TextFileStemmer.ENGLISH);

			int position = 1;
			for (String word : TextParser.parse(cleanedHtml)) {
				// store a snippet of each crawled webpage
				// if the page is less than 400 characters long, then just take the whole page,
				// otherwise take a 400 char snippet
				// trim so it doesn't have a bunch of white space on the webpage
				String snippet = cleanedHtml.substring(0, Math.min(400, cleanedHtml.length())).trim();
				localIndex.add(stemmer.stem(word).toString(), url.toString(), position, snippet,
						new Timestamp(System.currentTimeMillis()));
				position++;
			}
			index.combineIndex(localIndex);

		}

	}
}
