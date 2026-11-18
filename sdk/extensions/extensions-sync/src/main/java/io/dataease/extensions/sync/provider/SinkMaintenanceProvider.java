package io.dataease.extensions.sync.provider;

import io.dataease.extensions.sync.model.SinkMaintenance;
import io.dataease.extensions.sync.model.TableFieldDTO;
import io.dataease.extensions.sync.model.task.Target;

import java.util.List;

/**
 * 可选目标维护扩展，未实现此接口的旧插件保留原有同步行为
 * 结构和能力必须来自当前连接及实际目标表，规划阶段不得执行 DDL
 */
public interface SinkMaintenanceProvider {
    /**
     * 插件替表必须使用请求中的临时表名，声明后才能按运行隔离临时表，旧插件默认保持原命名
     */
    default boolean isolatedTemporaryTables() {
        return false;
    }

    /**
     * 替表前保存数据库对象凭据，返回 null 表示没有可用于自动恢复的凭据
     */
    default String replacementReceipt(Target target, String temporaryTable) {
        return null;
    }

    /**
     * 仅根据保存凭据核实替表成功，不凭表结构相同或临时表消失推定数据已替换
     */
    default SinkMaintenance.Outcome inspectReplacement(Target target, String temporaryTable, String receipt) {
        return SinkMaintenance.Outcome.of("UNKNOWN", receipt, null);
    }
    /**
     * 归一当前目标数据库的等价类型名称，不改变源字段类型或放宽长度精度约束
     */
    default String normalizeType(String type) {
        return type;
    }

    /**
     * 原地归一目标字段的类型和有效参数，调用方仅传入副本或待保存字段
     * 默认保留长度精度，数据库特有的无效参数由对应插件清除，不修改源类型或字段约束
     */
    default void normalizeField(TableFieldDTO field) {
        field.setFieldType(normalizeType(field.getFieldType()));
    }

    /**
     * 读取实际版本、列、索引和受保护字段；能力默认关闭，无法可靠核实时返回 blockedReason
     * operations 使用 ADD、DELETE、UPDATE、INDEX，fullRefresh 依赖原 SinkProvider 的临时表及替表能力
     * rules、长度精度限制和 typeChanges 由当前目标插件提供，页面不推断其他数据库的规则
     * typeChanges 描述已有目标类型允许变更到的类型，源到目标的映射仍在任务预览阶段检查
     * typeChangeRules 可补充 SAFE、WARNING、FORBIDDEN、原因及最小长度；旧插件仍可只提供 typeChanges
     * rules 区分官方限制、插件范围和任务规则，官方原文及文档须匹配实际数据库版本
     */
    SinkMaintenance.Structure inspect(Target target);

    /**
     * 仅生成可重复的执行计划，不修改数据库；步骤 ID、顺序和内容必须在相同输入下保持一致
     * 步骤使用 ADD、DROP、RENAME、TYPE、COMMENT、ADD_INDEX、DROP_INDEX，before/after 表示该步骤前后的完整字段
     * 插件负责转换白名单、索引依赖及默认键输入变化判断，字段问题通过 Plan.reject 同时返回门禁与定位信息
     * 整批检查应收集可独立判断的问题，不因某个字段不合法而执行其他字段的步骤
     * 存在数据内容、精度或后续写入风险时设置 riskConfirmationRequired 并返回 warnings，不能默认通过
     * 步骤 ID 在一个计划内唯一且最多 32 个字符，不依赖客户端生成全局唯一的操作 ID
     */
    default SinkMaintenance.Plan plan(Target target, SinkMaintenance.Structure structure,
                                      List<SinkMaintenance.Change> changes, List<TableFieldDTO> fields) {
        throw new UnsupportedOperationException();
    }

    /**
     * 每个已认领步骤仅提交一次；返回 DONE、WAITING、FAILED 或 UNKNOWN，异步作业标识保存在 receipt
     * FAILED 仅用于能够确认未生效的失败，超时和响应丢失应返回 UNKNOWN，不能在插件内盲目重试
     */
    default SinkMaintenance.Outcome applyStep(Target target, SinkMaintenance.Step step) {
        throw new UnsupportedOperationException();
    }

    /**
     * 依据 receipt、数据库作业和实际结构核对结果，不重新提交 DDL
     * 无法确认完成或失败时保留 UNKNOWN，只有实际效果确认后才返回 DONE
     */
    default SinkMaintenance.Outcome inspectStep(Target target, SinkMaintenance.Step step) {
        throw new UnsupportedOperationException();
    }
}
