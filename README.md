# Spring Boot 实战演练——论坛

Spring Boot + Bootstrap

参照[码匠社区](https://github.com/codedrinker/community)

## 本地运行

1. 设置 maven 环境变量（`File -> Settings -> Build -> Build Tools -> Maven -> Runner -> Environment Variables`）

    `JDBC_DATABASE_URL` （url 包含用户名和密码），格式如 `jdbc:postgresql://localhost:5432/postgres?useSSL=false&user=xxx&password=xxx`

2. 设置 Spring Boot 环境变量（`Edit Configurations -> Environment Variables`）

    ```
    JDBC_DATABASE_URL
    GITHUB_CLIENT_ID
    GITHUB_CLIENT_SECRET
    GITHUB_CALLBACK_URL
    ```

3. 设置配置切换（`Edit Configurations -> Spring Boot -> Active profiles`）为 `dev`

    多配置注意不要重复定义，公共部分写在主配置中。

## Heroku 运行

在 Heroku 的配置（`App 管理页面 -> Settings -> Config Vars -> Reveal Config Vars`）中添加：
 
```
GITHUB_CLIENT_ID
GITHUB_CLIENT_SECRET
GITHUB_CALLBACK_URL
```

数据库相关不需额外配置，在 Heroku 应用管理中添加 Heroku Postgres 插件后会自动配置。