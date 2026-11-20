# 数据集语义回归

从仓库根目录执行 `python3 tests/dataset-semantics/run.py`。

使用已有 CoreApplication.jar、本地 Maven Lombok/H2 依赖，定向编译当前实体、DTO、Manage、校验与迁移源码。仅连接新建的 H2 内存数据库，不启动产品、不下载依赖、不访问用户数据库。

覆盖旧表补列、迁移重复执行、表/字段语义独立存储与重新读取、显式清空值参考和可选业务含义。未覆盖真实 MySQL/PostgreSQL 数据库及生产配置。

浏览器用例见 `tests/browser/specs/dataset-editor-steps.spec.cjs` 中的 semantics 用例，使用真实编辑组件和合成 API，覆盖抽屉编辑、保存前草稿、保存重开，以及新增源语义复制和历史字段隔离。
