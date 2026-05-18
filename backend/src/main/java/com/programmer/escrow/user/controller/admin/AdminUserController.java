package com.programmer.escrow.user.controller.admin;

import com.programmer.escrow.admin.context.AdminContextHolder;
import com.programmer.escrow.admin.dto.AdminUserMessageSendDTO;
import com.programmer.escrow.admin.dto.DeveloperAuditDTO;
import com.programmer.escrow.admin.dto.UserBanDTO;
import com.programmer.escrow.admin.service.AdminOpsService;
import com.programmer.escrow.admin.service.AdminQueryService;
import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.admin.vo.AdminUserVO;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.inbox.service.InboxMessageService;
import com.programmer.escrow.inbox.vo.InboxMessageVO;
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
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminOpsService adminOpsService;
    private final AdminQueryService adminQueryService;
    private final InboxMessageService inboxMessageService;

    public AdminUserController(AdminOpsService adminOpsService,
                               AdminQueryService adminQueryService,
                               InboxMessageService inboxMessageService) {
        this.adminOpsService = adminOpsService;
        this.adminQueryService = adminQueryService;
        this.inboxMessageService = inboxMessageService;
    }

    @GetMapping
    public ApiResponse<List<AdminUserVO>> listUsers(@RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) Integer userType,
                                                    @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminQueryService.listUsers(status, userType, keyword));
    }

    @PostMapping("/{userId}/ban")
    public ApiResponse<AdminOperationVO> banUser(@PathVariable Long userId, @Valid @RequestBody UserBanDTO dto) {
        return ApiResponse.success(adminOpsService.banUser(AdminContextHolder.getRequiredAdminId(), userId, dto));
    }

    @PostMapping("/{userId}/developer-audit")
    public ApiResponse<AdminOperationVO> auditDeveloper(@PathVariable Long userId,
                                                        @Valid @RequestBody DeveloperAuditDTO dto) {
        return ApiResponse.success(adminOpsService.auditDeveloper(userId, dto.getApprove(), dto.getRemark()));
    }

    @PostMapping("/{userId}/messages")
    public ApiResponse<InboxMessageVO> sendUserMessage(@PathVariable Long userId,
                                                       @Valid @RequestBody AdminUserMessageSendDTO dto) {
        return ApiResponse.success(inboxMessageService.adminSendMessage(
                AdminContextHolder.getRequiredAdminId(),
                userId,
                dto
        ));
    }
}
