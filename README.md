# PetPlatform · 宠物领养信息平台

一个宠物领养信息服务平台：送养/领养匹配、领养申请审核工作流、领养后跟踪回访、社区动态/日记等模块。
后端基于 **Spring Boot 3.2 + MyBatis-Plus**，前端基于 **Vue 3 + Vite**。

> ⚠️ 本项目源码与配置中**不包含任何真实云厂商密钥 / JWT 密钥**。所有敏感配置均通过环境变量注入，详见下方「环境变量」一节。

---

## 功能模块

| 模块 | 说明 |
|------|------|
| 用户与鉴权 | 手机号+密码登录、注册、JWT 鉴权；角色：领养人(1) / 送养人(2) / 管理员(3) / 审核员(4) |
| 宠物 | 宠物发布、检索、详情 |
| 领养申请 | 提交 → 审核（状态机）→ 通过/驳回 → 生成领养记录 |
| 管理后台 | KPI 统计、用户/宠物管理、申请审核（路由 `/admin`，仪表盘「申请审核」可进入详情 `/admin/review/:id`） |
| 社区 | 领养日记 / 动态（`t_feed`） |
| 文件上传 | 本地存储（默认）或阿里云 OSS（可切换） |

---

## 技术栈

- **后端**：Spring Boot 3.2.5、Spring Security、MyBatis-Plus、Flyway、Redis、JWT、x-file-storage（本地/OSS）、SpringDoc
- **前端**：Vue 3、Vite、Vue Router、Axios
- **数据库**：MySQL 8.0
- **构建**：Maven（多模块）、Node.js 18+

## 项目结构（Maven 多模块）

```
pet-platform-common        # 公共 DTO / 枚举 / 异常 / 常量（最底层，不依赖其他模块）
pet-platform-repository    # 实体 + Mapper + Flyway 迁移脚本
pet-platform-infrastructure # 安全(JWT) / 缓存 / 存储 / 通用配置
pet-platform-service       # 业务逻辑（认证、审核状态机、KPI 统计等）
pet-platform-api           # Controller + 启动类
frontend-vue               # 前端单页应用
```

---

## 环境要求

- JDK 17
- Maven 3.9+
- Node.js 18+
- MySQL 8.0（库名 `pet_platform`）
- Redis 7+

---

## 快速开始

### 1. 后端

```bash
# 1) 准备 MySQL（建库 pet_platform，Flyway 会自动建表并灌入种子数据）与 Redis
mysql -uroot -e "CREATE DATABASE IF NOT EXISTS pet_platform DEFAULT CHARSET utf8mb4;"

# 2) （可选）设置环境变量，不设置则使用本地默认（local 存储 + 占位 JWT secret，仅限开发）
#    详见下方「环境变量」

# 3) 启动（使用 Maven Wrapper）
./mvnw -pl pet-platform-api -am spring-boot:run
#   或本地已装 maven：mvn -pl pet-platform-api -am spring-boot:run
```

- 接口文档：http://localhost:8080/swagger-ui.html
- 默认端口：`8080`

### 2. 前端

```bash
cd frontend-vue
npm install
npm run dev
```

访问 http://localhost:5173

---

## 环境变量（重要：密钥切勿写进仓库）

仓库内的 `pet-platform-api/src/main/resources/application-dev.yml` 已使用占位符，**不含任何真实密钥**。
本地开发请通过环境变量注入：

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `JWT_SECRET` | JWT 签名密钥（请换成足够长的随机串） | 占位串（仅开发可用） |
| `FILE_STORAGE_TYPE` | 文件存储类型：`local` / `oss` | `local` |
| `OSS_ENABLE` | 是否启用阿里云 OSS | `false` |
| `OSS_ACCESS_KEY` | 阿里云 AccessKeyId | （空） |
| `OSS_SECRET_KEY` | 阿里云 AccessKeySecret | （空） |
| `OSS_ENDPOINT` | OSS endpoint | `oss-cn-hangzhou.aliyuncs.com` |
| `OSS_BUCKET` | OSS bucket 名称 | `your-bucket-name` |
| `OSS_DOMAIN` | OSS 访问域名 | （空） |

**Linux / macOS 示例**

```bash
export JWT_SECRET="$(openssl rand -base64 48)"
export OSS_ENABLE=true
export OSS_ACCESS_KEY="你的AccessKeyId"
export OSS_SECRET_KEY="你的AccessKeySecret"
export OSS_BUCKET="your-bucket"
```

**Windows PowerShell 示例**

```powershell
$env:JWT_SECRET = "一段足够长的随机字符串"
$env:OSS_ENABLE = "true"
$env:OSS_ACCESS_KEY = "你的AccessKeyId"
$env:OSS_SECRET_KEY = "你的AccessKeySecret"
```

> 默认 `FILE_STORAGE_TYPE=local` 时，上传文件保存到本地 `./uploads`，**无需任何云密钥即可完整运行**。
> 只有当你把 `OSS_ENABLE=true` 并提供 `OSS_ACCESS_KEY` / `OSS_SECRET_KEY` 时才会启用云端存储。

---

## 默认开发账号（种子数据）

| 手机号 | 密码 | 角色 |
|--------|------|------|
| `13800000001` | `123456` | 送养人 |
| `13800000002` | `123456` | 领养人 |
| `13800000003` | `123456` | 管理员（ADMIN） |

> 种子用户密码均为 `BCrypt("123456")`，仅供本地开发。生产环境请更换强密码或关闭种子数据。

登录入口（前端）：`/login`，使用手机号 + 密码。管理员登录后可进入 `/admin` 管理后台。

---

## 数据库初始化

应用启动时 Flyway 自动执行 `pet-platform-api/src/main/resources/db/migration/` 下的迁移脚本：

- `V1__init_schema.sql` —— 建表（用户、宠物、领养申请、领养记录、回访任务、feed 等）
- `V2__seed_data.sql` —— 种子用户 / 宠物 / 申请
- `V10__feed_seed.sql` —— 领养日记 / 社区动态种子数据

如已初始化过数据库且需重置：删除 `pet_platform` 库后重启，或执行 `mvn flyway:clean flyway:migrate`（开发环境）。

---

## 构建产物说明

以下文件已被 `.gitignore` 排除，**不会**进入仓库：

- 编译产物：`**/target/`、`frontend-vue/dist/`
- 依赖：`frontend-vue/node_modules/`
- 运行时产物：`logs/`、`uploads/`
- 本地密钥：`.env`、`.env.*`
- 与代码无关的临时/备份文件（如简历、编译输出、`sql/` 冗余备份等）

---

## 许可

本项目仅供学习 / 演示使用。
