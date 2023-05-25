import { DEFAULT_TITLE_STYLE } from '@/views/chart/chart/chart'
import { equalsAny, includesAny } from '@/utils/StringUtils'
import _ from 'lodash'

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
    ],
    propertyInner: {
      'color-selector': [
        'tableHeaderBgColor',
        'tableItemBgColor',
        'tableHeaderFontColor',
        'tableFontColor',
        'tableBorderColor',
        'tableScrollBarColor',
        'alpha'
      ],
      'size-selector-ant-v': [
        'tableTitleFontSize',
        'tableItemFontSize',
        'tableHeaderAlign',
        'tableItemAlign',
        'tableTitleHeight',
        'tableItemHeight',
        'tableColumnMode',
        'showIndex',
        'indexLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'tableHeaderBgColor',
        'tableItemBgColor',
        'tableHeaderFontColor',
        'tableFontColor',
        'tableBorderColor',
        'tableScrollBarColor',
        'alpha'
      ],
      'size-selector-ant-v': [
        'tablePageMode',
        'tablePageSize',
        'tableTitleFontSize',
        'tableItemFontSize',
        'tableHeaderAlign',
        'tableItemAlign',
        'tableTitleHeight',
        'tableItemHeight',
        'tableColumnMode',
        'showIndex',
        'indexLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'tableHeaderBgColor',
        'tableItemBgColor',
        'tableHeaderFontColor',
        'tableFontColor',
        'tableBorderColor',
        'tableScrollBarColor',
        'alpha'
      ],
      'size-selector-ant-v': [
        'tableTitleFontSize',
        'tableItemFontSize',
        'tableHeaderAlign',
        'tableItemAlign',
        'tableTitleHeight',
        'tableItemHeight',
        'tableColumnMode'
      ],
      'total-cfg': [
        'row',
        'col'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'quotaColor',
        'dimensionColor'
      ],
      'size-selector-ant-v': [
        'quotaFontSize',
        'quotaFontFamily',
        'quotaFontStyle',
        'quotaLetterSpace',
        'quotaFontShadow',
        'dimensionShow',
        'dimensionFontSize',
        'dimensionFontFamily',
        'dimensionFontStyle',
        'dimensionLetterSpace',
        'dimensionFontShadow',
        'spaceSplit',
        'hPosition',
        'vPosition'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'quotaColor',
        'dimensionColor'
      ],
      'size-selector-ant-v': [
        'quotaFontSize',
        'quotaFontFamily',
        'quotaFontStyle',
        'quotaLetterSpace',
        'quotaFontShadow',
        'dimensionShow',
        'dimensionFontSize',
        'dimensionFontFamily',
        'dimensionFontStyle',
        'dimensionLetterSpace',
        'dimensionFontShadow',
        'spaceSplit',
        'hPosition',
        'vPosition'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'custom',
        'gradient',
        'alpha'
      ],
      'size-selector-ant-v': [
        'gaugeMin',
        'gaugeMax',
        'gaugeStartAngle',
        'gaugeEndAngle',
        'gaugeTickCount'
      ],
      'label-selector-ant-v': [
        'labelGauge'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector-ant-v': [
        'liquidShape',
        'liquidMax',
        'liquidSize'
      ],
      'label-selector-ant-v': [
        'labelGauge'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ]
    }
  },
  {
    render: 'antv',
    category: 'chart.chart_type_quota',
    value: 'richTextView',
    title: 'chart.rich_text_view',
    icon: 'richTextView',
    properties: [
      'title-selector-ant-v'
    ],
    propertyInner: {
      'title-selector-ant-v': [
        'title'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'alpha'
      ],
      'size-selector-ant-v': [
        'lineWidth',
        'lineSymbol',
        'lineSymbolSize',
        'lineSmooth'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'x-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'y-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
  },
  {
    render: 'antv',
    category: 'chart.chart_type_trend',
    value: 'area',
    title: 'chart.chart_area',
    icon: 'area',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'x-axis-selector-ant-v',
      'y-axis-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'alpha',
        'gradient'
      ],
      'size-selector-ant-v': [
        'lineWidth',
        'lineSymbol',
        'lineSymbolSize',
        'lineSmooth'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'x-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'y-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'gradient',
        'alpha'
      ],
      'size-selector-ant-v': [
        'lineWidth',
        'lineSymbol',
        'lineSymbolSize',
        'lineSmooth'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'x-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'y-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'gradient',
        'alpha'
      ],
      'size-selector-ant-v': [
        'barDefault',
        'barGap'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-v'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'x-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'y-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
  },
  {
    render: 'antv',
    category: 'chart.chart_type_compare',
    value: 'bar-group',
    title: 'chart.chart_bar_group',
    icon: 'bar-group',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'x-axis-selector-ant-v',
      'y-axis-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'gradient',
        'alpha'
      ],
      'size-selector-ant-v': [
        'barDefault',
        'barGap'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-v'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'x-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'y-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
  },
  {
    render: 'antv',
    category: 'chart.chart_type_compare',
    value: 'bar-stack',
    title: 'chart.chart_bar_stack',
    icon: 'bar-stack',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'x-axis-selector-ant-v',
      'y-axis-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'gradient',
        'alpha'
      ],
      'size-selector-ant-v': [
        'barDefault',
        'barGap'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-v'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'x-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'y-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
  },
  {
    render: 'antv',
    category: 'chart.chart_type_compare',
    value: 'bar-group-stack',
    title: 'chart.chart_bar_group_stack',
    icon: 'bar-group-stack',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'x-axis-selector-ant-v',
      'y-axis-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'gradient',
        'alpha'
      ],
      'size-selector-ant-v': [
        'barDefault',
        'barGap'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-v',
        'labelContent'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'x-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'y-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'custom',
        'gradient',
        'alpha'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-v'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'x-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'y-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'gradient',
        'alpha'
      ],
      'size-selector-ant-v': [
        'barDefault',
        'barGap'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-h'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'x-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'y-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'gradient',
        'alpha'
      ],
      'size-selector-ant-v': [
        'barDefault',
        'barGap'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-h'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'x-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'y-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
  },
  {
    render: 'antv',
    category: 'chart.chart_type_compare',
    value: 'percentage-bar-stack-horizontal',
    title: 'chart.chart_percentage_bar_stack_horizontal',
    icon: 'percentage-bar-stack-horizontal',
    properties: [
      'color-selector',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'x-axis-selector-ant-v',
      'y-axis-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'gradient',
        'alpha'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-h',
        'reserveDecimalCount'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'x-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'y-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'alpha'
      ],
      'size-selector-ant-v': [
        'pieOuterRadius'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-pie',
        'labelContent',
        'reserveDecimalCount'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
  },
  {
    render: 'antv',
    category: 'chart.chart_type_distribute',
    value: 'pie-donut',
    title: 'chart.chart_pie_donut',
    icon: 'pie-donut',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'alpha'
      ],
      'size-selector-ant-v': [
        'pieInnerRadius',
        'pieOuterRadius'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-pie',
        'labelContent',
        'reserveDecimalCount'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'alpha'
      ],
      'size-selector-ant-v': [
        'pieOuterRadius'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-pie'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
  },
  {
    render: 'antv',
    category: 'chart.chart_type_distribute',
    value: 'pie-donut-rose',
    title: 'chart.chart_pie_donut_rose',
    icon: 'pie-donut-rose',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'alpha'
      ],
      'size-selector-ant-v': [
        'pieInnerRadius',
        'pieOuterRadius'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-pie'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
  },
  {
    render: 'antv',
    category: 'chart.chart_type_compare',
    value: 'percentage-bar-stack',
    title: 'chart.chart_percentage_bar_stack',
    icon: 'percentage-bar-stack',
    properties: [
      'color-selector',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'x-axis-selector-ant-v',
      'y-axis-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'gradient',
        'alpha'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-v',
        'reserveDecimalCount'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'x-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'y-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
  },
  {
    render: 'antv',
    category: 'chart.chart_type_compare',
    value: 'bidirectional-bar',
    title: 'chart.chart_bidirectional_bar',
    icon: 'bidirectional-bar',
    properties: [
      'color-selector',
      'label-selector-ant-v',
      'tooltip-selector-ant-v',
      'x-axis-selector-ant-v',
      'y-axis-selector-ant-v',
      'title-selector-ant-v',
      'legend-selector-ant-v'
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'gradient',
        'alpha'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-h'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'x-axis-selector-ant-v': [
        'show',
        'position',
        'nameTextStyle',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'y-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'alpha'
      ],
      'size-selector-ant-v': [
        'radarShape',
        'radarSize'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-v'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ],
      'split-selector-ant-v': [
        'splitForm',
        'name',
        'lineStyle'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'alpha'
      ],
      'size-selector-ant-v': [
        'scatterSymbol',
        'scatterSymbolSize'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'x-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'y-axis-selector-ant-v': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisForm',
        'axisLabel'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'value',
        'colorPanel',
        'customColor',
        'alpha'
      ],
      'label-selector-ant-v': [
        'show',
        'fontSize',
        'color',
        'position-h'
      ],
      'tooltip-selector-ant-v': [
        'show',
        'textStyle'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ],
      'legend-selector-ant-v': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
  },
  {
    render: 'antv',
    category: 'chart.chart_type_space',
    value: 'flow-map',
    title: 'chart.chart_flow_map',
    icon: 'flow-map',
    properties: [
      'color-selector',
      'size-selector-ant-v',
      'title-selector-ant-v'
    ],
    propertyInner: {
      'color-selector': [
        'alpha',
        'mapStyle',
        'mapLineGradient',
        'mapLineSourceColor',
        'mapLineTargetColor'
      ],
      'size-selector-ant-v': [
        'mapPitch',
        'mapLineType',
        'mapLineWidth',
        'mapLineAnimate',
        'mapLineAnimateDuration',
        'mapLineAnimateInterval',
        'mapLineAnimateTrailLength'
      ],
      'title-selector-ant-v': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'remarkShow',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'tableHeaderBgColor',
        'tableItemBgColor',
        'tableHeaderFontColor',
        'tableFontColor',
        'tableBorderColor',
        'tableScrollBarColor',
        'alpha'
      ],
      'size-selector': [
        'tableTitleFontSize',
        'tableItemFontSize',
        'tableTitleHeight',
        'tableItemHeight',
        'tableColumnWidth',
        'showIndex',
        'indexLabel',
        'tableAutoBreakLine'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder'
      ]
    }
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
    ],
    propertyInner: {
      'color-selector': [
        'tableHeaderBgColor',
        'tableItemBgColor',
        'tableHeaderFontColor',
        'tableFontColor',
        'tableBorderColor',
        'tableScrollBarColor',
        'alpha'
      ],
      'size-selector': [
        'tablePageMode',
        'tablePageSize',
        'tableTitleFontSize',
        'tableItemFontSize',
        'tableTitleHeight',
        'tableItemHeight',
        'tableColumnWidth',
        'showIndex',
        'indexLabel',
        'tableAutoBreakLine'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ]
    }
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
    ],
    propertyInner: {

      'color-selector': [
        'quotaColor',
        'dimensionColor'
      ],
      'size-selector': [
        'quotaFontSize',
        'quotaFontFamily',
        'quotaFontStyle',
        'quotaLetterSpace',
        'quotaFontShadow',
        'dimensionShow',
        'dimensionFontSize',
        'dimensionFontFamily',
        'dimensionFontStyle',
        'dimensionLetterSpace',
        'dimensionFontShadow',
        'spaceSplit',
        'hPosition',
        'vPosition'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ]
    }
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
    ],
    propertyInner: {

      'color-selector': [
        'quotaColor',
        'dimensionColor'
      ],
      'size-selector': [
        'quotaFontSize',
        'quotaFontFamily',
        'quotaFontStyle',
        'quotaLetterSpace',
        'quotaFontShadow',
        'dimensionShow',
        'dimensionFontSize',
        'dimensionFontFamily',
        'dimensionFontStyle',
        'dimensionLetterSpace',
        'dimensionFontShadow',
        'spaceSplit',
        'hPosition',
        'vPosition'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'isItalic',
        'isBolder',
        'fontFamily',
        'letterSpace',
        'fontShadow'
      ]
    }
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
      'title-selector'
    ],
    propertyInner: {

      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'gaugeMin',
        'gaugeMax',
        'gaugeStartAngle',
        'gaugeEndAngle'
      ],
      'label-selector': [
        'show',
        'fontSize',
        'color',
        'gaugeFormatter'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ]
    }
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
      'legend-selector',
      'margin-selector'
    ],
    propertyInner: {
      'margin-selector': [
        'marginModel',
        'marginTop',
        'marginBottom',
        'marginLeft',
        'marginRight'
      ],
      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'lineWidth',
        'lineSymbol',
        'lineType',
        'lineSymbolSize',
        'lineSmooth'
      ],
      'label-selector': [
        'show',
        'fontSize',
        'color',
        'formatter'
      ],
      'tooltip-selector': [
        'show',
        'trigger',
        'textStyle',
        'formatter'
      ],
      'x-axis-selector': [
        'axisForm',
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisLabel'
      ],
      'y-axis-selector': [
        'axisForm',
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisLabel'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ],
      'legend-selector': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
      'legend-selector',
      'margin-selector'
    ],
    propertyInner: {
      'margin-selector': [
        'marginModel',
        'marginTop',
        'marginBottom',
        'marginLeft',
        'marginRight'
      ],
      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'lineWidth',
        'lineSymbol',
        'lineType',
        'lineSymbolSize',
        'lineSmooth'
      ],
      'label-selector': [
        'show',
        'fontSize',
        'color',
        'formatter'
      ],
      'tooltip-selector': [
        'show',
        'trigger',
        'textStyle',
        'formatter'
      ],
      'x-axis-selector': [
        'axisForm',
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisLabel'
      ],
      'y-axis-selector': [
        'axisForm',
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisLabel'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ],
      'legend-selector': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
      'legend-selector',
      'margin-selector'
    ],
    propertyInner: {
      'margin-selector': [
        'marginModel',
        'marginTop',
        'marginBottom',
        'marginLeft',
        'marginRight'
      ],
      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'mix'
      ],
      'label-selector': [
        'show',
        'fontSize',
        'color',
        'position-v',
        'formatter'
      ],
      'tooltip-selector': [
        'show',
        'trigger',
        'textStyle',
        'formatter'
      ],
      'x-axis-selector': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisLabel'
      ],
      'y-axis-selector': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisLabel'
      ],
      'y-axis-ext-selector': [
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisLabel'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ],
      'legend-selector': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
      'legend-selector',
      'margin-selector'
    ],
    propertyInner: {
      'margin-selector': [
        'marginModel',
        'marginTop',
        'marginBottom',
        'marginLeft',
        'marginRight'
      ],
      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'barDefault',
        'barWidth',
        'barGap'
      ],
      'label-selector': [
        'show',
        'fontSize',
        'color',
        'position-v',
        'formatter'
      ],
      'tooltip-selector': [
        'show',
        'trigger',
        'textStyle',
        'formatter'
      ],
      'x-axis-selector': [
        'axisForm',
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisLabel'
      ],
      'y-axis-selector': [
        'axisForm',
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisLabel'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ],
      'legend-selector': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
      'legend-selector',
      'margin-selector'
    ],
    propertyInner: {
      'margin-selector': [
        'marginModel',
        'marginTop',
        'marginBottom',
        'marginLeft',
        'marginRight'
      ],
      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'barDefault',
        'barWidth',
        'barGap'
      ],
      'label-selector': [
        'show',
        'fontSize',
        'color',
        'position-v',
        'formatter'
      ],
      'tooltip-selector': [
        'show',
        'trigger',
        'textStyle',
        'formatter'
      ],
      'x-axis-selector': [
        'axisForm',
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisLabel'
      ],
      'y-axis-selector': [
        'axisForm',
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisLabel'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ],
      'legend-selector': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
      'legend-selector',
      'margin-selector'
    ],
    propertyInner: {
      'margin-selector': [
        'marginModel',
        'marginTop',
        'marginBottom',
        'marginLeft',
        'marginRight'
      ],
      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'barDefault',
        'barWidth',
        'barGap'
      ],
      'label-selector': [
        'show',
        'fontSize',
        'color',
        'position-h',
        'formatter'
      ],
      'tooltip-selector': [
        'show',
        'trigger',
        'textStyle',
        'formatter'
      ],
      'x-axis-selector': [
        'axisForm',
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisLabel'
      ],
      'y-axis-selector': [
        'axisForm',
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisLabel'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ],
      'legend-selector': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
      'legend-selector',
      'margin-selector'
    ],
    propertyInner: {
      'margin-selector': [
        'marginModel',
        'marginTop',
        'marginBottom',
        'marginLeft',
        'marginRight'
      ],
      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'barDefault',
        'barWidth',
        'barGap'
      ],
      'label-selector': [
        'show',
        'fontSize',
        'color',
        'position-h',
        'formatter'
      ],
      'tooltip-selector': [
        'show',
        'trigger',
        'textStyle',
        'formatter'
      ],
      'x-axis-selector': [
        'axisForm',
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisLabel'
      ],
      'y-axis-selector': [
        'axisForm',
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisLabel'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ],
      'legend-selector': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
    ],
    propertyInner: {

      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'pieOuterRadius'
      ],
      'label-selector': [
        'show',
        'labelLine',
        'fontSize',
        'color',
        'position-pie',
        'formatter'
      ],
      'tooltip-selector': [
        'show',
        'trigger',
        'textStyle',
        'formatter'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ],
      'legend-selector': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
  },
  {
    render: 'echarts',
    category: 'chart.chart_type_distribute',
    value: 'pie-donut',
    title: 'chart.chart_pie_donut',
    icon: 'pie-donut',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'title-selector',
      'legend-selector'
    ],
    propertyInner: {

      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'pieInnerRadius',
        'pieOuterRadius'
      ],
      'label-selector': [
        'show',
        'labelLine',
        'fontSize',
        'color',
        'position-pie',
        'formatter'
      ],
      'tooltip-selector': [
        'show',
        'trigger',
        'textStyle',
        'formatter'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ],
      'legend-selector': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
    ],
    propertyInner: {

      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'pieOuterRadius',
        'pieRoseType',
        'pieRoseRadius'
      ],
      'label-selector': [
        'show',
        'fontSize',
        'color',
        'position-pie',
        'formatter'
      ],
      'tooltip-selector': [
        'show',
        'trigger',
        'textStyle',
        'formatter'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ],
      'legend-selector': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
  },
  {
    render: 'echarts',
    category: 'chart.chart_type_distribute',
    value: 'pie-donut-rose',
    title: 'chart.chart_pie_donut_rose',
    icon: 'pie-donut-rose',
    properties: [
      'color-selector',
      'size-selector',
      'label-selector',
      'tooltip-selector',
      'title-selector',
      'legend-selector'
    ],
    propertyInner: {

      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'pieInnerRadius',
        'pieOuterRadius',
        'pieRoseType',
        'pieRoseRadius'
      ],
      'label-selector': [
        'show',
        'fontSize',
        'color',
        'position-pie',
        'formatter'
      ],
      'tooltip-selector': [
        'show',
        'trigger',
        'textStyle',
        'formatter'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ],
      'legend-selector': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
    ],
    propertyInner: {

      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'radarShape',
        'radarSize'
      ],
      'label-selector': [
        'show',
        'fontSize',
        'color',
        'position-v',
        'formatter'
      ],
      'tooltip-selector': [
        'show',
        'trigger',
        'textStyle',
        'formatter'
      ],
      'split-selector': [
        'name',
        'axisLine',
        'axisLabel',
        'splitLine',
        'splitArea'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ],
      'legend-selector': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
    ],
    propertyInner: {

      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'treemapWidth',
        'treemapHeight'
      ],
      'label-selector': [
        'show',
        'fontSize',
        'color',
        'formatter'
      ],
      'tooltip-selector': [
        'show',
        'trigger',
        'textStyle',
        'formatter'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ]
    }
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
      'legend-selector',
      'margin-selector'
    ],
    propertyInner: {
      'margin-selector': [
        'marginModel',
        'marginTop',
        'marginBottom',
        'marginLeft',
        'marginRight'
      ],
      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'scatterSymbol',
        'scatterSymbolSize'
      ],
      'label-selector': [
        'show',
        'fontSize',
        'color',
        'position-v',
        'formatter'
      ],
      'tooltip-selector': [
        'show',
        'trigger',
        'textStyle',
        'formatter'
      ],
      'x-axis-selector': [
        'axisForm',
        'show',
        'position',
        'name',
        'nameTextStyle',
        'splitLine',
        'axisLabel'
      ],
      'y-axis-selector': [
        'axisForm',
        'show',
        'position',
        'name',
        'nameTextStyle',
        'axisValue',
        'splitLine',
        'axisLabel'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ],
      'legend-selector': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
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
    ],
    propertyInner: {

      'color-selector': [
        'value',
        'custom',
        'alpha'
      ],
      'size-selector': [
        'funnelWidth'
      ],
      'label-selector': [
        'show',
        'fontSize',
        'color',
        'position-h',
        'formatter'
      ],
      'tooltip-selector': [
        'show',
        'trigger',
        'textStyle',
        'formatter'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ],
      'legend-selector': [
        'show',
        'icon',
        'orient',
        'textStyle',
        'hPosition',
        'vPosition'
      ]
    }
  },

  {
    render: 'echarts',
    category: 'chart.chart_type_space',
    value: 'map',
    title: 'chart.chart_map',
    icon: 'map_mini',
    properties: [
      'color-selector',
      'label-selector',
      'tooltip-selector',
      'title-selector',
      'suspension-selector',
      'condition-style-selector'
    ],
    propertyInner: {

      'color-selector': [
        'value',
        'custom',
        'alpha',
        'area-border-color',
        'gradient-color'
      ],
      'label-selector': [
        'show',
        'fontSize',
        'color',

        'label-bg',
        'label-show-shadow',
        'label-shadow-color'
      ],
      'tooltip-selector': [
        'show',
        'textStyle',
        'formatter'
      ],
      'title-selector': [
        'show',
        'title',
        'fontSize',
        'color',
        'hPosition',
        'vPosition',
        'isItalic',
        'isBolder'
      ],
      'suspension-selector': [
        'show'
      ],
      'condition-style-selector': [
        'show'
      ]
    }
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

export function customColor(custom, res, colors) {
  const result = []
  for (let i = 0; i < res.length; i++) {
    const r = res[i]
    let flag = false
    for (let j = 0; j < custom.length; j++) {
      const c = custom[j]
      if (r.name === c.name) {
        flag = true
        result.push(c)
      }
    }
    if (!flag) {
      result.push(r)
    }
  }
  return result
}

export function getColors(chart, colors, reset) {
  // 自定义颜色，先按照没有设定的情况，并排好序，当做最终结果
  let seriesColors = []
  let series
  if (chart.type.includes('stack')) {
    if (chart.data) {
      const data = chart.data.data
      const stackData = []
      for (let i = 0; i < data.length; i++) {
        const s = data[i]
        stackData.push(s.category)
      }
      const sArr = stackData.filter(function(item, index, stackData) {
        return stackData.indexOf(item, 0) === index
      })

      for (let i = 0; i < sArr.length; i++) {
        const s = sArr[i]
        seriesColors.push({
          name: s,
          color: colors[i % colors.length],
          isCustom: false
        })
      }
    }
  } else if (chart.type === 'bidirectional-bar') {
    const yaxis = JSON.parse(chart.yaxis)[0]
    const yaxisExt = JSON.parse(chart.yaxisExt)[0]
    if (yaxis) {
      seriesColors.push({
        name: yaxis.name,
        color: colors[0],
        isCustom: false
      })
    }
    if (yaxisExt) {
      seriesColors.push({
        name: yaxisExt.name,
        color: colors[1],
        isCustom: false
      })
    }
  } else if (includesAny(chart.type, 'bar', 'scatter', 'radar', 'area') && !chart.type.includes('group')) {
    if (Object.prototype.toString.call(chart.yaxis) === '[object Array]') {
      series = JSON.parse(JSON.stringify(chart.yaxis))
    } else {
      series = JSON.parse(chart.yaxis)
    }
    if (series) {
      for (let i = 0; i < series.length; i++) {
        const s = series[i]
        seriesColors.push({
          name: s.name,
          color: colors[i % colors.length],
          isCustom: false
        })
      }
    }
  } else if (equalsAny(chart.type, 'bar-group', 'line')) {
    // 拿到data中的category，并去重，然后构建seriesColor
    if (chart.data) {
      const data = chart.data.data
      const s = []
      data.forEach((cur) => {
        if (s.indexOf(cur.category) < 0) {
          s.push(cur.category)
        }
      })
      for (let i = 0; i < s.length; i++) {
        seriesColors.push({
          name: s[i],
          color: colors[i % colors.length],
          isCustom: false
        })
      }
    }
  } else {
    if (chart.data) {
      const data = chart.data.data
      // data 的维度值，需要根据自定义顺序排序
      // let customSortData
      // if (Object.prototype.toString.call(chart.customSort) === '[object Array]') {
      //   customSortData = JSON.parse(JSON.stringify(chart.customSort))
      // } else {
      //   customSortData = JSON.parse(chart.customSort)
      // }
      // if (customSortData && customSortData.length > 0) {
      //   data = customSort(customSortData, data)
      // }

      for (let i = 0; i < data.length; i++) {
        const s = data[i]
        seriesColors.push({
          name: s.field,
          color: colors[i % colors.length],
          isCustom: false
        })
      }
    }
  }
  // 如果有自定义，则与上述中的结果合并。
  // res，custom，以custom为准，去掉res中不存在的，并将custom中name一样的color赋值给res，不存在的name，即新增值，使用i % colors.length，从配色方案中选
  if (!reset) {
    let sc = null
    if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
      sc = JSON.parse(JSON.stringify(chart.customAttr)).color.seriesColors
    } else {
      sc = JSON.parse(chart.customAttr).color.seriesColors
    }
    if (sc && sc.length > 0) {
      seriesColors = customColor(sc, seriesColors)
    }
    // 根据isCustom字段，修正color
    for (let i = 0; i < seriesColors.length; i++) {
      if (!seriesColors[i].isCustom) {
        seriesColors[i].color = colors[i % colors.length]
      }
    }
  }
  return seriesColors
}

