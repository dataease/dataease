const filterDatasetRecord = {
    name: 'dataset.task.last_exec_status',
    type: 'dataset_table_task.last_exec_status',
    activeType: 'execStatus',
    list: [{
        name: 'xpacktask.success',
        value: 'Completed',
    }, {
        name: 'dataset.task.exec',
        value: 'Underway',
    },
    {
        name: 'xpacktask.error',
        value: 'Error',
    }]
}
const filterDataset = [{
    name: 'dataset.execute_rate',
    type: 'dataset_table_task.rate',
    activeType: 'rate',
    list: [{
        name: 'dataset.execute_once',
        value: 'SIMPLE',
    }, {
        name: 'dataset.cron_config',
        value: 'CRON',
    },
    {
        name: 'dataset.simple_cron',
        value: 'SIMPLE_CRON',
    }]
}, {
    name: 'dataset.task.task_status',
    type: 'dataset_table_task.status',
    activeType: 'status',
    list: [{
        name: 'dataset.task.underway',
        value: 'Underway',
    }, {
        name: 'dataset.task.exec',
        value: 'Exec',
    },
    {
        name: 'dataset.task.pending',
        value: 'Pending',
    }, {
        name: 'dataset.task.stopped',
        value: 'Stopped',
    }]
}, {
    ...filterDatasetRecord
}]


// 入参 fmt-格式 date-日期
function dateFormat(fmt, date) {
    let ret;
    const opt = {
        "Y+": date.getFullYear().toString(),        // 年
        "m+": (date.getMonth() + 1).toString(),     // 月
        "d+": date.getDate().toString(),            // 日
        "H+": date.getHours().toString(),           // 时
        "M+": date.getMinutes().toString(),         // 分
        "S+": date.getSeconds().toString()          // 秒
        // 有其他格式化字符需求可以继续添加，必须转化成字符串
    };
    for (let k in opt) {
        ret = new RegExp("(" + k + ")").exec(fmt);
        if (ret) {
            fmt = fmt.replace(ret[1], (ret[1].length == 1) ? (opt[k]) : (opt[k].padStart(ret[1].length, "0")))
        };
    };
    return fmt;
}

export {
    filterDataset,
    dateFormat,
    filterDatasetRecord
}