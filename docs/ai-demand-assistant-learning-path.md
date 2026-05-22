# AI 需求助手学习与落地说明

这次改动不是单纯接一个聊天接口，而是把 AI 能力真正嵌进了现有业务流程：在“发布需求”页里，用户先写一个粗略想法，再由 AI 帮忙生成结构化需求初稿、预算区间、工期建议和阶段拆分。

## 这次改动对应了哪些招聘要求

1. 大模型 API 接入
   - 后端新增 `AiModelClient`，支持对接 OpenAI 兼容的 `chat completions` 接口。
   - 通过 `app.ai.*` 配置控制 `baseUrl`、`apiKey`、`model`、超时和开关。

2. Prompt 管理
   - 新增 `AiPromptManager`，把系统提示词和业务提示词集中管理，并带 `promptVersion`。
   - 这一步已经具备“Prompt 管理”的基础形态，后续可以继续升级成数据库化或后台可编辑。

3. 上下文处理与 RAG 编排
   - 新增 `AiContextRetrievalService`，会从现有 `knowledge_base`、`resource_post`、`demand_category` 中做轻量检索。
   - 虽然当前不是向量库版本，但已经有“检索层 -> Prompt 层 -> 模型层 -> 结果编排层”的结构，后面替换成 Milvus / pgvector 会很顺。

4. 缓存设计
   - 新增 Redis key：`ai:demand:draft:%s`。
   - 对相同输入的 AI 草稿结果做缓存，减少重复请求和接口成本。
   - 缓存读写失败时做了降级，不阻断主流程。

5. RESTful API 与前后端协作
   - 新增接口：`POST /api/client/ai/demand-draft`
   - 前端在 `ClientDemandCreateView.vue` 增加 `AI 生成初稿` 入口，并自动把返回结果回填到表单。

## 新增的核心代码位置

- 后端 AI 主流程：
  - `backend/src/main/java/com/programmer/escrow/ai/service/AiDemandDraftService.java`
- Prompt 管理：
  - `backend/src/main/java/com/programmer/escrow/ai/service/AiPromptManager.java`
  - `backend/src/main/java/com/programmer/escrow/ai/service/AiPromptTemplateService.java`
- 检索增强：
  - `backend/src/main/java/com/programmer/escrow/ai/service/AiContextRetrievalService.java`
- 模型调用：
  - `backend/src/main/java/com/programmer/escrow/ai/service/AiModelClient.java`
- 回退策略：
  - `backend/src/main/java/com/programmer/escrow/ai/service/AiDemandDraftFallbackService.java`
- 调用日志：
  - `backend/src/main/java/com/programmer/escrow/ai/service/AiCallLogService.java`
- 前端入口：
  - `frontend/src/views/client/ClientDemandCreateView.vue`
  - `frontend/src/api/modules/demand.js`
- 后台配置中心：
  - `frontend/src/views/admin/AdminAiCenterView.vue`
  - `frontend/src/api/modules/admin.js`

## 本地体验方式

1. 如果你还没有配置真实模型，直接使用默认配置也可以体验。
   - 默认 `APP_AI_ENABLED=false`
   - 这时系统会走“规则回退模式”，仍然能生成可用的结构化初稿。

2. 如果你要接真实模型：

```bash
set APP_AI_ENABLED=true
set APP_AI_API_KEY=your_api_key
set APP_AI_BASE_URL=https://api.openai.com
set APP_AI_MODEL=gpt-4.1-mini
```

3. 启动项目后，进入需求发布页，先输入标题/摘要/详情，再点击 `AI 生成初稿`。

4. 如果你要体验后台 Prompt 配置和日志能力：

```bash
mysql -uroot -p programmer_escrow < sql/08_ai_prompt_templates_and_logs.sql
mysql -uroot -p programmer_escrow < sql/09_ai_runtime_config.sql
```

然后进入后台的 `AI 配置` 页面。
现在这个页面除了 Prompt 模板和日志，还可以直接配置：
- provider
- baseUrl
- chatPath
- API Key（后端加密存储，前端只显示掩码）
- model
- temperature
- topK
- fallbackEnabled
- 缓存与超时参数

## 你接下来应该怎么学

建议按下面顺序吃透这块能力：

1. 先读 `AiDemandDraftService`
   - 重点看它怎么把“缓存、检索、Prompt、模型调用、结果兜底”串起来。

2. 再读 `AiContextRetrievalService`
   - 重点理解为什么这就是最基础的 RAG。
   - 你可以把它理解成“当前项目先做词法检索，下一步再换向量检索”。

3. 然后读 `AiPromptManager` 和 `AiPromptTemplateService`
   - 学会把 Prompt 当成“可维护的业务配置”而不是随手拼字符串。
   - 理解为什么 Prompt 模板、模型参数、TopK 要做成后台可调。

4. 再读 `AiCallLogService`
   - 理解为什么落地 AI 一定要有日志、缓存命中和回退记录。
   - 这一步很适合回答“你怎么做线上问题排查和效果优化”。

5. 最后再看前端回填逻辑
   - 理解 AI 输出不是直接展示，而是服务于业务表单的自动补全。

## 面试时怎么讲这个项目

你可以用下面这套表达：

“我在一个 Spring Boot + Vue 的接单平台里做了 AI 需求助手。后端我把能力拆成了检索层、Prompt 层、模型调用层、编排层和日志层。检索层会从现有知识库、资源库、需求分类里做轻量 RAG，把上下文喂给模型，返回结构化需求草稿。为了控制成本和提升并发场景下的体验，我给相同输入做了 Redis 缓存，并设计了无模型时可运行的回退策略。另外我把 Prompt 模板和模型参数做成了后台可配置，并记录每次调用的 Prompt 版本、上下文数、耗时、缓存命中和回退情况，方便后续调优。前端则把 AI 输出直接回填到需求发布表单，形成真实业务闭环。” 

## 下一步最值得继续做的三件事

1. 把当前关键词检索升级成向量检索
   - 可接 Milvus / pgvector
   - 这一步能更贴近岗位 JD 里的“向量检索与结果编排”

2. 在现有后台 Prompt 配置基础上继续做版本管理和灰度发布
   - 增加 Prompt 模板多版本、发布状态、回滚能力
   - 这会让“Prompt 管理”从可配置升级成可发布

3. 在现有调用日志基础上增加 AI 评估机制
   - 记录命中缓存率、模型耗时、用户采纳率
   - 这样你就能把“落地 AI”讲成真正的业务优化，而不是单次 demo
