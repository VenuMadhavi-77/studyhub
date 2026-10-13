package com.studyhub.platform.modules.resource;

import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.modules.resource.service.ResourceService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceUrlValidationTest {

    @Test
    void validateExternalUrl_ShouldAcceptValidHttpAndHttps() {
        assertDoesNotThrow(() -> ResourceService.validateExternalUrl("https://youtube.com/watch?v=12345"));
        assertDoesNotThrow(() -> ResourceService.validateExternalUrl("http://leetcode.com/problemset/all/"));
        assertDoesNotThrow(() -> ResourceService.validateExternalUrl("https://pwskills.com/courses"));
    }

    @Test
    void validateExternalUrl_ShouldRejectMaliciousProtocols() {
        assertThrows(BadRequestException.class, () -> ResourceService.validateExternalUrl("javascript:alert(1)"));
        assertThrows(BadRequestException.class, () -> ResourceService.validateExternalUrl("data:text/html;base64,PHNjcmlwdD4="));
        assertThrows(BadRequestException.class, () -> ResourceService.validateExternalUrl("vbscript:MsgBox(1)"));
        assertThrows(BadRequestException.class, () -> ResourceService.validateExternalUrl("ftp://example.com/file"));
        assertThrows(BadRequestException.class, () -> ResourceService.validateExternalUrl(""));
        assertThrows(BadRequestException.class, () -> ResourceService.validateExternalUrl(null));
    }
}
