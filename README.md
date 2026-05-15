# 程序员担保交易平台

一个面向程序员接单场景的担保交易平台 MVP，覆盖需求发布、开发者报价、订单托管、阶段交付、纠纷处理，以及内容运营和后台管理等核心流程。

这个仓库采用前后端分离结构，适合作为以下场景的基础工程：

- 毕设 / 课程设计 / 实训项目
- 外包撮合平台原型
- 接单担保平台 MVP 骨架
- Spring Boot + Vue 3 全栈练手项目

## 项目简介

平台围绕“需求方找开发者，平台做担保”这一主线设计，目标是把交易流程尽量标准化、可追踪、可仲裁。

在当前版本中，仓库已经具备以下几类能力：

- 公共门户：需求大厅、需求详情、资源分享、社区交流、文章/课程/知识库入口
- 需求方端：发布需求、查看报价、创建订单、支付托管、验收/驳回、发起纠纷
- 开发者端：完善资料、申请开发者身份、浏览需求、提交报价、推进订单交付、发起纠纷
- 管理后台：管理员登录、首页 Banner 管理、资源管理、技能标签管理、需求分类管理、知识库管理、需求审核、订单管理、纠纷管理、用户管理

整体更偏向“可运行、可演示、可扩展”的 MVP，而不是已经完全打磨完毕的生产系统。

## 技术栈

| 层级 | 技术方案 |
| --- | --- |
| 后端 | Spring Boot 3.3.5、MyBatis、MySQL、Redis |
| 前端 | Vue 3、Vue Router、Pinia、Element Plus、Vite |
| 样式 | Sass、Tailwind CSS |
| 构建工具 | Maven、npm |
| 运行环境 | JDK 17+、Node.js 18+、MySQL 8+、Redis 6+ |

## 核心业务流程

1. 需求方注册登录后发布项目需求。
2. 管理员审核需求，通过后进入需求大厅。
3. 开发者浏览需求并提交报价方案。
4. 需求方选择合适报价，创建订单并进行托管支付。
5. 开发者接单后开始开发，按阶段提交交付物。
6. 需求方进行验收，可接受、驳回或发起纠纷。
7. 管理员介入处理争议，完成仲裁或关闭工单。

## 功能概览

### 公共门户

- 需求大厅浏览
- 需求详情查看
- 资源分享内容展示
- 社区帖子与回复
- 文章、课程、知识库入口
- 登录、注册、个人中心入口

### 需求方端

- 发布需求
- 查看自己发布的需求
- 查看报价列表
- 创建订单
- 支付托管
- 验收交付
- 驳回阶段成果
- 发起纠纷

### 开发者端

- 申请开发者身份
- 维护个人资料与技能标签
- 浏览可接需求
- 提交报价
- 接单、开工、提交交付
- 查看自己的订单与纠纷

### 管理后台

- 管理员登录
- 用户管理与封禁
- 需求审核
- 订单管理
- 纠纷处理
- 首页 Banner 管理
- 需求分类管理
- 技能标签管理
- 资源管理
- 知识库管理

## 项目结构

```text
.
├─ backend/                     # Spring Boot 后端
│  ├─ src/main/java/com/programmer/escrow
│  │  ├─ auth/                  # 用户认证、注册、登录、开发者资料
│  │  ├─ demand/                # 需求发布、审核、分类
│  │  ├─ quote/                 # 开发者报价
│  │  ├─ order/                 # 订单、阶段流转、支付/验收
│  │  ├─ dispute/               # 纠纷处理
│  │  ├─ admin/                 # 后台登录与后台查询/操作
│  │  ├─ banner/                # 首页 Banner
│  │  ├─ resource/              # 资源分享
│  │  ├─ kb/                    # 知识库
│  │  ├─ community/             # 社区帖子与回复
│  │  └─ skill/                 # 技能标签
│  └─ src/main/resources/
│     ├─ application.yml        # 后端配置
│     └─ mapper/                # MyBatis XML
├─ frontend/                    # Vue 3 前端
│  ├─ src/api/                  # 接口封装
│  ├─ src/router/               # 路由
│  ├─ src/layouts/              # 三端布局
│  ├─ src/views/                # 页面视图
│  ├─ src/components/           # 公共组件
│  ├─ src/stores/               # Pinia 状态管理
│  └─ src/mock/                 # 部分首页展示 mock 数据
└─ sql/                         # 数据库脚本
   ├─ 01_init_schema.sql        # 基础业务表
   ├─ 02_platform_content_upgrade.sql
   └─ 03_community_module.sql
```

