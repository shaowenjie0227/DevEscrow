package com.programmer.escrow.community.controller;

import com.programmer.escrow.auth.context.LoginUser;
import com.programmer.escrow.auth.context.UserContextHolder;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.community.dto.CommunityPostCreateDTO;
import com.programmer.escrow.community.dto.CommunityReplyCreateDTO;
import com.programmer.escrow.community.service.CommunityService;
import com.programmer.escrow.community.vo.CommunityPostVO;
import com.programmer.escrow.community.vo.CommunityReplyVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/community/posts")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping
    public ApiResponse<List<CommunityPostVO>> listPosts(@RequestParam(required = false) String sort,
                                                        @RequestParam(required = false) String forumName,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) Boolean mine) {
        return ApiResponse.success(communityService.listPosts(sort, forumName, keyword, mine, currentUserIdOrNull()));
    }

    @GetMapping("/{id}")
    public ApiResponse<CommunityPostVO> getPost(@PathVariable Long id) {
        return ApiResponse.success(communityService.getPost(id, currentUserIdOrNull()));
    }

    @PostMapping
    public ApiResponse<CommunityPostVO> createPost(@Valid @RequestBody CommunityPostCreateDTO dto) {
        return ApiResponse.success(communityService.createPost(UserContextHolder.getRequiredUserId(), dto));
    }

    @PostMapping("/{id}/like")
    public ApiResponse<CommunityPostVO> likePost(@PathVariable Long id) {
        return ApiResponse.success(communityService.toggleLikePost(UserContextHolder.getRequiredUserId(), id));
    }

    @PostMapping("/{id}/favorite")
    public ApiResponse<CommunityPostVO> favoritePost(@PathVariable Long id) {
        return ApiResponse.success(communityService.toggleFavoritePost(UserContextHolder.getRequiredUserId(), id));
    }

    @GetMapping("/{id}/replies")
    public ApiResponse<List<CommunityReplyVO>> listReplies(@PathVariable Long id) {
        return ApiResponse.success(communityService.listReplies(id));
    }

    @PostMapping("/{id}/replies")
    public ApiResponse<CommunityReplyVO> createReply(@PathVariable Long id, @Valid @RequestBody CommunityReplyCreateDTO dto) {
        return ApiResponse.success(communityService.createReply(UserContextHolder.getRequiredUserId(), id, dto));
    }

    private Long currentUserIdOrNull() {
        LoginUser loginUser = UserContextHolder.get();
        return loginUser == null ? null : loginUser.getUserId();
    }
}