export function antVCustomColor(chart) {
  const colors = []
  if (chart.customAttr) {
    const customAttr = JSON.parse(chart.customAttr)
    // color
    if (customAttr.color) {
      const c = JSON.parse(JSON.stringify(customAttr.color))

      const customColors = getColors(chart, c.colors, false)
      for (let i = 0; i < customColors.length; i++) {
        colors.push(hexColorToRGBA(customColors[i].color, c.alpha))
      }
    }
  }
  return colors
}

export function getRemark(chart) {
  const remark = {}
  if (chart.customStyle) {
    const customStyle = JSON.parse(chart.customStyle)
    if (customStyle.text) {
      const title = JSON.parse(JSON.stringify(customStyle.text))
      remark.show = title.remarkShow ? title.remarkShow : DEFAULT_TITLE_STYLE.remarkShow
      remark.content = title.remark ? title.remark : DEFAULT_TITLE_STYLE.remark
      remark.bgFill = title.remarkBackgroundColor ? title.remarkBackgroundColor : DEFAULT_TITLE_STYLE.remarkBackgroundColor
    }
  }
  return remark
}

export const quotaViews = ['label', 'richTextView', 'text', 'gauge', 'liquid']

export function handleEmptyDataStrategy(strategy, chart, data, options) {
  if (!data?.length) {
    return
  }
  if (strategy === 'ignoreData') {
    handleIgnoreData(chart, data)
    return
  }
  const yaxis = JSON.parse(chart.yaxis)
  const extAxis = JSON.parse(chart.xaxisExt)
  const multiDimension = yaxis?.length >= 2 || extAxis?.length > 0
  switch (strategy) {
    case 'breakLine': {
      options.connectNulls = false
      if (multiDimension) {
        // 多维度线条断开
        handleBreakLineMultiDimension(chart, data)
      }
      break
    }
    case 'setZero': {
      if (multiDimension > 0) {
        // 多维度置0
        handleSetZeroMultiDimension(chart, data, options)
      } else {
        // 单维度置0
        handleSetZeroSingleDimension(chart, data, options)
      }
      break
    }
    default:
      break
  }
}

