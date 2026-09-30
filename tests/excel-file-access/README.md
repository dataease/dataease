# Excel 本地文件访问回归（TAPD 1076980）

上传文件按当前登录用户 ID 存放；新建配置只能引用自己的上传文件。修改已有数据源须有管理权限，允许保留服务端原配置中已绑定的文件。实际读取时再次检查上传目录、UUID 文件名、扩展名、真实路径及符号链接。

旧版本上传目录中的 UUID 文件可以被原数据源继续使用；新建数据源不能认领历史文件。目录外、非上传文件或符号链接等非法历史配置需要重新上传。

## 自动回归

在仓库根目录、Java 21 环境执行（SDK 等依赖须已安装）：

```sh
mvn -q -f core/core-backend/pom.xml compile dependency:build-classpath -Pstandalone -Dmaven.test.skip=true -Dmdep.outputFile=/tmp/excel-file-access-classpath
mkdir -p /tmp/excel-file-access-test
excel_test_cp="sdk/common/target/classes:sdk/extensions/extensions-datasource/target/classes:sdk/api/api-base/target/classes:core/core-backend/target/classes:$(cat /tmp/excel-file-access-classpath)"
javac -proc:none -cp "$excel_test_cp" -d /tmp/excel-file-access-test tests/excel-file-access/Regression.java
java -cp "/tmp/excel-file-access-test:$excel_test_cp" Regression
```

20 项断言覆盖合法上传、跨用户引用、存量文件引用、目录穿越、绝对/相对路径、相似目录前缀、非法文件名/扩展名、文件及目录符号链接，并调用实际 CSV 读取入口。测试只创建临时合成文件，结束后清理。不依赖 Surefire。

## 本地接口验收

通过本地前端的现有签名请求封装调用实际接口：

- 上传合成 CSV，返回当前用户目录下的 UUID 路径；新建数据源成功。
- 新建和更新配置分别替换为目录外合成 CSV、其他用户目录下合成 CSV，四项均返回业务错误 40001，提示重新上传文件。
- 原数据源追加新上传 CSV 后预览包含原行和追加行；替换后仅保留新行。
- 保留测试数据源 `1076980-Excel文件访问校验`，ID `1302630822871961600`，默认组织，最终预览一行 `beta,42`。

权限方法沿用 `CorePermissionManage` 管理权限契约。自动回归只验证文件规则；尚未覆盖多用户、多组织的完整 HTTP 权限矩阵及 Desktop 模式。
