# PetPlatform RocketMQ 接入说明

> 本次接入用 RocketMQ 把「审核通过的重副作用」与「站内通知」从同步链路解耦为异步可靠投递，
> 与已有的「Redis 缓存 + Redisson 防击穿 + 分布式限流 + ShedLock 幂等」共同构成完整的高并发防护体系。

---

## 一、方案概览（两个落点）

| 落点 | 消息流 | Topic | 消费者 |
|---|---|---|---|
| A：审核副作用异步化 | 审核通过 → 发消息 → 异步生成 12 期回访计划 | `adoption-followup-topic` | `AdoptionFollowUpConsumer` |
| B：通知可靠投递 | 申请提交/通过/驳回 → 发消息 → 异步写站内通知 | `notification-topic` | `NotificationConsumer` |

**关键设计边界（面试重点）**：领养记录、宠物状态、申请状态、拒绝其他申请这些**核心事实数据**必须与审核事务强一致（不可异步，否则会出现"宠物已领养却无记录"）；真正能异步化的是**重批量插入的 12 期回访计划**（最早 1 个月后才用到，延迟几秒生成无感）和**非核心的通知**。

## 二、消息流转

```
审核通过（@Transactional 内，同步）              事务提交后（AFTER_COMMIT）
┌─────────────────────────────────────┐     ┌──────────────────────────────────┐
│ 申请→APPROVED / 宠物→已领养          │     │ 领域事件 ApplicationApprovedEvent │
│ 生成领养记录 / 拒绝其他申请           │ ──► │ 监听器 AdoptionNotificationListener │
└─────────────────────────────────────┘     │   ├─ 发 回访计划消息 ──► MQ ──► 消费者异步建 12 期
                                            │   └─ 发 通知消息 ────► MQ ──► 消费者可靠写通知表
                                            └──────────────────────────────────┘
```

## 三、关键设计点

1. **事务一致性**：用 `@TransactionalEventListener(AFTER_COMMIT)` 在事务提交后才发消息，规避「消息已发但事务回滚」的数据不一致。
2. **幂等消费**：
   - 回访计划：消费前 `hasFollowUpPlan` 判断是否已生成，且生成逻辑 `@Transactional` 保证 12 期原子（失败回滚 → MQ 重试）。
   - 通知：以「refType + refId + type」为业务唯一键，Redis SETNX 去重（TTL 24h）。
3. **优雅降级**：`app.rocketmq.enabled` 默认 `false`，MQ 未启用时监听器自动回退为同步执行（通知直接写库、回访计划同步生成），应用无 MQ 也能正常启动运行。
4. **fail-open**：Redis 去重异常时视为「首次」照常处理，宁可重复也不丢消息。

## 四、本地启动 RocketMQ（Docker，4.9.4）

```bash
# 1. 准备 broker 配置（关键：brokerIP1 指向宿主机，autoCreateTopicEnable 自动建 Topic）
cat > /tmp/broker.conf <<'EOF'
brokerClusterName = DefaultCluster
brokerName = broker-a
brokerId = 0
brokerRole = ASYNC_MASTER
flushDiskType = ASYNC_FLUSH
brokerIP1 = 127.0.0.1
autoCreateTopicEnable = true
EOF

# 2. 启动 NameServer
docker run -d --name rmqnamesrv -p 9876:9876 apache/rocketmq:4.9.4 sh mqnamesrv

# 3. 启动 Broker（挂载 broker.conf + 指定 NameServer 地址）
docker run -d --name rmqbroker \
  -p 10911:10911 -p 10909:10909 \
  -v /tmp/broker.conf:/home/rocketmq/broker.conf \
  -e "NAMESRV_ADDR=127.0.0.1:9876" \
  apache/rocketmq:4.9.4 sh mqbroker -c /home/rocketmq/broker.conf
```

## 五、启用方式

启动 RocketMQ 后，设置环境变量重启应用即可：

```bash
ROCKETMQ_ENABLED=true
# 可选：ROCKETMQ_NAME_SERVER=localhost:9876
```

配置项见 `application-dev.yml`：
- `app.rocketmq.enabled`：MQ 总开关（默认 false）
- `rocketmq.name-server`：NameServer 地址
- `rocketmq.producer.group`：生产者组

## 六、代码文件清单

**新增（common 模块）**
- `common/mq/AdoptionApprovedMessage.java`（回访计划消息体）
- `common/mq/NotificationMessage.java`（通知消息体）
- `common/constant/MqTopicConstant.java`（Topic 常量）

**新增（service 模块）**
- `service/mq/MqProducerService.java`（生产者，@ConditionalOnProperty）
- `service/mq/AdoptionFollowUpConsumer.java`（回访计划消费者）
- `service/mq/NotificationConsumer.java`（通知消费者）

**修改**
- `service/state/AdoptionStateMachine.java`：移除同步 `generateFollowUpPlan`，事件携带回访计划字段
- `service/state/event/ApplicationApprovedEvent.java`：扩展 record 字段
- `service/listener/AdoptionNotificationListener.java`：改造为「事件 → MQ」桥接 + 降级
- `service/FollowUpService` + `FollowUpServiceImpl`：`generateFollowUpPlan` 加 @Transactional，新增 `hasFollowUpPlan`
- `common/constant/RedisKeyConstant.java`：新增 MQ 幂等去重前缀
- `infrastructure/cache/CacheService.java`：新增 `setIfAbsent`（fail-open）
- `pom.xml` / `pet-platform-service/pom.xml`：引入 `rocketmq-spring-boot-starter 2.3.1`
- `application-dev.yml`：MQ 开关与配置

## 七、面试话术（MQ 三大核心问题）

1. **消息不丢**：生产端发消息在事务提交后（AFTER_COMMIT），Broker 持久化刷盘，消费端 ACK 成功才确认——链路各环节都有兜底。
2. **消息不重复**：消费端做幂等——回访计划靠 `hasFollowUpPlan` 判断 + 事务原子，通知靠 Redis SETNX 去重。
3. **消息不乱序**：RocketMQ 支持顺序消息（同一业务 key 发到同一队列）；本项目回访计划/通知间无顺序依赖，故用并发消费即可，顺序场景按 key 做队列级顺序。

**可能被追问**：单体为什么上 MQ？—— 核心不是「微服务通信」，而是「异步解耦 + 削峰 + 可靠消息（进程内事件在宕机时会丢）」，把审核接口的响应时间从「同步建 12 期回访」中解放出来，且消息持久化保证副作用不丢。
