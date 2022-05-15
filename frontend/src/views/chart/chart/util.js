export function hexColorToRGBA(hex, alpha) {
  const rgb = [] // 定义rgb数组
  if (/^\#[0-9A-F]{3}$/i.test(hex)) { // 判断传入是否为#三位十六进制数
    let sixHex = '#'
    hex.replace(/[0-9A-F]/ig, function(kw) {
      sixHex += kw + kw // 把三位16进制数转化为六位
    })
    hex = sixHex // 保存回hex
  }
  if (/^#[0-9A-F]{6}$/i.test(hex)) { // 判断传入是否为#六位十六进制数
    hex.replace(/[0-9A-F]{2}/ig, function(kw) {
      // eslint-disable-next-line no-eval
      rgb.push(eval('0x' + kw)) // 十六进制转化为十进制并存如数组
    })
    return `rgba(${rgb.join(',')},${alpha / 100})` // 输出RGB格式颜色
  } else {
    return 'rgb(0,0,0)'
  }
}

export function digToHex(dig) {
  let prefix = ''
  const num = parseInt(dig * 2.55)
  if (num < 16) {
    prefix = '0'
  }
  return prefix.concat(num.toString(16).toUpperCase())
}

export const TYPE_CONFIGS = [
  {
    render: 'antv',
    category: 'chart.chart_type_table',
    value: 'table-normal',
    title: 'chart.chart_table_normal',
    icon: 'table-normal',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'title-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_table',
    value: 'table-info',
    title: 'chart.chart_table_info',
    icon: 'table-info',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'title-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_table',
    value: 'table-pivot',
    title: 'chart.chart_table_pivot',
    icon: 'table-pivot',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'total-cfg',
      'title-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_quota',
    value: 'label',
    title: 'chart.chart_label',
    icon: 'label',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'title-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_quota',
    value: 'text',
    title: 'chart.chart_card',
    icon: 'text',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'title-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_quota',
    value: 'gauge',
    title: 'chart.chart_gauge',
    icon: 'gauge',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'title-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_quota',
    value: 'liquid',
    title: 'chart.chart_liquid',
    icon: 'liquid',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'title-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_trend',
    value: 'line',
    title: 'chart.chart_line',
    icon: 'line',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'x-axis-selector-ant-v',
      'y-axis-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_trend',
    value: 'line-stack',
    title: 'chart.chart_line_stack',
    icon: 'line-stack',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'x-axis-selector-ant-v',
      'y-axis-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ]
  },

  {
    render: 'antv',
    category: 'chart.chart_type_compare',
    value: 'bar',
    title: 'chart.chart_bar',
    icon: 'bar',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'x-axis-selector-ant-v',
      'y-axis-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_compare',
    value: 'bar-stack',
    title: 'chart.chart_bar_stack',
    icon: 'bar-stack',
    properties: [
      'color-selector',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'x-axis-selector-ant-v',
      'y-axis-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_compare',
    value: 'waterfall',
    title: 'chart.chart_waterfall',
    icon: 'waterfall',
    properties: [
      'color-selector',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'x-axis-selector-ant-v',
      'y-axis-selector-ant-v',
      'title-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_compare',
    value: 'bar-horizontal',
    title: 'chart.chart_bar_horizontal',
    icon: 'bar-horizontal',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'x-axis-selector-ant-v',
      'y-axis-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_compare',
    value: 'bar-stack-horizontal',
    title: 'chart.chart_bar_stack_horizontal',
    icon: 'bar-stack-horizontal',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'x-axis-selector-ant-v',
      'y-axis-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ]
  },

  {
    render: 'antv',
    category: 'chart.chart_type_distribute',
    value: 'pie',
    title: 'chart.chart_pie',
    icon: 'pie',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_distribute',
    value: 'pie-rose',
    title: 'chart.chart_pie_rose',
    icon: 'pie-rose',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_distribute',
    value: 'radar',
    title: 'chart.chart_radar',
    icon: 'radar',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'split-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_distribute',
    value: 'treemap',
    title: 'chart.chart_treemap',
    icon: 'treemap',
    properties: [
      'color-selector',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_distribute',
    value: 'word-cloud',
    title: 'chart.chart_word_cloud',
    icon: 'word-cloud',
    properties: [
      'color-selector',
      'tooltip-selector-ant-v',
      'title-selector-ant-v'
    ]
  },

  {
    render: 'antv',
    category: 'chart.chart_type_relation',
    value: 'scatter',
    title: 'chart.chart_scatter',
    icon: 'scatter',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'x-axis-selector-ant-v',
      'y-axis-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ]
  },
  {
    render: 'antv',
    category: 'chart.chart_type_relation',
    value: 'funnel',
    title: 'chart.chart_funnel',
    icon: 'funnel',
    properties: [
      'color-selector',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ]
  },
  /* 下面是echarts图表类型 */
  {
    render: 'echarts',
    category: 'chart.chart_type_table',
    value: 'table-normal',
    title: 'chart.chart_table_normal',
    icon: 'table-normal',
    properties: [
      'color-selector',
      'size-selector',
      'title-selector'
    ]
  },
  {
    render: 'echarts',
    category: 'chart.chart_type_table',
    value: 'table-info',
    title: 'chart.chart_table_info',
    icon: 'table-info',
    properties: [
      'color-selector',
      'size-selector',
      'title-selector'
    ]
  },

  {
    render: 'echarts',
    category: 'chart.chart_type_quota',
    value: 'label',
    title: 'chart.chart_label',
    icon: 'label',
    properties: [
      'color-selector',
      'size-selector',
      'title-selector'
    ]
  },
  {
    render: 'echarts',
    category: 'chart.chart_type_quota',
    value: 'text',
    title: 'chart.chart_card',
    icon: 'text',
    properties: [
      'color-selector',
      'size-selector',
      'title-selector'
    ]
  },
  {
    render: 'echarts',
    category: 'chart.chart_type_quota',
    value: 'gauge',
    title: 'chart.chart_gauge',
    icon: 'gauge',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'title-selector',
    ]
  },

  {
    render: 'echarts',
    category: 'chart.chart_type_trend',
    value: 'line',
    title: 'chart.chart_line',
    icon: 'line',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'x-axis-selector',
      'y-axis-selector',
      'title-selector',
      'legend-selector'
    ]
  },
  {
    render: 'echarts',
    category: 'chart.chart_type_trend',
    value: 'line-stack',
    title: 'chart.chart_line_stack',
    icon: 'line-stack',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'x-axis-selector',
      'y-axis-selector',
      'title-selector',
      'legend-selector'
    ]
  },
  {
    render: 'echarts',
    category: 'chart.chart_type_trend',
    value: 'chart-mix',
    title: 'chart.chart_mix',
    icon: 'chart-mix',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'x-axis-selector',
      'y-axis-selector',
      'y-axis-ext-selector',
      'title-selector',
      'legend-selector'
    ]
  },

  {
    render: 'echarts',
    category: 'chart.chart_type_compare',
    value: 'bar',
    title: 'chart.chart_bar',
    icon: 'bar',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'x-axis-selector',
      'y-axis-selector',
      'title-selector',
      'legend-selector'
    ]
  },
  {
    render: 'echarts',
    category: 'chart.chart_type_compare',
    value: 'bar-stack',
    title: 'chart.chart_bar_stack',
    icon: 'bar-stack',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'x-axis-selector',
      'y-axis-selector',
      'title-selector',
      'legend-selector'
    ]
  },
  {
    render: 'echarts',
    category: 'chart.chart_type_compare',
    value: 'bar-horizontal',
    title: 'chart.chart_bar_horizontal',
    icon: 'bar-horizontal',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'x-axis-selector',
      'y-axis-selector',
      'title-selector',
      'legend-selector'
    ]
  },
  {
    render: 'echarts',
    category: 'chart.chart_type_compare',
    value: 'bar-stack-horizontal',
    title: 'chart.chart_bar_stack_horizontal',
    icon: 'bar-stack-horizontal',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'x-axis-selector',
      'y-axis-selector',
      'title-selector',
      'legend-selector'
    ]
  },

  {
    render: 'echarts',
    category: 'chart.chart_type_distribute',
    value: 'pie',
    title: 'chart.chart_pie',
    icon: 'pie',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'title-selector',
      'legend-selector'
    ]
  },
  {
    render: 'echarts',
    category: 'chart.chart_type_distribute',
    value: 'pie-rose',
    title: 'chart.chart_pie_rose',
    icon: 'pie-rose',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'title-selector',
      'legend-selector'
    ]
  },
  {
    render: 'echarts',
    category: 'chart.chart_type_distribute',
    value: 'radar',
    title: 'chart.chart_radar',
    icon: 'radar',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'split-selector',
      'title-selector',
      'legend-selector'
    ]
  },
  {
    render: 'echarts',
    category: 'chart.chart_type_distribute',
    value: 'treemap',
    title: 'chart.chart_treemap',
    icon: 'treemap',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'title-selector'
    ]
  },

  {
    render: 'echarts',
    category: 'chart.chart_type_relation',
    value: 'scatter',
    title: 'chart.chart_scatter',
    icon: 'scatter',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'x-axis-selector',
      'y-axis-selector',
      'title-selector',
      'legend-selector'
    ]
  },
  {
    render: 'echarts',
    category: 'chart.chart_type_relation',
    value: 'funnel',
    title: 'chart.chart_funnel',
    icon: 'funnel',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'title-selector',
      'legend-selector'
    ]
  },

  {
    render: 'echarts',
    category: 'chart.chart_type_space',
    value: 'map',
    title: 'chart.chart_map',
    icon: 'map',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'title-selector'
    ]
  }
]

export function customSort(custom, data) {
  const indexArr = []
  const joinArr = []
  for (let i = 0; i < custom.length; i++) {
    const ele = custom[i]
    for (let j = 0; j < data.length; j++) {
      const d = data[j]
      if (ele === d.field) {
        joinArr.push(d)
        indexArr.push(j)
      }
    }
  }
  // 取得 joinArr 就是两者的交集
  const indexArrData = []
  for (let i = 0; i < data.length; i++) {
    indexArrData.push(i)
  }
  const indexResult = []
  for (let i = 0; i < indexArrData.length; i++) {
    if (indexArr.indexOf(indexArrData[i]) === -1) {
      indexResult.push(indexArrData[i])
    }
  }

  const subArr = []
  for (let i = 0; i < indexResult.length; i++) {
    subArr.push(data[indexResult[i]])
  }

  return joinArr.concat(subArr)
}
