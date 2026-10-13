package com.studyhub.platform.modules.storage;

import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.common.exception.StorageException;
import com.studyhub.platform.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class LocalFileSystemStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileSystemStorageService.class);

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "doc", "docx", "ppt", "pptx", "txt", "zip", "png", "jpg", "jpeg");
    private final Path rootLocation;
    private final long maxFileSizeBytes;

    public LocalFileSystemStorageService(ApplicationProperties applicationProperties) {
        this.rootLocation = Paths.get(applicationProperties.getStorage().getLocalPath()).toAbsolutePath().normalize();
        this.maxFileSizeBytes = (long) applicationProperties.getStorage().getMaxSizeMb() * 1024 * 1024;
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize root storage folder", e);
        }
    }

    @Override
    public StoredFileMetadata store(MultipartFile file, String subDirectory) {
        if (file.isEmpty()) {
            throw new BadRequestException("Cannot upload an empty file");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new BadRequestException("File exceeds maximum allowed size of " + (maxFileSizeBytes / (1024 * 1024)) + " MB");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document");

        // Prevent path traversal
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Filename contains invalid path sequence: " + originalFilename);
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("File type ." + extension + " is not permitted for upload");
        }

        // Validate Magic Bytes for PDF files
        validateMagicBytes(file, extension);

        LocalDate now = LocalDate.now();
        String datedSubDir = String.format("%d/%02d", now.getYear(), now.getMonthValue());
        Path targetDir = rootLocation.resolve(subDirectory != null ? subDirectory + "/" + datedSubDir : datedSubDir).normalize();

        try {
            Files.createDirectories(targetDir);
            String storedFilename = UUID.randomUUID() + "." + extension;
            Path destinationFile = targetDir.resolve(storedFilename).normalize();

            // Verify the file is stored within root location
            if (!destinationFile.startsWith(rootLocation)) {
                throw new StorageException("Cannot store file outside target directory");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = rootLocation.relativize(destinationFile).toString().replace("\\", "/");
            String mimeType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

            log.info("Stored file: {} at relative path: {}", originalFilename, relativePath);
            return new StoredFileMetadata(storedFilename, originalFilename, mimeType, file.getSize(), relativePath);
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + originalFilename, e);
        }
    }

    @Override
    public Resource loadAsResource(String relativePath) {
        try {
            Path file = rootLocation.resolve(relativePath).normalize();
            if (!file.startsWith(rootLocation)) {
                throw new StorageException("Access to path outside storage root is forbidden");
            }

            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new StorageException("Could not read file: " + relativePath);
            }
        } catch (MalformedURLException e) {
            throw new StorageException("Could not read file from path: " + relativePath, e);
        }
    }

    @Override
    public void delete(String relativePath) {
        try {
            if (relativePath != null) {
                Path file = rootLocation.resolve(relativePath).normalize();
                if (file.startsWith(rootLocation)) {
                    Files.deleteIfExists(file);
                    log.info("Deleted file at: {}", relativePath);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to delete file at path: {}", relativePath, e);
        }
    }

    @Override
    public String generateDownloadUrl(String relativePath) {
        return "/api/v1/resources/download?path=" + relativePath;
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    private void validateMagicBytes(MultipartFile file, String extension) {
        if ("pdf".equalsIgnoreCase(extension)) {
            try (InputStream is = file.getInputStream()) {
                byte[] header = new byte[4];
                int read = is.read(header);
                if (read < 4 || header[0] != 0x25 || header[1] != 0x50 || header[2] != 0x44 || header[3] != 0x46) { // %PDF
                    throw new BadRequestException("Corrupted or invalid PDF file header");
                }
            } catch (IOException e) {
                throw new StorageException("Could not inspect file contents", e);
            }
        }
    }
}
