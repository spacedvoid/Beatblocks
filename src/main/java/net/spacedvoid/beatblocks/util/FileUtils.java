package net.spacedvoid.beatblocks.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
	public static void createFile(Path path) throws IOException {
		Files.createDirectories(path.getParent());
		Files.createFile(path);
	}
}
