# Spring Boot 实战演练——论坛

Spring Boot + Bootstrap + Thymeleaf + MyBatis + PostgreSQL + Redis

Redis 的配置还没加到 heroku 中，需要在 heroku 中添加 redis 插件。本 repo 的 heroku branch 为不包含 redis 的版本。

参照[码匠社区](https://github.com/codedrinker/community)

## 本地运行（IntelliJ IDEA）

1. 设置 maven 环境变量

    File -> Settings -> Build, Execution, Deployment -> Build Tools -> Maven -> Runner -> Environment Variables，设置：

    `JDBC_DATABASE_URL` ，格式如 `jdbc:postgresql://localhost:5432/postgres?useSSL=false&user=xxx&password=xxx`

2. 设置 Spring Boot 环境变量

    运行按钮左边下拉框 -> Edit Configurations -> Environment Variables，设置：

    ```
    JDBC_DATABASE_URL
    GITHUB_CLIENT_ID
    GITHUB_CLIENT_SECRET
    GITHUB_CALLBACK_URL
    ```

3. 设置配置切换

    运行按钮左边下拉框 -> Edit Configurations -> Spring Boot -> Active profiles，设置为 `dev`

    多配置注意不要重复定义，公共部分写在主配置中。

## Heroku 运行

在 Heroku 的配置（App 管理页面 -> Settings -> Config Vars -> Reveal Config Vars）中添加：

```
GITHUB_CLIENT_ID
GITHUB_CLIENT_SECRET
GITHUB_CALLBACK_URL
```

数据库相关不需额外配置，在 Heroku 应用管理中添加 Heroku Postgres 插件后会自动配置。
