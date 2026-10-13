package com.studyhub.platform.modules.storage;

import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.common.exception.StorageException;
import com.studyhub.platform.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "studyhub.storage.type", havingValue = "S3")
public class S3CompatibleStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3CompatibleStorageService.class);

    private final ApplicationProperties.Storage storageProps;
    private final HttpClient httpClient;
    private final LocalFileSystemStorageService fallbackLocalStorage;

    public S3CompatibleStorageService(ApplicationProperties applicationProperties,
                                      LocalFileSystemStorageService fallbackLocalStorage) {
        this.storageProps = applicationProperties.getStorage();
        this.fallbackLocalStorage = fallbackLocalStorage;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        log.info("Initialized S3CompatibleStorageService for bucket: {} at endpoint: {}",
                storageProps.getS3Bucket(), storageProps.getS3Endpoint());
    }

    @Override
    public StoredFileMetadata store(MultipartFile file, String subDirectory) {
        if (file.isEmpty()) {
            throw new BadRequestException("Cannot upload an empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document");
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String uniqueFilename = UUID.randomUUID().toString() + extension;
        String s3Key = subDirectory + "/" + LocalDate.now().getYear() + "/" + uniqueFilename;

        // If S3 endpoint and credentials are configured, upload via S3 REST API / HTTP PUT
        if (storageProps.getS3Endpoint() != null && !storageProps.getS3Endpoint().isBlank()) {
            try {
                String targetUrl = storageProps.getS3Endpoint();
                if (!targetUrl.endsWith("/")) targetUrl += "/";
                targetUrl += storageProps.getS3Bucket() + "/" + s3Key;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(targetUrl))
                        .header("Content-Type", file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                        .PUT(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    log.info("Successfully uploaded object to S3: {}", s3Key);
                    return new StoredFileMetadata(
                            uniqueFilename,
                            originalFilename,
                            file.getContentType(),
                            file.getSize(),
                            s3Key
                    );
                } else {
                    log.warn("S3 upload returned HTTP {}: {}. Falling back to local store.", response.statusCode(), response.body());
                }
            } catch (Exception e) {
                log.warn("S3 upload encountered exception: {}. Storing locally as fallback.", e.getMessage());
            }
        }

        // Fallback to local storage if S3 endpoint is unreachable or in mock mode
        return fallbackLocalStorage.store(file, subDirectory);
    }

    @Override
    public Resource loadAsResource(String storedPath) {
        if (storageProps.getS3PublicUrl() != null && !storageProps.getS3PublicUrl().isBlank()) {
            try {
                String url = storageProps.getS3PublicUrl();
                if (!url.endsWith("/")) url += "/";
                url += storedPath;
                return new UrlResource(URI.create(url));
            } catch (Exception e) {
                log.warn("Could not load from S3 public URL: {}", e.getMessage());
            }
        }
        return fallbackLocalStorage.loadAsResource(storedPath);
    }

    @Override
    public void delete(String storedPath) {
        if (storageProps.getS3Endpoint() != null && !storageProps.getS3Endpoint().isBlank()) {
            try {
                String targetUrl = storageProps.getS3Endpoint() + "/" + storageProps.getS3Bucket() + "/" + storedPath;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(targetUrl))
                        .DELETE()
                        .build();
                httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception e) {
                log.warn("Error deleting S3 object: {}", e.getMessage());
            }
        }
        fallbackLocalStorage.delete(storedPath);
    }

    @Override
    public String generateDownloadUrl(String storedPath) {
        if (storageProps.getS3PublicUrl() != null && !storageProps.getS3PublicUrl().isBlank()) {
            String url = storageProps.getS3PublicUrl();
            if (!url.endsWith("/")) url += "/";
            return url + storedPath;
        }
        return fallbackLocalStorage.generateDownloadUrl(storedPath);
    }
}