## 数据库说明

当前数据库脚本已经覆盖平台主要业务表，核心包括：

- 用户与管理员：`user`、`admin_user`
- 交易主线：`demand`、`quote`、`orders`、`order_stage`、`dispute`
- 预留沟通：`chat_message`
- 内容与运营：`demand_category`、`skill_tag`、`home_banner`、`knowledge_base`、`resource_post`
- 社区：`community_post`、`community_reply`

建议按以下顺序执行 SQL 脚本：

1. `sql/01_init_schema.sql`
2. `sql/02_platform_content_upgrade.sql`
3. `sql/03_community_module.sql`

## 环境要求

- JDK 17 或更高版本
- Maven 3.9 或兼容版本
- Node.js 18 或更高版本
- MySQL 8.0 或更高版本
- Redis 6 或更高版本

## 快速开始

### 1. 初始化数据库

```bash
mysql -uroot -p < sql/01_init_schema.sql
mysql -uroot -p programmer_escrow < sql/02_platform_content_upgrade.sql
mysql -uroot -p programmer_escrow < sql/03_community_module.sql
```

### 2. 配置后端

编辑文件 `backend/src/main/resources/application.yml`，至少确认以下配置项：

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.data.redis.host`
- `spring.data.redis.port`
- `spring.mail.*`
- `app.upload.base-dir`

说明：

- 默认后端端口为 `8080`
- 默认上传目录为 `uploads`
- 当前配置文件更像本地开发样例，提交到正式环境前建议改为环境变量或多环境配置

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

如果你的机器上同时装了多个 JDK，请先确认 Maven 使用的是 JDK 17+：

```bash
mvn -v
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 5. 访问项目

- 前端地址：`http://localhost:5173`
- 后端地址：`http://localhost:8080`

## 默认账号

后台管理员默认账号如下：

- 用户名：`admin`
- 密码：`admin123456`

说明：

- 当 `sql/01_init_schema.sql` 中管理员密码哈希仍然是占位值时，后端会接受 `admin123456` 作为引导密码
- 如果你已经把占位哈希替换为真实 bcrypt 密码，请使用你自己的密码登录

## 开发说明

### 前后端联调

- 前端接口基地址当前写在 `frontend/src/api/http.js`
- 默认请求后端 `http://localhost:8080`
- 如果你修改了后端端口或部署地址，需要同步修改这里

### 当前实现状态

- 已具备完整的三端页面结构和主要业务接口分层
- 内容运营模块已经接入数据库和后台管理
- 社区模块已经包含帖子与回复能力
- 首页部分展示数据仍混合了接口数据与本地 mock 数据
- 数据库已预留聊天消息表，但当前仓库中还没有完整的实时聊天实现
- 自动化测试目前较少，当前更适合快速原型、演示和二次开发

### 建议的后续迭代方向

- 接入真正的支付/托管能力
- 增加对象存储与文件上传鉴权
- 补充短信、邮件、通知中心
- 增加 WebSocket 即时沟通能力
- 完善操作日志、审计日志、风控规则
- 增加 Swagger / OpenAPI 文档
- 增加单元测试、集成测试和端到端测试
- 拆分 `dev`、`test`、`prod` 多环境配置

## 常见问题

### 1. 后端启动失败，提示 Java 版本不对

请先执行：

```bash
mvn -v
```

确认 Maven 实际绑定的是 JDK 17 或更高版本，而不是 JDK 8。

### 2. 前端页面能打开，但接口请求失败

优先检查：

- 后端是否已经启动
- MySQL 和 Redis 是否可连接
- `frontend/src/api/http.js` 中的接口地址是否和后端一致
- 浏览器控制台和后端日志是否有报错信息

### 3. 管理员无法登录

请检查：

- 数据库中是否存在 `admin` 账户
- 管理员状态是否为启用
- 是否仍在使用引导密码占位逻辑

## 说明

如果你准备把这个项目继续做成完整产品，建议优先补齐以下三件事：

1. 把配置文件中的本地凭据改为环境变量或私有配置
2. 为关键接口补测试，尤其是登录、下单、验收、纠纷流程
3. 明确支付托管、退款和仲裁规则，避免只停留在页面流程
