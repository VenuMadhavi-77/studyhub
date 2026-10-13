package com.studyhub.platform.modules.ai.service;

import com.studyhub.platform.common.exception.BadRequestException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class DocumentExtractorService {

    private static final Logger log = LoggerFactory.getLogger(DocumentExtractorService.class);
    private static final int MAX_PAGES = 10;
    private static final int MAX_CHARS = 15000;

    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "";
        }

        try (InputStream is = file.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(1);
                stripper.setEndPage(Math.min(document.getNumberOfPages(), MAX_PAGES));

                String text = stripper.getText(document);
                if (text == null || text.isBlank()) {
                    return "";
                }

                String sanitized = text.replaceAll("\\r\\n|\\r", "\n").trim();
                if (sanitized.length() > MAX_CHARS) {
                    sanitized = sanitized.substring(0, MAX_CHARS) + "\n... [Document content truncated for AI processing]";
                }
                return sanitized;
            }
        } catch (Exception e) {
            log.warn("Failed to extract text from PDF: {}", e.getMessage());
            return "";
        }
    }
}
