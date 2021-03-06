/*
 *    Copyright [2018] [Justin Nelson]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.filesystem.handlers;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

public final class FileAndDirectory {

	private FileAndDirectory() {
	}

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
	public static void recursiveDirectoryHandler(Path pathName,
			LinkedList<Path> fileList, LinkedList<Path> folderList,
			String fileExtension) {
		DirectoryStream<Path> stream;
		if (pathName == null) {
			return;
		}
		try {
			if (!pathName.isAbsolute()) {
				stream = Files.newDirectoryStream(pathName.toAbsolutePath());
			} else {
				stream = Files.newDirectoryStream(pathName);
			}
			// As long as current entry is a directory, recursively process them
			// till the entry has been a file, which requires recursion, and the
			// base case is when the entry has been a file, add the file.

			for (Path entry : stream) {
				if (fileHander(entry, fileList, fileExtension)) {
					;
				} else if (Files.isDirectory(entry)) {
					recursiveDirectoryHandler(entry, fileList, folderList,
							fileExtension);
				}
			}
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// This method is used for processing pure files only, not for
	// directory.
	public static boolean fileHander(Path entry, LinkedList<Path> files,
			String fileExtension) {
		// String processedFullName = delimiterCanceller(file);
		boolean canHandleWithFileHandler = false;
		if (Files.isRegularFile(entry)
				&& entry.toString().endsWith(fileExtension)
				&& !files.contains(entry)) {
			canHandleWithFileHandler = true;
			files.add(entry);
		}
		return canHandleWithFileHandler;
	}
}
