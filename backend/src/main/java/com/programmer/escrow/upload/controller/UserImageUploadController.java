package com.programmer.escrow.upload.controller;

import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.upload.service.ImageUploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
public class UserImageUploadController {

    private final ImageUploadService imageUploadService;

    public UserImageUploadController(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    @PostMapping("/images")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(imageUploadService.uploadImage(file));
    }
}
