# AI 智慧供应链管理平台

一个面向集团、工厂和商家的供应链数字化 Demo。项目围绕集团人员、门店与配送履约构建业务闭环，并接入 DeepSeek、LangChain4j、Milvus 和 MCP，展示 Java 应用中 RAG 检索增强、流式对话和业务工具调用的完整实现。

## 项目亮点

- **供应链业务闭环**：集团维护人员与门店，门店查看配送进度并发起催单，配送中心完成配送员、任务和履约状态管理。
- **AI 智能客服**：支持多轮对话、SSE 流式输出、Markdown 渲染与会话上下文管理。
- **RAG 知识库**：内置农产品采购、仓储、冷链、质量与售后资料，文档切分后写入 Milvus，并在回答时召回相关知识。
- **MCP 工具调用**：基于 Streamable HTTP 与 JSON-RPC 2.0 暴露业务工具，AI 可查询门店、配送单和经营概览。
- **工程化分层**：后端采用 Maven 多模块结构，领域对象、公共能力和应用服务边界清晰；敏感配置通过环境变量注入。

## 核心功能

| 模块 | 功能说明 |
| --- | --- |
| 数据看板 | 展示集团门店、人员、配送任务、履约状态等核心指标 |
| 集团管理 | 人员档案维护、账号状态管理、分页检索与权限登录 |
| 门店管理 | 门店信息维护、门店 ID 关联、配送任务查看与一键催单 |
| 配送中心 | 配送员管理、任务分配、状态流转、催单记录与语音播报 |
| 智能客服 | DeepSeek 对话、流式响应、Markdown 展示和连续会话 |
| 企业知识库 | Markdown 文档入库、向量检索、知识召回和索引刷新 |
| MCP 服务 | 门店查询、配送查询、经营统计等标准化业务工具 |

## 技术栈

### 后端与数据

- Java 17、Spring Boot 2.7、Maven 多模块
- MyBatis、PageHelper、Druid、MySQL 8
- JWT、Spring MVC、WebSocket、SSE
- Knife4j / Swagger、Lombok

### AI 能力

- LangChain4j：模型编排、对话记忆、Embedding 与 RAG
- DeepSeek OpenAI-Compatible API：智能客服大模型
- Milvus：农产品知识库向量存储与相似度检索
- MCP Streamable HTTP：基于 JSON-RPC 2.0 的业务工具协议
- BGE Small ZH：中文知识向量化

### 前端与部署

- Vue 2.6、TypeScript、Vue Router、Vuex
- Element UI、Axios、ECharts、Sass
- Web Speech API：浏览器端催单语音播报
- Nginx：静态资源托管与接口反向代理（可选）
- Docker：本地运行 Milvus 等基础设施

## 系统架构

```text
┌──────────────── Vue 管理端 ────────────────┐
│ 看板 / 集团 / 门店 / 配送中心 / 智能客服   │
└──────────── REST · SSE · WebSocket ─────────┘
                         │
┌────────────── Spring Boot 应用 ─────────────┐
│ Controller → Service → Mapper → MySQL       │
│                    │                         │
│     LangChain4j → DeepSeek                   │
│          ├────→ Milvus RAG 知识库            │
│          └────→ MCP 业务工具                 │
└──────────────────────────────────────────────┘
```

## 项目结构

```text
Supply-chain/
├─ backend/
│  ├─ chain-common/       # 通用配置、鉴权、异常与工具类
│  ├─ chain-domain/       # DTO、Entity、VO 等领域模型
│  ├─ chain-service/      # Web 接口、业务服务、AI/RAG/MCP
│  └─ sql/                # 初始化与演示数据脚本
├─ frontend/              # Vue + TypeScript 管理端
├─ gateway/               # 可选 Nginx 网关与静态部署配置
├─ knowledge/             # 农产品供应链 Markdown 知识资料
└─ PROJECT_ROADMAP.md     # 产品边界与后续演进路线
```

## 本地启动

### 1. 环境准备

- JDK 17+
- Maven 3.8+
- Node.js 16/18 与 npm
- MySQL 8
- Docker 与 Milvus（启用向量知识库时需要）

创建数据库 `supply_chain_demo`，然后执行 `backend/sql/ai_supply_chain_demo.sql`。如需沿用其他数据库名，可通过 `MYSQL_DATABASE` 环境变量覆盖。

### 2. 配置环境变量

```powershell
$env:MYSQL_DATABASE="supply_chain_demo"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="your-password"
$env:DEEPSEEK_API_KEY="your-deepseek-api-key"
$env:MILVUS_URI="http://localhost:19530"
```

API Key 不应提交到 Git 仓库。

### 3. 启动后端

```powershell
cd backend
mvn clean package -DskipTests
java -Dfile.encoding=UTF-8 -jar chain-service/target/chain-service-1.0-SNAPSHOT.jar
```

后端默认地址：`http://localhost:8080`

### 4. 启动前端

```powershell
cd frontend
npm install
npm run serve
```

访问：`http://localhost:8888`  
演示账号：`admin` / `123456`

## AI 与知识库

项目启动后读取 `knowledge/` 下的农产品资料，完成文本切分、Embedding 和 Milvus 索引构建。智能客服先检索知识片段，再结合 MCP 业务工具生成回答；前端通过 SSE 接收增量内容并渲染 Markdown。

可演示的问题示例：

- “叶菜类到货后应该如何验收和保存？”
- “查询门店 1 当前有哪些待配送任务。”
- “汇总一下今天的配送履约情况。”
- “这个配送单超时了，应该如何处理？”

## MCP 接口

MCP 服务入口为 `POST /mcp`，支持初始化、工具列表和工具调用。当前工具覆盖门店信息、配送任务、催单状态和经营概览，便于展示模型从自然语言理解到真实业务查询的完整链路。

## 演示建议

1. 登录后先展示数据看板和集团人员管理。
2. 在门店页面定位配送任务并发起催单，观察页面反馈与语音播报。
3. 进入配送中心演示配送员与任务状态流转。
4. 在智能客服中先提知识类问题，再提门店或配送查询问题，对比 RAG 与 MCP 两类能力。

## 项目定位

本项目以可运行、可讲解、可扩展为目标，适合作为 Java 后端、全栈开发或 AI 应用工程方向项目。当前重点是供应链履约与企业智能助手，不包含复杂财务结算、真实支付和生产级多租户治理。
