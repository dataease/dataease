const filterDatasetRecord = {
  name: 'dataset.task.last_exec_status',
  type: 'dataset_table_task.last_exec_status',
  activeType: 'execStatus',
  list: [{
    name: 'xpacktask.success',
    value: '1'
  }, {
    name: 'dataset.task.exec',
    value: '0'
  },
  {
    name: 'xpacktask.error',
    value: '-1'
  }]
}

// 入参 fmt-格式 date-日期
function dateFormat(fmt, date) {
  let ret
  const opt = {
    'Y+': date.getFullYear().toString(), // 年
    'm+': (date.getMonth() + 1).toString(), // 月
    'd+': date.getDate().toString(), // 日
    'H+': date.getHours().toString(), // 时
    'M+': date.getMinutes().toString(), // 分
    'S+': date.getSeconds().toString() // 秒
    // 有其他格式化字符需求可以继续添加，必须转化成字符串
  }
  for (const k in opt) {
    ret = new RegExp('(' + k + ')').exec(fmt)
    if (ret) {
      fmt = fmt.replace(ret[1], (ret[1].length === 1) ? (opt[k]) : (opt[k].padStart(ret[1].length, '0')))
    }
  }
  return fmt
}

const timeType = [{
  label: 'emailtask.once_a_day',
  value: 0
}, {
  label: 'emailtask.once_a_week',
  value: 1
}, {
  label: 'emailtask.once_a_month',
  value: 2
}]
const simpleTimeType = [{
  label: 'emailtask.hour',
  value: -1
}, {
  label: 'emailtask.day',
  value: 0
}, {
  label: 'emailtask.week',
  value: 1
}, {
  label: 'emailtask.month',
  value: 2
}]

const weekType = Array(7).fill(1).map((ele, index) => {
  return {
    label: `emailtask.week_${['mon', 'tue', 'wed', 'thu', 'fri', 'sat', 'sun'][index]}`,
    value: index + 1
  }
})

const monthType = Array(31).fill(1).map((ele, index) => {
  return {
    label: `${index + 1}`,
    value: index + 1
  }
})

export {
  dateFormat,
  filterDatasetRecord,
  timeType,
  weekType,
  monthType,
  simpleTimeType
}
