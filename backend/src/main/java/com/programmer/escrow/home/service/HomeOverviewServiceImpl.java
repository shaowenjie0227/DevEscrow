package com.programmer.escrow.home.service;

import com.programmer.escrow.config.properties.HomePageProperties;
import com.programmer.escrow.config.properties.WechatProperties;
import com.programmer.escrow.demand.mapper.DemandMapper;
import com.programmer.escrow.home.vo.HomeAdminContactVO;
import com.programmer.escrow.home.vo.HomeHeroMetricVO;
import com.programmer.escrow.home.vo.HomeInsightVO;
import com.programmer.escrow.home.vo.HomeOverviewVO;
import com.programmer.escrow.notice.mapper.HomeNoticeMapper;
import com.programmer.escrow.order.mapper.OrderMapper;
import com.programmer.escrow.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
public class HomeOverviewServiceImpl implements HomeOverviewService {

    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_ORDER_PAID = 2;
    private static final int STATUS_ORDER_IN_PROGRESS = 3;
    private static final int STATUS_ORDER_WAIT_ACCEPT = 4;
    private static final int STATUS_ORDER_COMPLETED = 5;
    private static final int STATUS_DEVELOPER_APPROVED = 2;

    private final HomePageProperties homePageProperties;
    private final WechatProperties wechatProperties;
    private final DemandMapper demandMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final HomeNoticeMapper homeNoticeMapper;

    public HomeOverviewServiceImpl(HomePageProperties homePageProperties,
                                   WechatProperties wechatProperties,
                                   DemandMapper demandMapper,
                                   UserMapper userMapper,
                                   OrderMapper orderMapper,
                                   HomeNoticeMapper homeNoticeMapper) {
        this.homePageProperties = homePageProperties;
        this.wechatProperties = wechatProperties;
        this.demandMapper = demandMapper;
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.homeNoticeMapper = homeNoticeMapper;
    }

    @Override
    public HomeOverviewVO getOverview() {
        long marketDemandCount = demandMapper.countMarketDemands();
        long activeUserCount = userMapper.countByStatus(STATUS_ACTIVE);
        long certifiedDeveloperCount = userMapper.countCertifiedDevelopers(STATUS_ACTIVE, STATUS_DEVELOPER_APPROVED);
        long activeOrderCount = orderMapper.countByStatuses(List.of(
                STATUS_ORDER_PAID,
                STATUS_ORDER_IN_PROGRESS,
                STATUS_ORDER_WAIT_ACCEPT
        ));
        long completedOrderCount = orderMapper.countByStatus(STATUS_ORDER_COMPLETED);
        long activeNoticeCount = homeNoticeMapper.selectActiveList().size();

        return HomeOverviewVO.builder()
                .adminContact(resolveAdminContact())
                .heroMetrics(buildHeroMetrics(marketDemandCount, activeOrderCount, certifiedDeveloperCount))
                .stats(buildStats(activeUserCount, completedOrderCount, activeNoticeCount))
                .marketSignals(resolveMarketSignals())
                .tradingRules(resolveTradingRules())
                .build();
    }

    private HomeAdminContactVO resolveAdminContact() {
        HomePageProperties.AdminContact adminContact = homePageProperties.getAdminContact();
        String qrImageUrl = StringUtils.hasText(adminContact.getQrImageUrl())
                ? adminContact.getQrImageUrl()
                : wechatProperties.getOfficialAccountQrImageUrl();

        return HomeAdminContactVO.builder()
                .title(StringUtils.hasText(adminContact.getTitle()) ? adminContact.getTitle() : "联系平台管理员")
                .message(StringUtils.hasText(adminContact.getMessage())
                        ? adminContact.getMessage()
                        : "如需入驻答疑、活动合作、问题反馈或人工协助，可通过下方二维码联系平台管理员。")
                .qrImageUrl(qrImageUrl)
                .build();
    }

    private List<HomeHeroMetricVO> buildHeroMetrics(long marketDemandCount,
                                                    long activeOrderCount,
                                                    long certifiedDeveloperCount) {
        return List.of(
                HomeHeroMetricVO.builder()
                        .label("公开需求")
                        .value(formatCount(marketDemandCount))
                        .hint("已审核并在市场公开展示的需求数，适合快速浏览与报价。")
                        .build(),
                HomeHeroMetricVO.builder()
                        .label("进行中协作")
                        .value(formatCount(activeOrderCount))
                        .hint("统计当前处于托管、开发中与待验收阶段的真实协作订单。")
                        .build(),
                HomeHeroMetricVO.builder()
                        .label("认证开发者")
                        .value(formatCount(certifiedDeveloperCount))
                        .hint("仅统计已通过开发者审核、可直接参与沟通与接单的账号。")
                        .build()
        );
    }

    private List<HomeInsightVO> buildStats(long activeUserCount,
                                           long completedOrderCount,
                                           long activeNoticeCount) {
        return List.of(
                HomeInsightVO.builder()
                        .label("注册用户")
                        .value(formatCount(activeUserCount))
                        .note("实时统计当前可正常使用的平台账号规模，用于展示平台活跃基础。")
                        .build(),
                HomeInsightVO.builder()
                        .label("完结协作")
                        .value(formatCount(completedOrderCount))
                        .note("累计完成验收的协作订单数量，可直接反映平台交付沉淀。")
                        .build(),
                HomeInsightVO.builder()
                        .label("公告与活动")
                        .value(formatCount(activeNoticeCount))
                        .note("由现有首页公告后台维护，上下线后会在首页右侧实时展示。")
                        .build()
        );
    }

    private List<HomeInsightVO> resolveMarketSignals() {
        if (homePageProperties.getMarketSignals() == null || homePageProperties.getMarketSignals().isEmpty()) {
            return List.of(
                    HomeInsightVO.builder()
                            .label("高需求方向")
                            .value("企业后台 / 小程序 / AI 工具")
                            .note("近阶段更容易进入有效沟通的需求，优先来自已审核通过的真实发布内容。")
                            .build(),
                    HomeInsightVO.builder()
                            .label("更易成交")
                            .value("Demo / MVP / 二期接手")
                            .note("范围边界清晰、里程碑明确的需求，通常更容易快速进入报价与排期。")
                            .build(),
                    HomeInsightVO.builder()
                            .label("沟通重点")
                            .value("交付范围 / 节点 / 预算")
                            .note("上线文案建议优先强调范围清晰和交付节奏，减少无效往返。")
                            .build()
            );
        }

        return homePageProperties.getMarketSignals().stream()
                .map(item -> HomeInsightVO.builder()
                        .label(item.getLabel())
                        .value(item.getValue())
                        .note(item.getNote())
                        .build())
                .toList();
    }

    private List<String> resolveTradingRules() {
        if (homePageProperties.getTradingRules() == null || homePageProperties.getTradingRules().isEmpty()) {
            return List.of(
                    "先明确交付范围和里程碑，再进入报价，可以显著减少来回确认成本。",
                    "优先验证关键流程是否跑通，再逐步补齐非核心页面和扩展能力。",
                    "把预算、周期、协作方式说清楚，通常更容易在当天进入有效沟通。"
            );
        }
        return homePageProperties.getTradingRules();
    }

    private String formatCount(long value) {
        return NumberFormat.getIntegerInstance(Locale.CHINA).format(value);
    }
}
