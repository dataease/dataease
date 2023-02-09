<template>
  <div :style="chartSize">
    <div id="main" :style="chartSizeMax"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  getDatasourceRelationship,
  getDatasetRelationship,
  getPanelRelationship
} from '@/api/chart/chart.js'
export default {
  props: {
    chartSize: {
      type: Object,
      default: () => {}
    },
  },
  data() {
    return {
      measureText: null,
      current: {},
      maxSize: 0,
      activeId: '',
      treeData: [],
      myChart: null,
      datasourcePanel: {
        datasource: 'panel',
        panel: 'datasource',
        dataset: 'panel'
      }
    }
  },
  computed: {
    chartSizeMax() {
      const { height } = this.chartSize
      return this.maxSize > parseInt(height)/25  ? {
        height: this.maxSize * 25  + 'px',
        width: this.chartSize.width
      } : this.chartSize
    }
  },
  watch: {
    chartSize: {
      handler() {
        this.initEchart()
      },
      deep: true
    },
    chartSizeMax: {
      handler() {
        setTimeout(() => {
          this.initEchart()
        }, 1000)
      },
      deep: true
    }
  },
  methods: {
    getChartData(current) {
      this.current = { ...current }
      const { queryType, num: id } = current
      this.activeId = id
      switch (queryType) {
        case 'datasource':
          this.getDatasourceRelationship(id)
          break
        case 'dataset':
          this.getDatasetRelationship(id)
          break
        case 'panel': {
          let targetId = id
          if (current.nodeData?.source) {
            targetId = current.nodeData.source
          }
          this.getPanelRelationship(targetId)
          break
        }
        default:
          break
      }
    },
    getDatasourceRelationship(id) {
      getDatasourceRelationship(id).then((res) => {
        const arr = res.data || []
        this.treeData = []
        this.dfsTree(arr, this.current.num)
        this.initEchart()
      })
    },
    getDatasetRelationship(id) {
      getDatasetRelationship(id).then((res) => {
        const { id, name } = res.data
        res.data.id = this.current.num
        res.data.name = this.current.label
        res.data.type = this.current.queryType
        const arr = res.data ? [res.data] : []
        if (id) {
          this.current = { num: id, label: name, queryType: 'datasource' }
        }
        this.treeData = []
        this.dfsTree(arr, id)
        this.initEchart()
      })
    },
    getPanelRelationship(id) {
      getPanelRelationship(id).then((res) => {
        const arr = res.data || []
        this.treeData = []
        this.dfsTreeFlip(arr)
        this.initEchart()
      })
    },
    dfsTreeFlip(arr = [], obj) {
      arr.forEach((ele) => {
        const { id, name, type, subRelation = [] } = ele
        if (subRelation.length) {
          this.dfsTreeFlip(subRelation, { id, name })
        } else if (type === 'dataset') {
          this.treeData.push({ id, name, type, pid: this.current.num })
          if (obj.id) {
            this.treeData.push({
              id: obj.id,
              name: obj.name,
              type: 'datasource',
              pid: id
            })
          }
        }
      })
    },
    dfsTree(arr = [], pid = 0) {
      arr.forEach((ele) => {
        const { id, name, type, subRelation = [] } = ele
        this.treeData.push({ id, name, type, pid })
        if (subRelation.length) {
          this.dfsTree(subRelation, id)
        }
      })
    },
    deleteRepeat(arr = []) {
      let list = JSON.parse(JSON.stringify(arr))
      const repeatPanel = {}
      list.forEach((ele) => {
        if (ele[6] === this.datasourcePanel[this.current.queryType]) {
          if (repeatPanel[ele[4]]) {
            repeatPanel[ele[4]].push(ele[0])
          } else {
            repeatPanel[ele[4]] = [ele[0]]
          }
        }
      })

      Object.keys(repeatPanel).forEach((ele) => {
        if (repeatPanel[ele].length === 1) {
          repeatPanel[ele] = undefined
          return
        }
        repeatPanel[ele] = repeatPanel[ele].sort((a, b) => {
          return a - b
        })[Math.floor(repeatPanel[ele].length / 2)]
      })
      return list.filter((ele) => {
        if (repeatPanel[ele[4]] === undefined) return true
        return repeatPanel[ele[4]] === ele[0]
      })
    },
    calculatedLine(arr = []) {
      if (!arr.length || arr.length === 1) return
      const repeatPanel = {}
      const dataItemListMap = {}
      const list = []
      const root = arr.find((ele) => ele[6] === this.current.queryType)
      arr.forEach((ele) => {
        const [index, start, end, width, id, name, type, pid] = ele
        dataItemListMap[id] = {
          index,
          start,
          end,
          width,
          id,
          name,
          type,
          pid
        }

        if (type === 'dataset' && this.current.queryType !== 'dataset') {
          list.push([root[0], root[1], start, index, root[3], type, id])
        }
      })

      arr.forEach((ele) => {
        const [index, start, end, width, id, name, type, pid] = ele
        if (type === this.datasourcePanel[this.current.queryType]) {
          let dataset = dataItemListMap[pid]
          if (repeatPanel[id]) {
            repeatPanel[id].push({ index, start })
          } else {
            repeatPanel[id] = [{ index, start }]
          }
          list.push([
            dataset.index,
            dataset.start,
            start,
            index,
            dataset.width,
            type,
            id
          ])
        }
      })

      Object.keys(repeatPanel).forEach((ele) => {
        if (repeatPanel[ele].length === 1) {
          repeatPanel[ele] = null
          return
        }
        repeatPanel[ele] = repeatPanel[ele].sort((a, b) => {
          return a.index - b.index
        })[Math.floor(repeatPanel[ele].length / 2)]
      })

      list.forEach((ele) => {
        if (!repeatPanel[ele[6]]) return
        const { start, index } = repeatPanel[ele[6]]
        ele[2] = start
        ele[3] = index
      })

      return list
    },
    calculatedWidth(arr = []) {
      const c = document.createElement('canvas')
      const ctx = c.getContext('2d')
      ctx.font = '14px Arial'
      const dataItemList = []
      let list = []
      const repeatPanel = {}

      let max = {
        dataset: 0,
        datasource: 0,
        panel: 0
      }

      const { queryType, num, label } = this.current
      max[queryType] = ctx.measureText(label).width + 30

      if (!arr.length)
        return [
          [[0, 0, max[queryType], max[queryType], num, label, queryType, 0]],
          {
            dataset: 0,
            datasource: 0,
            panel: 0
          }
        ]

      arr.forEach((ele, index) => {
        const { id, name, type, pid } = ele
        let width = ctx.measureText(name).width + 30
        max[type] = Math.max(width, max[type])
        dataItemList.push([width, id, name, type, pid])
      })
      let inserted = false

      dataItemList.forEach((ele, index) => {
        if (index === Math.floor(dataItemList.length / 2)) {
          inserted = true
          list.push([
            index,
            0,
            max[queryType],
            max[queryType],
            num,
            label,
            queryType,
            0
          ])
        }
        const [width, id, name, type, pid] = ele
        if (type === 'dataset' && this.current.queryType !== 'dataset') {
          list.push([
            inserted ? index + 1 : index,
            max[this.current.queryType],
            max[this.current.queryType] + width,
            width,
            id,
            name,
            type,
            pid
          ])
        }

        if (type === 'panel' && this.current.queryType !== 'panel') {
          list.push([
            inserted ? index + 1 : index,
            max.dataset + max.datasource,
            max.dataset + max.datasource + width,
            width,
            id,
            name,
            type,
            pid
          ])
        }

        if (type === 'datasource' && this.current.queryType !== 'datasource') {
          list.push([
            inserted ? index + 1 : index,
            max.dataset + max.panel,
            max.dataset + max.panel + width,
            width,
            id,
            name,
            type,
            pid
          ])
        }
      })

      function maxGap(source, target) {
        return Math.min(
          Math.max(
            list.filter((ele) => ele[6] === source).length * 5,
            max[target] / 2,
            30
          ),
          30
        )
      }
      let gap = {
        dataset: maxGap('dataset', 'datasource'),
        datasource: maxGap('datasource', 'datasource'),
        panel: maxGap('panel', 'dataset')
      }
      return [list, gap]
    },
    initEchart(clickNode = []) {
      if (this.myChart) {
        this.myChart.dispose()
        this.myChart = null
      }

      this.myChart = echarts.init(document.getElementById('main'))
      const that = this
      let [data, gap] = this.calculatedWidth(this.treeData)
      const gapDetail = {
        dataset: gap.dataset,
        datasource: gap.dataset + gap.panel,
        panel: gap.panel + gap.dataset
      }

      gapDetail[this.current.queryType] = 0
      let lineData = this.calculatedLine(data)
      data = this.deleteRepeat(data)

      const lineEnd = [clickNode[0]]
      lineData = lineData.map(ele => {
        let arr = [...ele]
        if (clickNode[0] === ele[0]) {
          arr.push(1)
          lineEnd.push(ele[3])
        } else if(clickNode[0] === ele[3]) {
          arr.push(1)
          lineEnd.push(ele[0])
        } else {
          arr.push(0)
        }
        return arr
      })

      data = data.map(ele => {
        let arr = [...ele]
        arr.push(Boolean(lineEnd.includes(ele[0])))
        return arr
      })
      
      if (this.maxSize !== data.length) {
        this.maxSize = data.length
        return
      }
      let option = {
        xAxis: {
          show: false, //不显示分隔线,
          splitLine: {
            show: false //不显示分隔线
          }
        },
        grid: {
          top: 10,
          bottom: 10,
          left: '5%'
        },
        tooltip: {
          show: true,
          trigger: 'item',
          formatter: (a) => {
            return a.value[5]
          }
        },
        yAxis: {
          show: false,
          type: 'category',
          splitLine: {
            show: false //不显示分隔线
          }
        },
        series: [
          {
            type: 'custom',
            encode: {
              // data 中『维度1』和『维度2』对应到 X 轴
              x: [1, 2],
              // data 中『维度0』对应到 Y 轴
              y: 0
            },
            roam: true,
            renderItem: function (params, api) {
              let categoryIndex = api.value(0)
              let startPoint = api.coord([api.value(1), categoryIndex])
              let width = api.value(3)
              let height = 22

              const imageType = {
                datasource: 'data:image/png;base64,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',
                panel: 'data:image/png;base64,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',
                dataset: 'data:image/png;base64,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',
              }

              
              return {
                type: 'group', //当需要多个自定义拼接时，需要用group，此案例是文字和图形的拼接
                children: [
                  {
                    type: 'text',
                    position: [
                      startPoint[0] + gapDetail[api.value(6)],
                      startPoint[1] - height / 2
                    ], //相对位置
                    z2: 10,
                    style: {
                      text: isNaN(api.value(5)) ? data.find(ele => ele[4] === api.value(4))[5] : api.value(5), //data中取值
                      color: '#1F2329',
                      x: 25,
                      y: 5
                    }
                  },
                  {
                    type: 'image',
                    x: startPoint[0] + gapDetail[api.value(6)],
                    y: startPoint[1] - height / 2,
                    z2: 20,
                    style: {
                      image: imageType[api.value(6)],
                      width: 15,
                      height: 15,
                      x: 5,
                      y: 3.5
                    }
                  },
                  {
                    // 表示这个图形元素是矩形。还可以是 'circle', 'sector', 'polygon' 等等。
                    type: 'rect',
                    z2: 2,
                    // shape 属性描述了这个矩形的像素位置和大小。
                    // 其中特殊得用到了 echarts.graphic.clipRectByRect，意思是，
                    // 如果矩形超出了当前坐标系的包围盒，则剪裁这个矩形。
                    shape: echarts.graphic.clipRectByRect(
                      {
                        // 矩形的位置和大小。
                        x: startPoint[0] + gapDetail[api.value(6)],
                        y: startPoint[1] - height / 2,
                        width,
                        height: height
                      },
                      {
                        // 当前坐标系的包围盒。
                        x: params.coordSys.x,
                        y: params.coordSys.y,
                        width: params.coordSys.width,
                        height: params.coordSys.height
                      }
                    ),
                    style: {
                      ...api.style(),
                      fill: api.value(4) === that.activeId ? '#c2d4ff' : 'none',
                      stroke: api.value(8) ? 'green' : '#3370FF'
                    }
                  }
                ]
              }
            },
            data
          },
          {
            type: 'custom',
            encode: {
              x: [1, 2],
              y: [0, 3]
            },
            roam: true,
            renderItem: function (params, api) {
              let categoryIndex = api.value(0)
              let categoryIndex2 = api.value(3)
              let startPoint = api.coord([api.value(1), categoryIndex])
              let endPoint = api.coord([api.value(2), categoryIndex2])
              let height = 20

              function startPointX1() {
                if (api.value(5) === 'dataset') {
                  return startPoint[0] + api.value(4)
                }

                if (
                  api.value(5) === that.datasourcePanel[that.current.queryType]
                ) {
                  return startPoint[0] + api.value(4) + gapDetail.dataset
                }

                if (api.value(5) === that.current.queryType) {
                  return 0
                }
              }

              let x1 = startPointX1()

              return {
                type: 'group', //当需要多个自定义拼接时，需要用group，此案例是文字和图形的拼接
                children: [
                  {
                    type: 'bezierCurve',
                    silent: true,
                    shape: {
                      x1,
                      y1: startPoint[1],
                      x2: endPoint[0] + gapDetail[api.value(5)],
                      y2: endPoint[1],
                      cpx1:endPoint[0] + gapDetail[api.value(5)] - 50,
                      cpx2:endPoint[0] + gapDetail[api.value(5)] - 10,
                      cpy1:endPoint[1],
                      cpy2:endPoint[1],
                      percent: 1
                    },
                    style: {
                      stroke: api.value(7) ? 'green' : '#3370FF',
                      fill: 'transparent',
                      lineWidth: 1
                    }
                  }
                ]
              }
            },
            data: lineData
          }
        ]
      }
      this.myChart.setOption(option, true)
      this.myChart.on('click', function (params) {
          that.initEchart(params.value)
      });
    }
  }
}
</script>
