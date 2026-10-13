package com.studyhub.platform.modules.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    StoredFileMetadata store(MultipartFile file, String subDirectory);
    Resource loadAsResource(String storedFilename);
    void delete(String storedFilename);
    String generateDownloadUrl(String storedFilename);

    record StoredFileMetadata(
            String storedFilename,
            String originalFilename,
            String mimeType,
            long sizeBytes,
            String storagePath
    ) {}
}
