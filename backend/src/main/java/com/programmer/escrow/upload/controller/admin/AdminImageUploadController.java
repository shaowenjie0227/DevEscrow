package com.programmer.escrow.upload.controller.admin;

import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.common.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/uploads")
public class AdminImageUploadController {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    @Value("${app.upload.base-dir:uploads}")
    private String uploadBaseDir;

    @PostMapping("/images")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "请选择图片文件");
        }
        if (!StringUtils.hasText(file.getContentType()) || !file.getContentType().startsWith("image/")) {
            throw new BizException(400, "仅支持上传图片文件");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BizException(400, "图片大小不能超过 5MB");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        Path basePath = Paths.get(uploadBaseDir).toAbsolutePath().normalize();
        Path imageDir = basePath.resolve("images");
        Path targetPath = imageDir.resolve(fileName);

        try {
            Files.createDirectories(imageDir);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BizException(500, "图片上传失败");
        }

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/images/")
                .path(fileName)
                .toUriString();

        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("fileName", fileName);
        return ApiResponse.success(result);
    }

    private String resolveExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            return ".png";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}
