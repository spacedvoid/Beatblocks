package net.spacedvoid.beatblocks.resourcepack;

import net.spacedvoid.beatblocks.common.exceptions.UncheckedThrowable;
import net.spacedvoid.beatblocks.util.BLogger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
	private final BLogger logger = new BLogger("ZipUtil");

	/**
	 * Creates a zip file from a folder. <code>sourceDir</code> will be the root directory.
	 * <br><b>Critical bug</b>: some large files are modified, causing resource packs not being applied correctly.
	 * @param sourceDir The source folder path
	 * @param output The path of the result zip file.
	 * @return The path of the zip file.
	 * @throws IllegalArgumentException If the specified path(s) are invalid
	 * @throws RuntimeException If the deletion of the original file fails, or creating files have failed
	 */
	public Path zip(String sourceDir, String output) {
		Path sourceFolder = Path.of(sourceDir);
		Path outputFile = Path.of(output);
		if(!Files.isDirectory(sourceFolder) || !Files.isReadable(sourceFolder)) throw new IllegalArgumentException("The source directory cannot be found or read");
		try {
			Files.deleteIfExists(outputFile);
		}
		catch (IOException e) {
			logger.exception("Failed to delete original file", new UncheckedThrowable(e));
		}
		if(!Files.exists(outputFile.getParent())) {
			logger.info("The parent directory of output folder " + outputFile.getParent() + " does not exist. Creating...");
			try {
				Files.createDirectories(outputFile.getParent());
				Files.createFile(outputFile);
			}
			catch (IOException e) {
				logger.exception("Failed to create output file", new UncheckedThrowable(e));
			}
		}
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile.toFile())));
		     Stream<Path> paths = Files.walk(sourceFolder).filter(path -> !sourceFolder.equals(path))) {
			logger.info("Creating zip file at: " + output);
			paths.forEach(path -> {
				String name;
				if(Files.isDirectory(path)) {
					try {
						name = sourceFolder.relativize(path).toString().replace("\\", "/");
						zipOutputStream.putNextEntry(new ZipEntry(name.endsWith("/") ? name : name + "/"));
						zipOutputStream.closeEntry();
					} catch (IOException e) {
						throw new UncheckedThrowable(e);
					}
					logger.info("Added folder: " + name);
				}
				else {
					try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(path.toFile()))) {
						name = sourceFolder.relativize(path).toString().replace("\\", "/");
						zipOutputStream.putNextEntry(new ZipEntry(name));
						zipOutputStream.write(in.readAllBytes());
					} catch (IOException e) {
						throw new UncheckedThrowable(e);
					}
					logger.info("Added file: " + name);
				}
			});
			zipOutputStream.closeEntry();
			logger.info("Folder successfully compressed");
		} catch (IOException e) {
			throw new UncheckedThrowable(e);
		}
		return outputFile;
	}
}