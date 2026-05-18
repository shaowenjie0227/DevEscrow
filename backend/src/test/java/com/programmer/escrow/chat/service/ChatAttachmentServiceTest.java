package com.programmer.escrow.chat.service;

import com.programmer.escrow.common.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatAttachmentServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void uploadAttachmentShouldRejectUnsupportedExtension() {
        ChatAttachmentService service = new ChatAttachmentService();
        ReflectionTestUtils.setField(service, "uploadBaseDir", tempDir.toString());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dangerous.exe",
                "application/octet-stream",
                "boom".getBytes()
        );

        BizException exception = assertThrows(BizException.class, () -> service.uploadAttachment(file));

        assertEquals(400, exception.getCode());
    }

    @Test
    void uploadAttachmentShouldStoreAllowedFileUnderChatTemp() throws Exception {
        ChatAttachmentService service = new ChatAttachmentService();
        ReflectionTestUtils.setField(service, "uploadBaseDir", tempDir.toString());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "brief.pdf",
                "application/pdf",
                "sample".getBytes()
        );

        var result = service.uploadAttachment(file);

        assertTrue(result.getUrl().contains("/uploads/chat-temp/"));
        assertEquals("brief.pdf", result.getOriginalName());
        assertTrue(Files.exists(tempDir.resolve("chat-temp").resolve(result.getFileName())));
        RequestContextHolder.resetRequestAttributes();
    }
}
