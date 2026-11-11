package io.dataease.api.sync.task.api;

import io.dataease.api.sync.task.dto.TaskMaintenanceRequest;
import io.dataease.api.sync.task.vo.TaskMaintenanceVO;
import io.dataease.api.sync.task.dto.TaskExecutionRecoveryRequest;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import static io.dataease.constant.AuthResourceEnum.TASK;

/**
 * 独立维护入口，避免调用原任务编辑的删表重建路径
 */
@DeApiPath(value = "/sync/task/maintenance", rt = TASK)
public interface TaskMaintenanceApi {
    /**
     * 读取任务已保存字段、源字段和当前目标能力，读取失败时保留已保存配置及具体阻断原因
     * 全量确认可跳过源字段和候选类型读取，不发起同步或改变执行配置
     */
    @GetMapping("/{id}")
    @DePermit("m:read")
    TaskMaintenanceVO context(@PathVariable String id,
                              @RequestParam(value = "fullRefresh", defaultValue = "false") boolean fullRefresh);

    /**
     * 轮询轻量进度，不重新读取外部表结构或携带完整字段快照
     */
    @GetMapping("/status/{id}")
    @DePermit("m:read")
    TaskMaintenanceVO status(@PathVariable String id);

    /**
     * 按需读取当前调整的前后字段和完整步骤，不执行结构变更
     */
    @GetMapping("/details/{id}")
    @DePermit("m:read")
    TaskMaintenanceVO details(@PathVariable String id);

    /**
     * 整批检查字段差异并生成计划，未通过时返回字段定位信息，不提交 DDL
     */
    @PostMapping("/preview")
    @DePermit("m:manage")
    TaskMaintenanceVO preview(@Valid @RequestBody TaskMaintenanceRequest request);

    /**
     * 重新核验预检查指纹及风险确认后启动调整，同一任务的相同操作编号不重复应用
     */
    @PostMapping("/apply")
    @DePermit("m:manage")
    TaskMaintenanceVO apply(@Valid @RequestBody TaskMaintenanceRequest request);

    /**
     * 继续已有调整或保存配置，跳过已完成步骤，不将结果未知的步骤直接重发
     */
    @PostMapping("/resume")
    @DePermit("m:manage")
    TaskMaintenanceVO resume(@Valid @RequestBody TaskMaintenanceRequest request);

    /**
     * 查询数据库作业和实际结构并补记结果，不提交新的 DDL
     */
    @PostMapping("/recheck")
    @DePermit("m:manage")
    TaskMaintenanceVO recheck(@Valid @RequestBody TaskMaintenanceRequest request);

    /**
     * 保留已核实完成的修改，恢复一致配置并结束剩余步骤，不回滚已执行的数据库变更
     */
    @PostMapping("/finish")
    @DePermit("m:manage")
    TaskMaintenanceVO finish(@Valid @RequestBody TaskMaintenanceRequest request);

    /**
     * 按当前生效配置执行一次全量同步，不修改已保存的增量方式和调度设置
     */
    @PostMapping("/full")
    @DePermit("m:manage")
    TaskMaintenanceVO full(@Valid @RequestBody TaskMaintenanceRequest request);

    /**
     * 用户主动重新核对当前运行或替表结果，绕过自动核对次数限制，不重发同步或已提交的替表
     * 取数确认成功后仍按原流程完成尚未提交的替表
     */
    @PostMapping("/execution/recheck")
    @DePermit("m:manage")
    TaskMaintenanceVO recheckExecution(@Valid @RequestBody TaskExecutionRecoveryRequest request);

    /**
     * 操作人核实数据库结果并填写依据后结束运行，替表结果仍需服务端核对实际结构
     * 取数结果不明时只允许按失败结束，不解除关联初始化要求
     */
    @PostMapping("/execution/resolve")
    @DePermit("m:manage")
    TaskMaintenanceVO resolveExecution(@Valid @RequestBody TaskExecutionRecoveryRequest request);
}
