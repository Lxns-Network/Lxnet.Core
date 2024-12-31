# LxNet.Core
落雪咖啡屋 跨年活动 2025 小游戏群组联结插件。

# 2025
![](./2025-01-01_00.00.02.png)

# Modules

 - `shared` 所有组件公用代码，包含 RPC 序列化器配置以及调用方法和数据定义
 - `paper-common` 基于 `shared` 面向 Paper API 插件封装的接口，提供玩家弱一致性数据缓存服务以及预配置的 MQ 客户端。
 - `velocity` 实现了 `shared` 中远程方法的逻辑，如 `AddPlayerScoreCall` 等，同时负责多服务器之间的调用传递，如传播 `RaisePlayerCall`。此外附带有一些额外职能，比如修复聊天签名问题。
 - `raise-receiver` 单独的 `RaisePlayerCall` 信号接收者
 - `plugin-support` 针对各个小游戏子服主游戏插件所做的适配工作，此外还有一些定制插件。
   1. `activity` 原本负责发 2025 新年快乐，因故未执行
   2. `bedwars` 负责对接起床战争，添加积分以及成就等
   3. `blockhunt`, `buildbattle`, `parkour` 同上
   4. `menu` 大厅菜单实现。

因为工tian期tian紧mo迫yo，代码写的很糟糕，并不适合作为实现参考。