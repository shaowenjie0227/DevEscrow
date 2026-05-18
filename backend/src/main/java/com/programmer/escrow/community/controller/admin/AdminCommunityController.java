package com.programmer.escrow.community.controller.admin;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.community.dto.CommunityStatusDTO;
import com.programmer.escrow.community.service.CommunityService;
import com.programmer.escrow.community.vo.CommunityPostVO;
import com.programmer.escrow.community.vo.CommunityReplyVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/community")
public class AdminCommunityController {

    private final CommunityService communityService;

    public AdminCommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/posts")
    public ApiResponse<List<CommunityPostVO>> listPosts(@RequestParam(required = false) String forumName,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) Integer status) {
        return ApiResponse.success(communityService.listAdminPosts(forumName, keyword, status));
    }

    @GetMapping("/posts/{postId}/replies")
    public ApiResponse<List<CommunityReplyVO>> listReplies(@PathVariable Long postId,
                                                           @RequestParam(required = false) Integer status) {
        return ApiResponse.success(communityService.listAdminReplies(postId, status));
    }

    @PostMapping("/posts/{postId}/status")
    public ApiResponse<AdminOperationVO> updatePostStatus(@PathVariable Long postId,
                                                          @Valid @RequestBody CommunityStatusDTO dto) {
        return ApiResponse.success(communityService.updatePostStatus(postId, dto));
    }

    @DeleteMapping("/posts/{postId}")
    public ApiResponse<AdminOperationVO> deletePost(@PathVariable Long postId) {
        return ApiResponse.success(communityService.deletePost(postId));
    }

    @PostMapping("/replies/{replyId}/status")
    public ApiResponse<AdminOperationVO> updateReplyStatus(@PathVariable Long replyId,
                                                           @Valid @RequestBody CommunityStatusDTO dto) {
        return ApiResponse.success(communityService.updateReplyStatus(replyId, dto));
    }

    @DeleteMapping("/replies/{replyId}")
    public ApiResponse<AdminOperationVO> deleteReply(@PathVariable Long replyId) {
        return ApiResponse.success(communityService.deleteReply(replyId));
    }
}
