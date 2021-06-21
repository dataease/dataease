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

<body link="blue" vlink="purple" class="xl65">
<table width="577.40" border="0" cellpadding="0" cellspacing="0" style='width:577.40pt;border-collapse:collapse;'>
   <col width="56.80" style='mso-width-source:userset;mso-width-alt:2423;'/>
   <col width="83.60" class="xl65" style='mso-width-source:userset;mso-width-alt:3566;'/>
   <col width="437" class="xl66" style='mso-width-source:userset;mso-width-alt:18645;'/>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl67" height="216" width="56.80" rowspan="12" style='height:216.00pt;width:56.80pt;border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:str>仪表板</td>
    <td class="xl68" width="83.60" rowspan="10" style='width:83.60pt;border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:str>仪表板管理</td>
    <td class="xl69" width="437" style='width:437.00pt;' x:str>在线编辑仪表板</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl69" x:str>仪表板展示内容支持视图/时间组件/文本组件/数字组件/样式组件等</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl69" x:str>支持视图及组件样式的设置</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl69" x:str>支持仪表板主题</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl69" x:str>支持仪表板的全屏预览</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl69" x:str>支持仪表板模板的导出</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl69" x:str>以树状形式展示仪表板分组</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl69" x:str>支持仪表板分组的拖拽排序</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl69" x:str>支持默认仪表板的设置</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl69" x:str>支持仪表板的收藏</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl68" rowspan="2" style='border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:str>仪表板分享</td>
    <td class="xl69" x:str>支持按组织/角色/用户分享</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl69" x:str>支持生成外部可访问的带密码保护的分享链接</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl67" height="180" rowspan="9" style='height:180.00pt;border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:str>视图</td>
    <td class="xl68" rowspan="9" style='border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:str>视图管理</td>
    <td class="xl69" x:str>在线编辑视图</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>支持可视化拖拽进行视图操作</td>
   </tr>
   <tr height="36" style='height:36.00pt;'>
    <td class="xl70" x:str>支持明细表/指标卡/基础柱状图/堆叠柱状图/横向柱状图/横向堆叠柱状图/基础折线图/堆叠折线图/饼图/南丁格尔玫瑰图/漏斗图/雷达图/仪表盘等多种图表类型</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>支持维度/指标的排序展示</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>支持指标的多种汇总计算方式</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>支持对图表类型的图形属性进行设置</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>支持对图表类型的组件样式进行设置</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>支持过滤条件筛选视图展示的数据</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>支持下载视图图片</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl67" height="144" rowspan="8" style='height:144.00pt;border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:str>数据集</td>
    <td class="xl68" rowspan="8" style='border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:str>数据集管理</td>
    <td class="xl70" x:str>支持数据库数据集/SQL 数据集/Excel 数据集/自定义数据集等多种类型的数据集</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>数据库数据集和 SQL 数据集支持直连和定时同步两种连接方式</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>定时同步类型数据集支持全量更新和增量更新两种方式</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>支持定时更新的频率控制</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>支持定时更新任务的查看</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>支持对数据集的字段类型/字段名/展示字段进行设置</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>支持创建不同类型数据集与数据集之间的关联</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>支持 Excel 数据集数据的替换和更新</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl67" height="36" rowspan="2" style='height:36.00pt;border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:str>数据源</td>
    <td class="xl68" rowspan="2" style='border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:str>数据源管理</td>
    <td class="xl70" x:str>支持 MySQL 数据源</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>支持对数据源有效性校验</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl67" height="90" rowspan="5" style='height:90.00pt;border-right:.5pt solid windowtext;' x:str>系统管理</td>
    <td class="xl68" rowspan="2" style='border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:str>用户租户管理</td>
    <td class="xl70" x:str>支持多级租户体系</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>支持多种租户角色</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl68" rowspan="2" style='border-right:.5pt solid windowtext;border-bottom:.5pt solid windowtext;' x:str>权限管理</td>
    <td class="xl70" x:str>支持组织/角色/用户三个维度的权限管理</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl70" x:str>支持数据源/数据集/视图/仪表板/菜单和操作权限的细颗粒度权限控制</td>
   </tr>
   <tr height="18" style='height:18.00pt;'>
    <td class="xl68" style='border-right:.5pt solid windowtext;' x:str>显示设置</td>
    <td class="xl70" x:str>支持对Logo/系统名/标题等属性的设置</td>
   </tr>
  </table>
</body>

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
