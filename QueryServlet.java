import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.text.StringEscapeUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Servlet to GET handle requests to /headers.
 */
public class QueryServlet extends HttpServlet {
	/**
	 * ID used for serialization, which we are not using.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The decimal formatter used by this class.
	 */
	private static final DecimalFormat FORMATTER = new DecimalFormat("0.00000000");
	/**
	 * index to search
	 */
	ThreadSafeInvertedIndex index;
	/** The title to use for this webpage. */
	private static final String TITLE = "Cookies!";

	/** Used to fetch whether cookies were approved. */
	private static final String COOKIES_OK = "Cookies";

	/** Used to fetch the visited date from a cookie. */
	private static final String VISIT_DATE = "Visited";

	/** Used to fetch the visited date from a cookie. */
	private static final String QUERY = "Query";

	/** Used to fetch the visited count from a cookie. */
	private static final String VISIT_COUNT = "Count";

	/** Used to format date/time output. */
	private static final String DATE_FORMAT = "hh:mm a 'on' EEEE, MMMM dd yyyy";

	/** used to store search history */
	private ArrayList<Map<String, String>> history;

	/**
	 * Constructor
	 * 
	 * @param index the indverted index to be parsed
	 */
	public QueryServlet(ThreadSafeInvertedIndex index) {
		this.index = index;
		history = new ArrayList<Map<String, String>>();

	}

