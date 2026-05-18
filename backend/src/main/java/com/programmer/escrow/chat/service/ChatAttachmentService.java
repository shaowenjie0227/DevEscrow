package com.programmer.escrow.chat.service;

import com.programmer.escrow.chat.vo.ChatAttachmentUploadVO;
import com.programmer.escrow.common.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class ChatAttachmentService {

    private static final long MAX_ATTACHMENT_SIZE = 20L * 1024L * 1024L;
    private static final long EXPIRE_MILLIS = 24L * 60L * 60L * 1000L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".png", ".jpg", ".jpeg", ".webp", ".gif",
            ".pdf", ".txt", ".doc", ".docx",
            ".xls", ".xlsx", ".ppt", ".pptx",
            ".zip"
    );

    @Value("${app.upload.base-dir:uploads}")
    private String uploadBaseDir;

    public ChatAttachmentUploadVO uploadAttachment(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "attachment is required");
        }
        if (file.getSize() > MAX_ATTACHMENT_SIZE) {
            throw new BizException(400, "attachment size cannot exceed 20MB");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BizException(400, "unsupported attachment type");
        }
        String safeBaseName = java.util.UUID.randomUUID().toString().replace("-", "");
        String fileName = safeBaseName + extension;
        Path attachmentDir = resolveAttachmentDir();
        Path targetPath = attachmentDir.resolve(fileName);

        try {
            Files.createDirectories(attachmentDir);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BizException(500, "attachment upload failed");
        }

        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(System.currentTimeMillis() + EXPIRE_MILLIS),
                ZoneId.systemDefault()
        );

        return ChatAttachmentUploadVO.builder()
                .url(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uploads/chat-temp/")
                        .path(fileName)
                        .toUriString())
                .fileName(fileName)
                .originalName(resolveOriginalName(file.getOriginalFilename(), fileName))
                .contentType(file.getContentType())
                .size(file.getSize())
                .expiresAt(expiresAt)
                .build();
    }

    @Scheduled(fixedDelay = 60L * 60L * 1000L)
    public void cleanupExpiredAttachments() {
        Path attachmentDir = resolveAttachmentDir();
        if (!Files.exists(attachmentDir)) {
            return;
        }
        long expireBefore = System.currentTimeMillis() - EXPIRE_MILLIS;
        try (Stream<Path> files = Files.list(attachmentDir)) {
            files.filter(Files::isRegularFile)
                    .forEach(path -> deleteIfExpired(path, expireBefore));
        } catch (IOException ex) {
            throw new IllegalStateException("failed to cleanup expired chat attachments", ex);
        }
    }

    private void deleteIfExpired(Path path, long expireBefore) {
        try {
            long lastModified = Files.getLastModifiedTime(path).toMillis();
            if (lastModified <= expireBefore) {
                Files.deleteIfExists(path);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("failed to delete expired chat attachment: " + path, ex);
        }
    }

    private Path resolveAttachmentDir() {
        return Paths.get(uploadBaseDir).toAbsolutePath().normalize().resolve("chat-temp");
    }

    private String resolveExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }

    private String resolveOriginalName(String originalFilename, String fallback) {
        if (!StringUtils.hasText(originalFilename)) {
            return fallback;
        }
        return Paths.get(originalFilename).getFileName().toString();
    }
}
