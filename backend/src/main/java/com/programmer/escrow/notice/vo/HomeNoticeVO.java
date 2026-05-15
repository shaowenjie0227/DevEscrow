package com.programmer.escrow.notice.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HomeNoticeVO {
    private Long id;
    private Integer noticeType;
    private String typeLabel;
    private String title;
    private String summary;
    private String targetUrl;
    private String coverUrl;
    private Integer sortOrder;
    private Integer status;
}
