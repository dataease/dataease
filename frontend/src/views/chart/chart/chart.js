export const DEFAULT_COLOR_CASE = {
  value: 'default',
  colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'],
  alpha: 100
}
export const DEFAULT_SIZE = {
  barDefault: true,
  barWidth: 40,
  barGap: 0.4,
  lineWidth: 1,
  lineType: 'solid',
  lineSymbol: 'emptyCircle',
  lineSymbolSize: 4,
  lineSmooth: false,
  lineArea: false,
  pieInnerRadius: 0,
  pieOuterRadius: 60,
  funnelWidth: 80,
  radarShape: 'polygon'
}
export const DEFAULT_LABEL = {
  show: false,
  position: 'top',
  color: '#909399',
  fontSize: '10',
  formatter: '{c}'
}
export const DEFAULT_TOOLTIP = {
  show: true,
  trigger: 'item',
  textStyle: {
    fontSize: '10',
    color: '#909399'
  },
  formatter: ''
}
export const DEFAULT_TITLE_STYLE = {
  show: true,
  fontSize: '18',
  color: '#303133',
  hPosition: 'center',
  vPosition: 'top',
  isItalic: false
}
export const DEFAULT_LEGEND_STYLE = {
  show: true,
  hPosition: 'center',
  vPosition: 'bottom',
  orient: 'horizontal',
  icon: 'rect',
  textStyle: {
    color: '#333333',
    fontSize: '12'
  }
}
export const DEFAULT_XAXIS_STYLE = {
  show: true,
  position: 'bottom',
  name: '',
  axisLabel: {
    rotate: 0,
    formatter: '{value}'
  }
}
export const DEFAULT_YAXIS_STYLE = {
  show: true,
  position: 'left',
  name: '',
  axisLabel: {
    rotate: 0,
    formatter: '{value}'
  }
}
// chart config
export const BASE_BAR = {
  title: {
    text: ''
  },
  tooltip: {},
  legend: {
    show: true,
    type: 'scroll',
    itemWidth: 10,
    itemHeight: 10,
    icon: 'rect',
    data: []
  },
  xAxis: {
    data: []
  },
  yAxis: {
    type: 'value'
  },
  series: [],
  dataZoom: [
    {
      type: 'inside'
    }
  ]
}
export const HORIZONTAL_BAR = {
  title: {
    text: ''
  },
  tooltip: {},
  legend: {
    show: true,
    type: 'scroll',
    itemWidth: 10,
    itemHeight: 10,
    icon: 'rect',
    data: []
  },
  xAxis: {
    type: 'value'
  },
  yAxis: {
    data: []
  },
  series: [],
  dataZoom: [
    {
      type: 'inside'
    }
  ]
}

export const BASE_LINE = {
  title: {
    text: ''
  },
  tooltip: {},
  legend: {
    show: true,
    type: 'scroll',
    itemWidth: 10,
    itemHeight: 10,
    icon: 'rect',
    data: []
  },
  xAxis: {
    boundaryGap: false,
    data: []
  },
  yAxis: {
    type: 'value'
  },
  series: [],
  dataZoom: [
    {
      type: 'inside'
    }
  ]
}

export const BASE_PIE = {
  title: {
    text: ''
  },
  tooltip: {},
  legend: {
    show: true,
    type: 'scroll',
    itemWidth: 10,
    itemHeight: 10,
    icon: 'rect'
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['0%', '60%'],
      avoidLabelOverlap: false,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      data: []
    }
  ]
}

export const BASE_FUNNEL = {
  title: {
    text: ''
  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    show: true,
    type: 'scroll',
    itemWidth: 10,
    itemHeight: 10,
    icon: 'rect'
  },
  series: [
    {
      name: '',
      type: 'funnel',
      left: 'center',
      top: 60,
      bottom: 60,
      width: '80%',
      min: 0,
      max: 100,
      minSize: '0%',
      maxSize: '100%',
      sort: 'descending',
      gap: 1,
      // label: {
      //   show: true,
      //   position: 'inside'
      // },
      labelLine: {
        length: 10,
        lineStyle: {
          width: 1,
          type: 'solid'
        }
      },
      itemStyle: {
        borderColor: '#fff',
        borderWidth: 1
      },
      emphasis: {
        label: {
          fontSize: 20
        }
      },
      data: []
    }
  ]
}

export const BASE_RADAR = {
  title: {
    text: ''
  },
  tooltip: {},
  legend: {
    show: true,
    type: 'scroll',
    itemWidth: 10,
    itemHeight: 10,
    icon: 'rect',
    data: []
  },
  radar: {
    shape: 'polygon',
    name: {
      textStyle: {
        color: '#000000'
        // backgroundColor: '#999',
        // borderRadius: 3,
        // padding: [3, 5]
      }
    },
    indicator: []
  },
  series: [{
    type: 'radar',
    // areaStyle: {normal: {}},
    data: []
  }]
}
