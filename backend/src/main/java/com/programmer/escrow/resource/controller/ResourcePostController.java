package com.programmer.escrow.resource.controller;

import com.programmer.escrow.auth.context.UserContextHolder;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.resource.service.ResourcePostService;
import com.programmer.escrow.resource.vo.ResourcePostVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourcePostController {

    private final ResourcePostService resourcePostService;

    public ResourcePostController(ResourcePostService resourcePostService) {
        this.resourcePostService = resourcePostService;
    }

    @GetMapping
    public ApiResponse<List<ResourcePostVO>> list() {
        return ApiResponse.success(resourcePostService.listActive());
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Void> like(@PathVariable Long id) {
        resourcePostService.like(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/favorite")
    public ApiResponse<Void> favorite(@PathVariable Long id) {
        resourcePostService.favorite(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/share")
    public ApiResponse<Void> share(@PathVariable Long id) {
        resourcePostService.share(id);
        return ApiResponse.success(null);
    }
}
