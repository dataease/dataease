import { Column } from '@antv/g2plot/lib/plots/column'
import _ from 'lodash'

const data = [
  {
    type: '家具家电',
    sales: 38
  },
  {
    type: '粮油副食',
    sales: 52
  },
  {
    type: '生鲜水果',
    sales: 61
  },
  {
    type: '美容洗护',
    sales: 145
  },
  {
    type: '母婴用品',
    sales: 48
  },
  {
    type: '进口食品',
    sales: 38
  },
  {
    type: '食品饮料',
    sales: 38
  },
  {
    type: '家庭清洁',
    sales: 38
  }
]

export function baseBarOption(plot, container, chart) {
  if (chart) {
    // data
    const data = _.cloneDeep(chart?.data?.data)
    if (!data) return plot
    const options = {
      data: data,
      xField: 'field',
      yField: 'value',
      seriesField: 'category',
      interactions: [
        {
          type: 'legend-active',
          cfg: {
            start: [{ trigger: 'legend-item:mouseenter', action: ['element-active:reset'] }],
            end: [{ trigger: 'legend-item:mouseleave', action: ['element-active:reset'] }]
          }
        },
        {
          type: 'legend-filter',
          cfg: {
            start: [
              {
                trigger: 'legend-item:click',
                action: [
                  'list-unchecked:toggle',
                  'data-filter:filter',
                  'element-active:reset',
                  'element-highlight:reset'
                ]
              }
            ]
          }
        },
        {
          type: 'tooltip',
          cfg: {
            start: [{ trigger: 'interval:mousemove', action: 'tooltip:show' }],
            end: [{ trigger: 'interval:mouseleave', action: 'tooltip:hide' }]
          }
        },
        {
          type: 'active-region',
          cfg: {
            start: [{ trigger: 'interval:mousemove', action: 'active-region:show' }],
            end: [{ trigger: 'interval:mouseleave', action: 'active-region:hide' }]
          }
        }
      ]
    }

    if (plot) {
      plot.destroy()
    }
    plot = new Column(container, options)
    return plot
  } else {
    const options = {
      data: data,
      xField: 'type',
      yField: 'sales',
      label: {
        // 可手动配置 label 数据标签位置
        position: 'middle', // 'top', 'bottom', 'middle',
        // 配置样式
        style: {
          fill: '#FFFFFF',
          opacity: 0.6
        }
      },
      xAxis: {
        label: {
          autoHide: true,
          autoRotate: false
        }
      },
      meta: {
        type: {
          alias: '类别'
        },
        sales: {
          alias: '销售额'
        }
      }
    }

    if (plot) {
      plot.destroy()
    }
    plot = new Column(container, options)
    return plot
  }
}
