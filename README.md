![license](https://img.shields.io/github/license/dataease/dataease?color=%231890FF&style=flat-square)
![release](https://img.shields.io/github/v/release/dataease/dataease?color=%231890FF&sort=semver&style=flat-square)
![stars](https://img.shields.io/github/stars/dataease/dataease?color=%231890FF&style=flat-square)
![downloads](https://img.shields.io/github/downloads/dataease/dataease/total)

# DataEase - 人人可用的开源数据可视化分析工具

DataEase 是开源的数据可视化分析工具，帮助用户快速分析数据并洞察业务趋势，从而实现业务的改进与优化。DataEase 支持丰富的数据源连接，能够通过拖拉拽方式快速制作图表，并可以方便的与他人分享。

- 图表展示: 支持 PC 端、移动端及大屏;
- 图表制作: 支持丰富的图表类型(基于 Apache ECharts 实现)、支持拖拉拽方式快速制作仪表板;
- 数据引擎: 支持直连模式、本地模式(基于 Apache Doris / Kettle 实现);
- 数据连接: 支持关系型数据库、Excel 等文件、Hadoop 等大数据平台、NoSQL 等各种数据源。

## UI 展示

![de-ui](https://dataease.oss-cn-hangzhou.aliyuncs.com/img/de-ui.png)

## 在线体验

-   环境地址：<https://demo.dataease.io/>
-   用户名：demo
-   密码：P@ssw0rd123..

## 快速开始

仅需两步快速安装 DataEase：

1.  准备一台不小于 16 G内存的 64位 Linux 主机；
2.  以 root 用户执行如下命令一键安装 DataEase。

```sh
curl -sSL https://github.com/dataease/dataease/releases/latest/download/quick_start.sh | sh
```

- [在线文档](https://dataease.io/docs/)
- [演示视频](https://dataease.oss-cn-hangzhou.aliyuncs.com/video/de-v1-demo.mp4)

## 微信群

![wechat-group](https://dataease.oss-cn-hangzhou.aliyuncs.com/img/wechat-group.png)

## 功能架构

![de-architecture](https://dataease.oss-cn-hangzhou.aliyuncs.com/img/de-architecture.png)

## 产品优势

- 开源开放：零门槛，线上快速获取和安装；快速获取用户反馈、按月发布新版本；
- 简单易用：极易上手，通过鼠标点击和拖拽即可完成分析；
- 秒级响应：集成 Apache Doris，超大数据量下秒级查询返回延时；
- 安全分享：支持多种数据分享方式，确保数据安全。

## 功能列表

<table>
   <tr>
      <td bgcolor="#3779d9" align="middle" style="font-weight:bold;color: white;width: 150px">
         功能模块
      </td>
      <td bgcolor="#3779d9" align="middle" style="font-weight:bold;color: white;width: 170px">
         功能
      </td>
      <td bgcolor="#3779d9" align="middle" style="font-weight:bold;color: white;width: 750px">
         功能描述
      </td>
   </tr>
   <tr>
      <td rowspan="19">
         仪表板
      </td>
      <td rowspan="7">
         仪表板管理
      </td>
      <td>
         支持新建仪表板
      </td>
   </tr>
   <tr>
      <td>
         支持重命名仪表板
      </td>
   </tr>
   <tr>
      <td>
         支持删除仪表板
      </td>
   </tr>
   <tr>
      <td>
         支持在线编辑仪表板
      </td>
   </tr>
   <tr>
      <td>
         支持以树状形式展示仪表板分组
      </td>
   </tr>
   <tr>
      <td>
         支持重命名仪表板分组
      </td>
   </tr>
   <tr>
      <td>
         支持删除仪表板分组
      </td>
   </tr>
   <tr>
      <td rowspan="10">
         仪表板制作
      </td>
      <td>
         支持多种组件，视图/时间组件/文本组件/数字组件/样式组件/图片等
      </td>
   </tr>
   <tr>
      <td>
         支持组件样式设置，如图形属性、组件样式等
      </td>
   </tr>
   <tr>
      <td>
         支持仪表板背景、以及组件间隙设置等
      </td>
   </tr>
   <tr>
      <td>
         支持一键切换仪表板主题
      </td>
   </tr>
   <tr>
      <td>
         支持调整仪表板画布大小
      </td>
   </tr>
   <tr>
      <td>
         支持撤销、重做、清空画布内容
      </td>
   </tr>
   <tr>
      <td>
         支持仪表板的全屏预览
      </td>
   </tr>
   <tr>
      <td>
         支持仪表板模板的导出
      </td>
   </tr>
   <tr>
      <td>
         支持默认仪表板的设置
      </td>
   </tr>
   <tr>
      <td>
         支持仪表板的收藏
      </td>
   </tr>
   <tr>
      <td rowspan="2">
         仪表板共享
      </td>
      <td>
         支持按组织/角色/用户分享
      </td>
   </tr>
   <tr>
      <td>
         支持生成分享链接，外部用户可通过密码访问
      </td>
   </tr>
   <tr>
      <td rowspan="14">
         视图
      </td>
      <td rowspan="6">
         视图管理
      </td>
      <td>
         支持新增视图
      </td>
   </tr>
   <tr>
      <td>
         支持编辑视图
      </td>
   </tr>
   <tr>
      <td>
         支持删除视图
      </td>
   </tr>
   <tr>
      <td>
         支持对视图进行分组
      </td>
   </tr>
   <tr>
      <td>
         支持重命名视图分组
      </td>
   </tr>
   <tr>
      <td>
         支持删除视图分组
      </td>
   </tr>
   <tr>
      <td rowspan="8">
         视图制作
      </td>
      <td>
         支持通过简单的拖拉操作，制作视图
      </td>
   </tr>
   <tr>
      <td>
         支持多种图表类型，明细表/指标卡/基础柱状图/堆叠柱状图/横向柱状图/横向堆叠柱状图/基础折线图/堆叠折线图/饼图/南丁格尔玫瑰图/漏斗图/雷达图/仪表盘等
      </td>
   </tr>
   <tr>
      <td>
         支持选择图表的样式优先级
      </td>
   </tr>
   <tr>
      <td>
         支持选择图表的排序方式，根据维度、指标升序、降序展示
      </td>
   </tr>
   <tr>
      <td>
         支持指标的多种汇总计算方式，如求和、平均、最大值、最小值等
      </td>
   </tr>
   <tr>
      <td>
         支持对图表类型的图形属性进行设置
      </td>
   </tr>
   <tr>
      <td>
         支持对图表类型的组件样式进行设置
      </td>
   </tr>
   <tr>
      <td>
         支持通过过滤条件筛选视图数据
      </td>
   </tr>
   <tr>
      <td rowspan="8">
         数据集
      </td>
      <td rowspan="8">
         数据集管理
      </td>
      <td>
         支持多种类型的数据集，数据库数据集/SQL 数据集/Excel 数据集/自定义数据集
      </td>
   </tr>
   <tr>
      <td>
         数据库数据集和 SQL 数据集支持直连和定时同步两种连接方式
      </td>
   </tr>
   <tr>
      <td>
         定时同步类型数据集，支持全量更新和增量更新两种方式
      </td>
   </tr>
   <tr>
      <td>
         支持创建定时任务，以此控制数据集的更新
      </td>
   </tr>
   <tr>
      <td>
         支持定时更新任务的查看
      </td>
   </tr>
   <tr>
      <td>
         支持对数据集的字段类型/字段名/展示字段进行设置
      </td>
   </tr>
   <tr>
      <td>
         支持创建数据集间的关联关系（1：1，一对一；1：N，一对N；N：1，N对1）
      </td>
   </tr>
   <tr>
      <td>
         支持 Excel 数据集数据的替换和追加
      </td>
   </tr>
   <tr>
      <td rowspan="2">
         数据源
      </td>
      <td rowspan="2">
         数据源管理
      </td>
      <td>
         创建 MySQL数据源
      </td>
   </tr>
   </tr>
   <tr>
      <td>
         校验数据源的有效性
      </td>
   </tr>
   <tr>
      <td rowspan="5">
         系统管理
      </td>
      <td rowspan="2">
         用户租户管理
      </td>
      <td>
         支持多级租户体系
      </td>
   </tr>
   <tr>
      <td>
         支持多种租户角色
      </td>
   </tr>
   <tr>
      <td rowspan="2">
         权限管理
      </td>
      <td>
         支持组织/角色/用户三个维度的权限管理
      </td>
   </tr>
   <tr>
      <td>
         支持数据源/数据集/视图/仪表板/菜单和操作权限的细颗粒度权限控制
      </td>
   </tr>
   <tr>
      <td>
         显示设置
      </td>
      <td>
         支持对Logo/系统名/标题等属性的设置
      </td>
   </tr>
</table>

## 技术栈

-   后端: [Spring Boot](https://spring.io/projects/spring-boot)；
-   前端: [Vue.js](https://vuejs.org/)；
-   中间件: [MySQL](https://www.mysql.com/)；
-   数据处理: [Kettle](https://github.com/pentaho/pentaho-kettle)、[Apache Doris](https://github.com/apache/incubator-doris/)；
-   基础设施: [Docker](https://www.docker.com/)。

## 致谢

-   [Kettle](https://github.com/pentaho/pentaho-kettle/)：DataEase 使用了 Kettle 进行数据处理工作；
-   [Apache Doris](https://doris.apache.org/)：DataEase 使用了 Apache Doris 进行快速的数据分析；
-   [Element](https://element.eleme.cn/)：感谢 Element 提供的优秀组件库。

## 版本说明

DataEase 版本号命名规则为：v大版本.功能版本.Bug修复版本。比如：

```text
v1.0.1 是 v1.0.0 之后的 Bug 修复版本；
v1.1.0 是 v1.0.0 之后的功能版本。
```

像其它优秀开源项目一样，DataEase 将每月发布一个功能版本。

## License & Copyright

Copyright (c) 2014-2021 飞致云 FIT2CLOUD, All rights reserved.

Licensed under The GNU General Public License version 2 (GPLv2)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

<https://www.gnu.org/licenses/gpl-2.0.html>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