function handleBreakLineMultiDimension(chart, data) {
  const dimensionInfoMap = new Map()
  const subDimensionSet = new Set()
  for (let i = 0; i < data.length; i++) {
    const item = data[i]
    const dimensionInfo = dimensionInfoMap.get(item.field)
    if (dimensionInfo) {
      dimensionInfo.set.add(item.category)
    } else {
      dimensionInfoMap.set(item.field, { set: new Set([item.category]), index: i })
    }
    subDimensionSet.add(item.category)
  }
  // Map 是按照插入顺序排序的，所以插入索引往后推
  let insertCount = 0
  dimensionInfoMap.forEach((dimensionInfo, field) => {
    if (dimensionInfo.set.size < subDimensionSet.size) {
      let subInsertIndex = 0
      subDimensionSet.forEach(dimension => {
        if (!dimensionInfo.set.has(dimension)) {
          data.splice(dimensionInfo.index + insertCount + subInsertIndex, 0, {
            field,
            value: null,
            category: dimension
          })
        }
        subInsertIndex++
      })
      insertCount += subDimensionSet.size - dimensionInfo.set.size
    }
  })
}

function handleSetZeroMultiDimension(chart, data) {
  const dimensionInfoMap = new Map()
  const subDimensionSet = new Set()
  for (let i = 0; i < data.length; i++) {
    const item = data[i]
    if (item.value === null) {
      item.value = 0
    }
    const dimensionInfo = dimensionInfoMap.get(item.field)
    if (dimensionInfo) {
      dimensionInfo.set.add(item.category)
    } else {
      dimensionInfoMap.set(item.field, { set: new Set([item.category]), index: i })
    }
    subDimensionSet.add(item.category)
  }
  let insertCount = 0
  dimensionInfoMap.forEach((dimensionInfo, field) => {
    if (dimensionInfo.set.size < subDimensionSet.size) {
      let subInsertIndex = 0
      subDimensionSet.forEach(dimension => {
        if (!dimensionInfo.set.has(dimension)) {
          data.splice(dimensionInfo.index + insertCount + subInsertIndex, 0, {
            field,
            value: 0,
            category: dimension
          })
        }
        subInsertIndex++
      })
      insertCount += subDimensionSet.size - dimensionInfo.set.size
    }
  })
}

