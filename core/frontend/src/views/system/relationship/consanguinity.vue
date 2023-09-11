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
      if (!this.treeData.length) return
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
                datasource: '/static/svg/de-datasource.svg',
                panel: '/static/svg/de-dashboard.svg',
                dataset: '/static/svg/de-dataset.svg'
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
