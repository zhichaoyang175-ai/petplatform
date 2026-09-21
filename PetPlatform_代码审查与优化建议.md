# PetPlatform 代码审查与优化建议

> 审查对象：PetPlatform 后端（Spring Boot 3.2.5 多模块单体）
> 审查维度：代码结构 / 性能 / 可读性 / 安全性 / 可维护性
> 说明：以下每条均基于实际代码核对，标注文件路径与行号；优先级 P0=必须修，P1=建议修，P2=可选优化。

---

## 一、总览

| 维度 | 结论 |
|---|---|
| 代码结构 | 分层总体清晰，但存在 **Controller 直连 Mapper** 的分层穿透 |
| 性能 | 缓存/分布式锁基础扎实，但 **每次认证都查库**、分页无上限 |
| 可读性 | 注释规范、命名清晰，个别硬编码 JSON / 魔法值 |
| 安全性 | 鉴权体系完善，但 **异常 HTTP 状态码恒 200**、JWT 密钥默认值弱 |
| 可维护性 | 白名单**两处重复维护**、无 traceId 链路追踪 |

---

## 二、P0（高优先级，建议尽快修复）

### P0-1　JWT 过滤器被重复注册（双重执行）
- **位置**：`JwtAuthenticationFilter.java:33`（`@Component`）+ `SecurityConfig.java:68`（`addFilterBefore`）
- **问题**：`@Component` 会让 Spring Boot 把它同时注册为**普通 servlet Filter（对所有请求生效）**，而 `SecurityConfig` 又把它加进安全过滤链，导致过滤器在**同一次请求里执行两次**（一次在 Servlet 容器层，一次在 Security 链层）。
- **影响**：认证逻辑重复执行 → 每个认证请求**多一次数据库查询**，且黑名单校验、`SecurityContext` 写入/清理各做一遍，存在状态污染隐患。
- **建议**：二选一——
  1. 去掉 `@Component`，仅由 `SecurityConfig.addFilterBefore` 纳入安全链（推荐，职责单一）；
  2. 保留 `@Component`，但注入 `FilterRegistrationBean` 并 `setEnabled(false)` 禁用容器层自动注册。

### P0-2　全局异常响应的 HTTP 状态码恒为 200
- **位置**：`GlobalExceptionHandler.java`（全部 `@ExceptionHandler` 均返回 `R<Void>`，未标注 `@ResponseStatus` 也未返回 `ResponseEntity`）
- **问题**：无论 400/401/403/500，HTTP 层都返回 **200 OK**，只有响应体里的 `code` 字段区分。
- **影响**：网关 / Nginx / 监控（如 Prometheus 状态码指标）无法据 HTTP 状态码感知错误；前端与爬虫的缓存策略也会被误导；面试常被追问「错误码为什么和 HTTP 状态码脱节」。
- **建议**：为各异常方法加 `@ResponseStatus`，或返回 `ResponseEntity<R<Void>>` 显式设置状态码（400 / 401 / 403 / 500），与 `SecurityConfig` 中 401/403 的 `response.setStatus` 保持一致。

---

## 三、P1（中优先级，建议排期修复）

### P1-1　每次认证请求都实时查库加载用户
- **位置**：`JwtAuthenticationFilter.java:92`（`userMapper.selectOne(...)`）
- **问题**：每个携带 JWT 的请求都同步查一次 MySQL 加载用户（校验状态、角色）。
- **建议**：用户信息写入 Redis 短期缓存（如 5~10 分钟，TTL 略短于 Access Token），命中直接取缓存，失效回源 DB；用户禁用/改角色时主动删缓存。可复用现有 `CacheService`。

### P1-2　Controller 直连 Mapper，穿透 Service 层
- **位置**：`PetController.java:39-41`（注入 `PetMapper` / `AdoptionRecordMapper` / `UserMapper`）、`PetController.java:123-133`（`getStats` 直接在 Controller 拼查询）
- **问题**：破坏了「Controller → Service → Repository/Mapper」的严格分层，统计逻辑散落在 Controller，事务与业务约束无法统一管控。
- **建议**：把统计聚合下沉到 `PetService`（或新建 `StatsService`），Controller 只做参数接收与结果包装。

### P1-3　认证白名单两处重复维护
- **位置**：`JwtAuthenticationFilter.java:38-46`（`SKIP_PATHS`）与 `SecurityConfig.java:47-54`（`permitAll`）
- **问题**：两处白名单语义接近但**不完全一致**（如 `JwtAuthenticationFilter` 里 `/api/v1/pets/stats` 放行、`SecurityConfig` 里又单独 `permitAll`），易漏改、易漂移——此前「POST /api/v1/pets 返 401」的 P0 bug 根源正是 AntPathMatcher 未区分 HTTP 方法。
- **建议**：白名单收敛到**单一配置源**（如 `@ConfigurationProperties` 的 `app.security.whitelist`），JWT 过滤器与 Security 链共用同一份；匹配时明确 HTTP 方法。

