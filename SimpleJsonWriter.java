import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using tabs.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class SimpleJsonWriter {

	/** The decimal formatter used by this class. */
	private static final DecimalFormat FORMATTER = new DecimalFormat("0.00000000");

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asObject(TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements, Writer writer, int level)
			throws IOException {

		writer.write("{");

		if (!elements.isEmpty()) {
			writer.write("\n");
			Iterator<String> keys = elements.keySet().iterator();
			String k = keys.next();
			quote(k, writer, level + 1);
			writer.write(": ");
			asNestedArray(elements.get(k), writer, level + 1);
			while (keys.hasNext()) {
				writer.write(",\n");
				k = keys.next();
				quote(k, writer, level + 1);
				writer.write(": ");
				asNestedArray(elements.get(k), writer, level + 1);
			}
		}
		writer.write("\n}");

	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of integer objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asNestedArray(Map<String, ? extends Collection<Integer>> elements, Writer writer, int level)
			throws IOException {

		writer.write("{");

		if (!elements.isEmpty()) {
			writer.write("\n");
			Iterator<String> keys = elements.keySet().iterator();
			String k = keys.next();
			quote(k, writer, level + 1);
			writer.write(": ");
			asArray(elements.get(k), writer, level + 1);
			while (keys.hasNext()) {
				writer.write(",\n");
				k = keys.next();
				quote(k, writer, level + 1);
				writer.write(": ");
				asArray(elements.get(k), writer, level + 1);
			}
		}
		writer.write("\n");
		indent("}", writer, level);
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asArray(Collection<Integer> elements, Writer writer, int level) throws IOException {

		writer.write("[");
		if (!elements.isEmpty()) {
			writer.write("\n");

			Iterator<Integer> inner = elements.iterator();
			Integer innerK = inner.next();

			indent(innerK.toString(), writer, level + 1);

			while (inner.hasNext()) {
				writer.write(",\n");
				innerK = inner.next();
				indent(innerK.toString(), writer, level + 1);
			}
		}
		writer.write("\n");
		indent("]", writer, level);
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asObject(Map<String, Integer> elements, Writer writer, int level) throws IOException {

		List<String> sortedList = new ArrayList<>(elements.keySet());
		Collections.sort(sortedList);

		writer.write("{");

		if (!sortedList.isEmpty()) {
			writer.write("\n");

			Iterator<String> keys = sortedList.iterator();
			String k = keys.next();

			quote(k, writer, level + 1);
			writer.write(": " + elements.get(k).toString());

			while (keys.hasNext()) {
				writer.write(",\n");
				k = keys.next();
				quote(k, writer, level + 1);
				writer.write(": " + elements.get(k).toString());
			}

		}

		writer.write("\n}");
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param result the elements to write
	 * @param writer the writer to use
	 * @param level  the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void resultObjectHelper(InvertedIndex.Result result, Writer writer, int level) throws IOException {
		indent("{", writer, level);
		writer.write("\n");
		quote("count", writer, level + 1);
		writer.write(": " + result.getCount() + ",\n");

		quote("score", writer, level + 1);
		writer.write(": " + FORMATTER.format(result.getScore()) + ",\n");

		quote("where", writer, level + 1);
		writer.write(": \"" + result.getWhere() + "\"");

		writer.write("\n");
		indent("}", writer, level);
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void resultObjectHelper(ArrayList<InvertedIndex.Result> elements, Writer writer, int level)
			throws IOException {

		writer.write("[");

		if (!elements.isEmpty()) {
			Iterator<InvertedIndex.Result> results = elements.iterator();
			InvertedIndex.Result result = results.next();

			writer.write("\n");

			resultObjectHelper(result, writer, level + 1);
			while (results.hasNext()) {
				writer.write(",\n");
				result = results.next();

				resultObjectHelper(result, writer, level + 1);
			}

		}
		writer.write("\n");
		indent("]", writer, level);

	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asResultObject(TreeMap<String, ArrayList<InvertedIndex.Result>> elements, Writer writer,
			int level) throws IOException {

		writer.write('{');

		if (!elements.keySet().isEmpty()) {
			writer.write("\n");

			Iterator<String> keys = elements.keySet().iterator();
			String k = keys.next();

			quote(k.trim(), writer, level + 1);
			writer.write(": ");
			resultObjectHelper(elements.get(k), writer, level + 1);
			while (keys.hasNext()) {
				writer.write(",\n");
				k = keys.next();
				quote(k.trim(), writer, 1);
				writer.write(": ");
				resultObjectHelper(elements.get(k), writer, level + 1);

			}

		}

		writer.write("\n}");

	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asNestedArray(Map, Writer, int)
	 */
	public static void asNestedArray(Map<String, ? extends Collection<Integer>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asNestedArray(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 */
	public static void asObject(TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asObject(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 */
	public static void asObject(Map<String, Integer> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asObject(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 */
	public static void asResultObject(TreeMap<String, ArrayList<InvertedIndex.Result>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asResultObject(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static void asArray(Collection<Integer> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asArray(elements, writer, 0);
		}
	}

	/**
	 * general method that takes in a writer and level
	 *
	 *
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param level  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void indent(Writer writer, int level) throws IOException {
		while (level-- > 0) {
			writer.write("\t");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param level   the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void indent(String element, Writer writer, int level) throws IOException {
		indent(writer, level);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param level   the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void quote(String element, Writer writer, int level) throws IOException {
		indent(writer, level);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static String asArray(Collection<Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static String asObject(Map<String, Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			asObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns the elements as a nested pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asNestedArray(Map, Writer, int)
	 */
	public static String asNestedArray(Map<String, ? extends Collection<Integer>> elements) {
		try {
			StringWriter writer = new StringWriter();
			asNestedArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

}