function handleSetZeroSingleDimension(chart, data) {
  data.forEach(item => {
    if (item.value === null) {
      item.value = 0
    }
  })
}

function handleIgnoreData(chart, data) {
  for (let i = data.length - 1; i >= 0; i--) {
    const item = data[i]
    if (item.value === null) {
      data.splice(i, 1)
    }
  }
}

export function resetRgbOpacity(sourceColor, times) {
  if (sourceColor?.startsWith('rgb')) {
    const numbers = sourceColor.match(/(\d(\.\d+)?)+/g)
    if (numbers?.length === 4) {
      const opacity = parseFloat(numbers[3])
      if (_.isNumber(opacity)) {
        let resultOpacity = (opacity * times).toFixed(2)
        if (resultOpacity > 1) {
          resultOpacity = 1
        }
        const colorArr = numbers.slice(0, 3).concat(resultOpacity)
        return `rgba(${colorArr.join(',')})`
      }
    }
  }
  return sourceColor
}

export function handleTableEmptyStrategy(tableData, chart) {
  let newData = tableData
  let intersection = []
  let senior = chart.senior
  if (senior) {
    senior = JSON.parse(senior)
  }
  let emptyDataStrategy = senior?.functionCfg?.emptyDataStrategy
  if (!emptyDataStrategy) {
    emptyDataStrategy = 'breakLine'
  }
  const emptyDataFieldCtrl = senior?.functionCfg?.emptyDataFieldCtrl
  if (emptyDataStrategy !== 'breakLine' && emptyDataFieldCtrl?.length && tableData?.length) {
    const deNames = _.keys(tableData[0])
    intersection = _.intersection(deNames, emptyDataFieldCtrl)
  }
  if (intersection.length) {
    newData = _.clone(tableData)
    for (let i = 0; i < newData.length; i++) {
      for (let j = 0, tmp = intersection.length; j < tmp; j++) {
        const deName = intersection[j]
        if (newData[i][deName] === null) {
          if (emptyDataStrategy === 'setZero') {
            newData[i][deName] = 0
          }
          if (emptyDataStrategy === 'ignoreData') {
            newData = _.filter(newData, (_, index) => index !== i)
          }
        }
      }
    }
  }
  return newData
}
