package net.spacedvoid.beatblocks.resourcepack;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
	private List<String> fileList = new ArrayList<>();

	/**
	 * Creates a zip file from a folder.
	 * @param sourceDir The source folder path
	 * @param output The path of the result zip file.
	 * @return The path of the zip file.
	 * @throws IllegalArgumentException If the specified path(s) are invalid
	 * @throws RuntimeException If the deletion of the original file fails, or creating files have failed
	 */
	public Path zip(String sourceDir, String output) {
		File sourceFolder = new File(sourceDir);
		File outputFile = new File(output);
		if(!Files.isDirectory(sourceFolder.toPath()) || !Files.isReadable(sourceFolder.toPath())) throw new IllegalArgumentException("The source directory cannot be found or read");
		try {
			Files.deleteIfExists(outputFile.toPath());
		}
		catch (IOException e) {
			exception("Failed to delete original file", e);
		}
		if(!Files.exists(outputFile.getParentFile().toPath())) {
			info("The parent directory of output folder " + outputFile.getParentFile().getPath() + " does not exist. Creating...");
			try {
				Files.createDirectories(outputFile.getParentFile().toPath());
				Files.createFile(outputFile.toPath());
			}
			catch (IOException e) {
				exception("Failed to create output file", e);
			}
		}

		fileList = generateFileList(sourceFolder.toPath(), sourceDir);

		byte[] buffer = new byte[1024];
		String source = sourceFolder.getName();

		try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(outputFile))) {
			info("Creating zip file at: " + output);
			for(String file : fileList) {
				info("Added file: " + file);
				ZipEntry entry = new ZipEntry(source + File.separator + file);
				outputStream.putNextEntry(entry);
				try (FileInputStream in = new FileInputStream(sourceDir + File.separator + file)) {
					int length;
					while ((length = in.read(buffer)) > 0) {
						outputStream.write(buffer, 0, length);
					}
				}
			}
			outputStream.closeEntry();
			info("Folder successfully compressed");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputFile.toPath();
	}

	private List<String> generateFileList(Path node, String sourceDir) {
		if(Files.isRegularFile(node)) {
			fileList.add(generateZipEntry(node.toString(), sourceDir));
		}
		if(Files.isDirectory(node)) {
			try (Stream<Path> stream = Files.list(node)) {
				stream.forEach(path -> generateFileList(path, sourceDir));
			}
			catch (IOException e) {
				exception("An IOException occurred while walking files", e);
			}
		}
		return fileList;
	}

	private String generateZipEntry(String file, String sourceDir) {
		return file.substring(sourceDir.length() + 1);
	}

	private void info(String message) {
		Bukkit.getLogger().log(Level.INFO, "[ZipUtil] " + message);
	}

	private void exception(String message, Exception exception) {
		Bukkit.getLogger().warning("[ZipUtil] " + message + ": " + exception.getMessage());
		throw new RuntimeException(message);
	}
}