	/**
	 * Displays a form where users can enter a query to search When the button is
	 * pressed, submits the query back to / as a GET request.
	 *
	 * If a query is valid, fetch and display the results of the search.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// taken from
		// https://github.com/usf-cs272-fall2021/lectures/blob/main/ServletData/src/main/java/ReverseServer.java
		// make an input required: https://www.w3schools.com/tags/att_input_required.asp
		// reference for dark mode:
		// https://www.geeksforgeeks.org/how-to-make-dark-mode-for-websites-using-html-css-javascript/
		String html = """
				<!DOCTYPE html>
				<html lang="en">
				<head>
				  <meta charset="utf-8">
				  <title>%1$s</title>
				  <style>
				  		body{
						  background-color: white;
						  color: black;
						}

						.dark-mode {
						  background-color: black;
						  color: white;
						}
				</style>
				</head>
				<body>
				<img width="400" src="https://scontent-sjc3-1.xx.fbcdn.net/v/t1.15752-9/265206779_1266999673780934_3483648274031448305_n.png?_nc_cat=108&cb=c578a115-c1c39920&ccb=1-5&_nc_sid=ae9488&_nc_ohc=HWNhs712iDwAX8DmpLZ&_nc_ht=scontent-sjc3-1.xx&oh=03_AVL_ZiLJMCH-5z5M0nZJUq9Amw8h4TlzvGo1E1kcLRiQLA&oe=61E0B14A">
				<form method="get" action="/search">
				  <p>
				    <input required type="text" name="query" size="50"></input>
				  </p>
				  <p>
				    <input type="checkbox" name="exact" size="50"> Exact search</input>
				  </p>
				   <p>
				    <input type="checkbox" name="reverseSort" size="50"> Reverse sort</input>
				  </p>
				  <p>
				    <input type="checkbox" name="viewHistory" size="50"> View search history </input>
				  </p>
				  <p>
				    <input type="checkbox" name="clearHistory" size="50">Clear history</input>
				  </p>
				  <p>
				    <input type="checkbox" name="privateSearch" size="50">Turn off tracking</input>
				  </p>
				  <p>
				    <button>Search</button>
				  </p>
				  <p>
				    <button name="imFeelingLucky">I'm feeling lucky</button>
				  </p>
				</form>
				  <p>
				    <button onclick="darkMode()">Darkmode</button>
				  </p>
				<pre>
				%2$s
				</pre>
				<script>
				  function darkMode() {
				    var element = document.body;
				    element.classList.toggle("dark-mode");
				  }
				</script>
				</body>
				</html>
				""";

		String userInput = request.getParameter("query");
		String viewHistory = request.getParameter("viewHistory");

		// avoid xss attacks using apache commons text
		userInput = StringEscapeUtils.escapeHtml4(userInput);

		String output = "Last visited timestamp: unknown (tracking is turned off).%n%n";

		if (history.size() > 0) {
			Map<String, String> lastItemInHistory = history.get(history.size() - 1);
			output = String.format("Last visited timestamp: %s%n%n", lastItemInHistory.get(VISIT_DATE));
		}

		// store current query's history in a map if tracking is on
		if (request.getParameter("privateSearch") == null) {
			Map<String, String> queryHistory = new HashMap<>();
			queryHistory.put(QUERY, userInput);
			queryHistory.put(VISIT_DATE, new Timestamp(System.currentTimeMillis()).toString());
			// add it to overall history
			// needs to be threadsafe since multiple workers can access this at the same
			// time
			synchronized (history) {
				history.add(queryHistory);
			}
		}

		if (request.getParameter("clearHistory") != null) {
			history = new ArrayList<>();
		}

		if (userInput == null || userInput.isBlank()) {
			output = "";
		} else {
			try {
				SnowballStemmer stemmer = new SnowballStemmer(TextFileStemmer.ENGLISH);

				TreeSet<String> queries = (TreeSet<String>) TextFileStemmer.uniqueStems(userInput, stemmer);

				long startTime = System.currentTimeMillis();

				ArrayList<InvertedIndex.Result> results = index.search(queries, request.getParameter("exact") != null);
				if (request.getParameter("reverseSort") != null) {
					Collections.reverse(results);
				}

				// if user has selected i'm feeling lucky button, automatically open the top
				// search result
				if (request.getParameter("imFeelingLucky") != null) {
					if (results.size() > 0) {
						response.sendRedirect(results.get(0).getWhere());
					}
				}

				output = String.format(output + resultFormatter(results, startTime, userInput));

			} catch (Exception e) {
				userInput = "Error! Unable to search index for for: " + StringEscapeUtils.escapeHtml4(userInput);
			}
		}

		if (viewHistory != null) {
			output = String.format(output + historyFormatter());
		}

		PrintWriter out = response.getWriter();
		out.printf(html, "Search Engine", output, Thread.currentThread().getName());

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
	}

	/**
	 * Formats the results to be displayed on the webpage
	 *
	 * @param results   the list of results to show
	 * @param startTime the startTime of the query execution
	 * @param userInput the user's query
	 * @return output html
	 */
	public String resultFormatter(ArrayList<InvertedIndex.Result> results, long startTime, String userInput) {
		long endTime = System.currentTimeMillis();

		long searchTime = endTime - startTime;

		String output = String.format("<b> Fetching search results for query: </b>%s%n%n"
				+ "<b> Number of search results: </b>%s%n%n" + "<b> This search took: </b>%s millisecond(s).%n%n",
				userInput, results.size(), searchTime);

		for (InvertedIndex.Result result : results) {
			output = String.format(output
					+ "<b> where: </b><a href=\"%s\">%s</a>%n<b> score: </b>%s%n<b> count: </b>%s%n<b> preview: </b>%s%n<b> crawled at: </b>%s%n%n",
					result.getWhere(), result.getWhere(), FORMATTER.format(result.getScore()), result.getCount(),
					result.getSnippet(), result.getTimestamp());
		}

		if (results.size() == 0) {
			output = String.format(output + " Sorry! No results matched your query.%n%n");
		}

		return output;
	}

	/**
	 * Formats the search history to be displayed on the webpage
	 *
	 * @return output html
	 */
	public String historyFormatter() {

		String output = String.format("<b> Previous searches: </b>\n\n");

		for (Map<String, String> map : history) {

			if (map.get(QUERY) != null && map.get(VISIT_DATE) != null) {
				// do not trust values stored in cookies either!
				String decodedQuery = URLDecoder.decode(map.get(QUERY), StandardCharsets.UTF_8);
				String cleanedQuery = StringEscapeUtils.escapeHtml4(decodedQuery);

				String decodedTimestamp = URLDecoder.decode(map.get(VISIT_DATE), StandardCharsets.UTF_8);
				String cleanedVisitedTime = StringEscapeUtils.escapeHtml4(decodedTimestamp);

				output = String.format(output + "<b> Query: </b>%s%n<b> Searched at: </b>%s%n%n", cleanedQuery,
						cleanedVisitedTime);
			}

		}

		if (history.size() == 0) {
			output = String.format(output + " No search history yet!");
		}

		return output;
	}
}