### P1-4　分页参数无上限，存在慢查询风险
- **位置**：`PetController.java:47`（`searchPets`）、`PetController.java:113`（`getMyPets` 的 `size`）
- **问题**：`size` 未做上限校验，恶意/误用可传超大值（如 `size=100000`），触发全表扫描与内存占用。
- **建议**：统一在 DTO 校验层（`@Max`）或 `PageDTO` 上限制 `size ∈ [1, 100]`，超限自动裁剪为上限。

### P1-5　日志实现用了 StdOut，且无 traceId
- **位置**：`application-dev.yml:60`（`mybatis-plus.configuration.log-impl: StdOutImpl`）；全项目无 `logback-spring.xml`、无 traceId/MDC
- **问题**：SQL 日志走标准输出，无法与文件日志统一采集；无 traceId 时，高并发下无法把一次请求的多行日志串起来定位问题。
- **建议**：`log-impl` 改为 `org.apache.ibatis.logging.slf4j.Slf4jImpl`；新增 `logback-spring.xml`，配合「请求链路追踪中间件（traceId + MDC）」把 traceId 打进每行日志（可作为下一步中间件，与本次限流中间件形成可观测性闭环）。

---

## 四、P2（低优先级，可选优化）

### P2-1　SecurityConfig 内硬编码 JSON 字符串
- **位置**：`SecurityConfig.java:72-83`（401/403 直接 `write("{\"code\":401,...}")`）
- **问题**：魔法字符串可读性差、易拼错，且与 `R` 类脱节。
- **建议**：复用 `R.fail(...)` + Jackson `ObjectMapper.writeValueAsString` 输出，或抽 `JsonUtil`。

### P2-2　JWT 密钥默认值过弱
- **位置**：`application-dev.yml:70`（`secret: replace-with-a-long-random-base64-secret`）
- **问题**：若部署时忘记覆盖环境变量，攻击者可用默认密钥伪造任意身份 Token。
- **建议**：启动时校验密钥非默认值（`@PostConstruct` 断言），或改用更显眼的占位并文档强提醒。

### P2-3　`CacheService` 的降级开关状态不恢复
- **位置**：`CacheService.java:117-122`（`redisAvailable` 一旦置 `false` 不再回置）
- **问题**：Redis 短暂抖动后恢复，但 `redisAvailable` 仍为 `false`，导致后续告警缺失（功能不受影响，仅日志降级提示不准确）。
- **建议**：`redisAvailable` 用带 TTL 的重试或「连续失败 N 次才告警」策略。

### P2-4　限流维度可继续扩展（与本次新增中间件衔接）
- 本次新增的 `@RateLimit` 已覆盖 IP / USER / INTERFACE 三维度；后续可扩展**滑动窗口**（ZSET，规避固定窗口「临界突刺」）或**令牌桶**，应对更高 QPS 场景。

---

## 五、本次已落地的优化

本次已在项目中**新增分布式接口限流中间件**（详见下方「中间件实现说明」），直接回应了 P0-2 之外的部分安全防护诉求：

- `@RateLimit` 注解 + `RateLimitType` 维度枚举（`common` 模块）
- `RateLimitAspect` AOP 切面 + Redis Lua 原子限流（`infrastructure` 模块）
- `RedisKeyConstant.RATE_LIMIT_PREFIX` 常量
- `application-dev.yml` 增加 `app.rate-limit.enabled` 全局开关
- 示例落地：`AuthController.sendCode`（IP 维度，防短信轰炸）、`PetController.create`（USER 维度，防刷发布）

---

## 六、中间件实现说明（新增）

### 6.1 设计用途
为后端接口提供**声明式、分布式、可降级**的访问频率控制，防止接口被恶意刷取（短信轰炸、批量发布、爬虫）击穿后端，与已有的「Redisson 防缓存击穿」「ShedLock 定时任务幂等」共同构成高并发防护体系。

### 6.2 职责边界
- **做什么**：方法级限流、多维 key 隔离、原子计数、超限快速失败、Redis 故障时 fail-open。
- **不做什么**：不负责鉴权（归 Spring Security）、不负责缓存（归 CacheService/Redisson）、不负责业务校验（归 Service）。限流是**横切防护**，与业务逻辑解耦。

### 6.3 实现方式
1. **注解层** `@RateLimit`：`type`（维度）、`limit`（阈值）、`window`（窗口秒）、`key`（自定义前缀）、`message`（提示）。
2. **切面层** `RateLimitAspect`：`@Around("@annotation(rateLimit)")` 拦截，解析维度 key。
3. **原子计数层**：Redis Lua 脚本 `INCR + 首次 EXPIRE + TTL 兜底`，保证「自增 + 过期」原子，规避并发竞态。
4. **降级层**：Redis 异常 catch 后 fail-open 放行，限流组件不影响核心链路；`app.rate-limit.enabled=false` 可全局关闭。
