import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * TextFileFinder to parse directory input and return list of txt/text files.
 *
 * @author Ramneet Kaur CS 272 Software Development (University of San
 *         Francisco)
 * @version Fall 2021
 */

public class TextFileFinder {

	/**
	 * When provided a directory as input, this function finds all .text and .txt
	 * (case-insensitive) files within that directory and returns them all in a
	 * list.
	 *
	 * @param path Path
	 * @return List of TextFiles
	 */
	public static boolean isTextFile(Path path) {
		String lower = path.getFileName().toString().toLowerCase();
		return lower.endsWith(".txt") || lower.endsWith(".text");
	}

	/**
	 * When provided a directory as input, this function finds all .text and .txt
	 * (case-insensitive) files within that directory and returns them all in a
	 * list.
	 *
	 * @param path Path
	 * @return List of TextFiles
	 * @throws IOException if an IO error occurs
	 */
	public static List<Path> getTextFiles(Path path) throws IOException {
		ArrayList<Path> list = new ArrayList<Path>();

		if (Files.isDirectory(path)) {
			getTextFilesHelper(path, list);
		} else {
			list.add(path);
		}

		return list;
	}

	/**
	 * helper function to traverse nested directories of files
	 *
	 * @param path Path
	 * @param list list of paths to append remaining paths to
	 * @return List of TextFiles
	 * @throws IOException if an IO error occurs
	 */
	private static List<Path> getTextFilesHelper(Path path, List<Path> list) throws IOException {
		if (Files.isDirectory(path)) {
			try (DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
				for (Path nested : paths) {
					getTextFilesHelper(nested, list);
				}
			}
		} else if (isTextFile(path)) {
			list.add(path);
		}

		return list;
	}

}
