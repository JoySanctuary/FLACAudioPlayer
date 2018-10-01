package com.java.audioplayer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class FileHandler {

	/**
	 * Given a path name, parse files and directories in current path name, and
	 * store files into fileList and directories folderList, respectively.
	 * 
	 * @param pathName
	 * @param fileList
	 * @param folderList
	 * @return
	 * @throws IOException
	 */
	public static boolean recursiveDirectoryHandler(Path pathName,
			ArrayList<Path> fileList, ArrayList<Path> folderList,
			String fileExtension) {
		DirectoryStream<Path> stream;
		if (pathName == null) {
			return false;
		}
		boolean stillHasFolder = false;
		try {
			if (!pathName.isAbsolute()) {
				stream = Files.newDirectoryStream(pathName.toAbsolutePath());
			} else {
				stream = Files.newDirectoryStream(pathName);
			}
			for (Path entry : stream) {
				if (fileHander(entry, fileList, fileExtension)) {
					;
				} else if (entry.toFile().isDirectory()
						&& !folderList.contains(entry.toAbsolutePath())) {
					folderList.add(entry.toAbsolutePath());
					stillHasFolder = true;
				}
			}
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stillHasFolder;
	}

	// This method is used for processing pure files only, not for
	// directory.
	public static boolean fileHander(Path entry, ArrayList<Path> files,
			String fileExtension) {
		// String processedFullName = delimiterCanceller(file);
		boolean canHandleWithFileHandler = false;
		if (entry.toFile().isFile()
				&& entry.toAbsolutePath().toString().endsWith(fileExtension)
				&& !files.contains(entry)) {
			canHandleWithFileHandler = true;
			files.add(entry);
		}
		return canHandleWithFileHandler;
	}
}
