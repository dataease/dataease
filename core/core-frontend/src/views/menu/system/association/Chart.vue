<script lang="tsx" setup>
import { computed, watch } from 'vue'
import { getCSSVariable } from '@/utils/color'
import { cloneDeep } from 'lodash-es'
import * as echarts from 'echarts'
const props = defineProps({
  chartSize: {
    type: Object,
    default: () => {}
  },
  detailDisabled: {
    type: Boolean,
    default: false
  }
})

interface Current {
  queryType: string
  num: string
  label: string
  nodeData?: {
    source: string
  }
}
let current: Current = {
  queryType: '',
  num: '0',
  label: '',
  nodeData: {
    source: '0'
  }
}
let maxSize = 0
let activeId = '0'
let activeNode = null
let treeData = []
let myChart = null
const canvasVal = document.createElement('canvas')
const contextVal = canvasVal.getContext('2d')
contextVal!.font =
  '14px -apple-system,BlinkMacSystemFont,Segoe UI,Roboto,PingFang SC,Helvetica Neue,Noto Sans,Noto Sans CJK SC,Microsoft Yahei,Arial,Hiragino Sans GB,sans-serif'
const datasourcePanel = {
  datasource: ['dashboard', 'dataV'],
  dashboard: ['datasource', 'dataset'],
  dataV: ['datasource', 'dataset'],
  dataset: ['dashboard', 'dataV']
}

const chartSizeMax = computed(() => {
  const { height, width } = props.chartSize
  return maxSize > parseInt(height) / 25
    ? {
        height: maxSize * 25 + 'px',
        width
      }
    : props.chartSize
})

watch(
  () => props.chartSize,
  () => {
    initEchart()
  },
  { deep: true }
)

watch(
  () => chartSizeMax.value,
  () => {
    setTimeout(() => {
      initEchart()
    }, 1000)
  },
  { deep: true }
)
let clearNode = false
const reRender = val => {
  clearNode = true
  getChartData(val)
}
function svgToDataURI(svgString) {
  const parser = new DOMParser()
  const doc = parser.parseFromString(svgString, 'image/svg+xml')
  const svgXml = new XMLSerializer().serializeToString(doc.documentElement)
  return `data:image/svg+xml;base64,${btoa(svgXml)}`
}

const getChartData = ({ info, res }) => {
  if (!clearNode) {
    activeNode = null
  }
  clearNode = false
  current = cloneDeep(info)
  const { queryType, num: id } = current
  activeId = `_${activeNode ? activeNode.id : id}_`
  switch (queryType) {
    case 'datasource':
      getDatasourceRelationship(res)
      break
    case 'dataset':
      getDatasetRelationship(id, res)
      break
    case 'dataV':
    case 'dashboard': {
      getPanelRelationship(res)
      break
    }
    default:
      break
  }
}
const getDatasourceRelationship = res => {
  const arr = res?.data?.list || []
  treeData = []
  dfsTree(arr, `_${current.num}_`)
  initEchart()
}
const getDatasetRelationship = (id, res) => {
  const { dsList, dvList } = res.data
  treeData = []
  dfsTreeDataset(dvList, `_${id}_`)
  dsList.forEach(ele => {
    const { name } = ele
    treeData.push({ id: `_${ele.id}_`, name, type: 'datasource', pid: '0' })
  })
  treeData.push({
    id: `_${current.num}_`,
    name: current.label,
    type: 'dataset',
    pid: dsList[0]?.id ? `_${dsList[0]?.id}_` : '0'
  })

  if (dsList[0]) {
    current = {
      num: dsList[0]?.id,
      label: dsList[0]?.name,
      queryType: 'datasource'
    }
  }
  initEchart()
}
const getPanelRelationship = res => {
  const arr = res?.data?.list || []
  treeData = []
  dfsTreeFlip(arr, {})
  initEchart()
}
const dfsTreeFlip = (arr = [], obj) => {
  arr.forEach(ele => {
    const { id, name, type, subRelation = [] } = ele
    if (subRelation?.length) {
      dfsTreeFlip(subRelation, { id: `_${id}_`, name })
    } else if (type === 'dataset') {
      treeData.push({ id: `_${id}_`, name, type, pid: `_${current.num}_` })
      if (obj.id) {
        treeData.push({
          id: obj.id,
          name: obj.name,
          type: 'datasource',
          pid: `_${id}_`
        })
      }
    }
  })
}
const dfsTree = (arr = [], pid = '0') => {
  arr.forEach(ele => {
    const { id, name, type, subRelation = [] } = ele
    treeData.push({ id: `_${id}_`, name, type, pid })
    if (subRelation?.length) {
      dfsTree(subRelation, `_${id}_`)
    }
  })
}

const dfsTreeDataset = (arr = [], pid = '0') => {
  arr.forEach(ele => {
    const { id, name, type } = ele
    treeData.push({ id: `_${id}_`, name, type, pid })
  })
}
const deleteRepeat = (arr = []) => {
  const list = cloneDeep(arr)
  const repeatPanel = {}
  list.forEach(ele => {
    if (datasourcePanel[current.queryType].includes(ele[6])) {
      if (repeatPanel[ele[4]]) {
        repeatPanel[ele[4]].push(ele[0])
      } else {
        repeatPanel[ele[4]] = [ele[0]]
      }
    }
  })

  Object.keys(repeatPanel).forEach(ele => {
    if (repeatPanel[ele].length === 1) {
      repeatPanel[ele] = undefined
      return
    }
    repeatPanel[ele] = repeatPanel[ele].sort((a, b) => {
      return a - b
    })[Math.floor(repeatPanel[ele].length / 2)]
  })

  return list.filter(ele => {
    if (repeatPanel[ele[4]] === undefined) return true
    return repeatPanel[ele[4]] === ele[0]
  })
}
const calculatedLine = (arr = []) => {
  if (!arr.length || arr.length === 1) return
  const repeatPanel = {}
  const dataItemListMap = {}
  const list = []
  const rootArr = arr.filter(ele => ele[6] === current.queryType)
  arr.forEach(ele => {
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
    rootArr.forEach(root => {
      if (type === 'dataset' && current.queryType !== 'dataset' && root) {
        list.push([root[0], root[1], start, index, root[3], type, id])
      }
    })
  })

  arr.forEach(ele => {
    // eslint-disable-next-line
    const [index, start, end, width, id, name, type, pid] = ele
    if (datasourcePanel[current.queryType].includes(type)) {
      const dataset = dataItemListMap[pid]
      if (repeatPanel[id]) {
        repeatPanel[id].push({ index, start })
      } else {
        repeatPanel[id] = [{ index, start }]
      }
      list.push([dataset.index, dataset.start, start, index, dataset.width, type, id])
    }
  })

  Object.keys(repeatPanel).forEach(ele => {
    if (repeatPanel[ele].length === 1) {
      repeatPanel[ele] = null
      return
    }
    repeatPanel[ele] = repeatPanel[ele].sort((a, b) => {
      return a.index - b.index
    })[Math.floor(repeatPanel[ele].length / 2)]
  })

  list.forEach(ele => {
    if (!repeatPanel[ele[6]]) return
    const { start, index } = repeatPanel[ele[6]]
    ele[2] = start
    ele[3] = index
  })
  return list
}
const calculatedWidth = (arr = []) => {
  const dataItemList = []
  const list = []

  const max = {
    dataset: 250,
    datasource: 250,
    dashboard: 250,
    dataV: 250
  }

  const { queryType, num, label } = current

  if (!arr.length) {
    return [
      [[0, 0, max[queryType], max[queryType], num, label, queryType, '0']],
      {
        dataset: 0,
        datasource: 0,
        dashboard: 0,
        dataV: 0
      }
    ]
  }

  arr.forEach(ele => {
    const { id, name, type, pid } = ele
    dataItemList.push([250, id, name, type, pid])
  })
  let inserted = false
  dataItemList.forEach((ele, index) => {
    const [width, id, name, type, pid] = ele

    if (
      index === Math.floor(dataItemList.length / 2) &&
      !(current.queryType === 'datasource' && ele[4] === '0')
    ) {
      inserted = true
      if (!list.find(item => label === item[5] && queryType === item[6])) {
        list.push([index, 0, max[queryType], max[queryType], `_${num}_`, label, queryType, 0])
      }

      if (index === 1 && type === 'datasource') {
        list.push([
          inserted ? index + 1 : index,
          max.dataset + Math.max(max.dashboard, max.dataV),
          max.dataset + Math.max(max.dashboard, max.dataV) + width,
          width,
          id,
          name,
          type,
          pid
        ])
      }
    } else if (current.queryType === 'datasource' && ele[4] === '0') {
      if (!list.find(item => name === item[5] && type === item[6])) {
        list.push([index, 0, max[queryType], max[queryType], id, name, type, index])
      }
    } else if (type === 'datasource' && current.queryType !== 'datasource') {
      list.push([
        inserted ? index + 1 : index,
        max.dataset + Math.max(max.dashboard, max.dataV),
        max.dataset + Math.max(max.dashboard, max.dataV) + width,
        width,
        id,
        name,
        type,
        pid
      ])
    }

    if (type === 'dataset' && current.queryType !== 'dataset') {
      list.push([
        inserted ? index + 1 : index,
        max[current.queryType],
        max[current.queryType] + width,
        width,
        id,
        name,
        type,
        pid
      ])
    }

    if (
      ['dashboard', 'dataV'].includes(type) &&
      !['dashboard', 'dataV'].includes(current.queryType)
    ) {
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
  })

  function maxGap(source, target) {
    return Math.min(
      Math.max(list.filter(ele => ele[6] === source).length * 5, max[target] / 2, 30),
      30
    )
  }
  const gap = {
    dataset: maxGap('dataset', 'datasource'),
    datasource: maxGap('datasource', 'datasource'),
    dashboard: maxGap('dashboard', 'dataset'),
    dataV: maxGap('dataV', 'dataset')
  }

  return [list, gap]
}

const initEchart = (clickNode = []) => {
  if (current.num === '0') return
  treeData = treeData.filter(ele => !!ele.name)
  if (!treeData.length) {
    const { num, queryType, label } = current
    if (!!num && !!queryType && !!label) {
      treeData.push({ id: `_${num}_`, name: label, type: queryType, pid: '0' })
    }
  }
  if (myChart) {
    myChart.dispose()
    myChart = null
  }

  if (clickNode.length) {
    activeId = `${clickNode[4]}`
  }

  myChart = echarts.init(document.getElementById('main'), null, {
    renderer: 'svg'
  })
  // eslint-disable-next-line
  let [data, gap] = calculatedWidth(treeData)
  const gapDetail = {
    dataset: gap.dataset,
    datasource: gap.dataset + Math.max(gap.dashboard, gap.dataV),
    dashboard: gap.dashboard + gap.dataset,
    dataV: gap.dataV + gap.dataset
  }

  gapDetail[current.queryType] = 0
  let lineData = data?.length > 1 ? calculatedLine(data) : []
  data = deleteRepeat(data)
  let lineEnd = [clickNode[0]]

  if (!clickNode.length && activeNode) {
    clickNode = [activeNode.index]
    lineEnd.splice(0, 1, activeNode.index)
  }
  const pidArr = []
  const idArr = []

  function dfsId(activeIndex) {
    lineData?.forEach(ele => {
      if (activeIndex === ele[0]) {
        pidArr.push(ele[3])
      } else if (activeIndex === ele[3]) {
        idArr.push(ele[0])
      }
    })
  }

  dfsId(clickNode[0])
  if (pidArr.length) {
    lineData?.forEach(ele => {
      if (pidArr[0] === ele[0]) {
        pidArr.push(ele[3])
      }
    })
  }

  if (idArr.length) {
    lineData?.forEach(ele => {
      if (idArr[0] === ele[3]) {
        idArr.push(ele[0])
      }
    })
  }

  lineEnd = [...new Set([...lineEnd, ...idArr, ...pidArr])]

  lineData = lineData.map(ele => {
    const arr = [...ele]
    const idList = idArr.concat(pidArr)
    idList.push(clickNode[0])
    arr.push(Number(idList.includes(ele[0]) && idList.includes(ele[3])))
    return arr
  })
  data = data.map(ele => {
    const arr = [...ele]
    arr[4] = '_' + arr[4]
    arr.push(Boolean(lineEnd.includes(ele[0])))
    return arr
  })

  const option = {
    xAxis: {
      show: false, // 不显示分隔线,
      splitLine: {
        show: false // 不显示分隔线
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
      formatter: a => {
        return a.value[5]
      }
    },
    yAxis: {
      show: false,
      type: 'category',
      splitLine: {
        show: false // 不显示分隔线
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
          const categoryIndex = api.value(0)
          const startPoint = api.coord([api.value(1), categoryIndex])
          const width = api.value(3)
          const height = 22

          const mainColor = getCSSVariable()
          const icon_database_svg = `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M1 4C1 1.79086 2.79086 0 5 0H19C21.2091 0 23 1.79086 23 4V20C23 22.2091 21.2091 24 19 24H5C2.79086 24 1 22.2091 1 20V4Z" fill="${mainColor}"/>
<path fill-rule="evenodd" clip-rule="evenodd" d="M17.1986 16.6394C17.5307 16.3776 17.8334 15.997 17.8334 15.5V8.50002C17.8334 8.00305 17.5307 7.62245 17.1986 7.36068C16.8599 7.09374 16.4059 6.87885 15.8969 6.70918C14.8728 6.36782 13.4951 6.16669 12.0001 6.16669C10.5051 6.16669 9.12741 6.36782 8.1033 6.70918C7.5943 6.87885 7.14031 7.09374 6.80158 7.36068C6.46944 7.62245 6.16675 8.00305 6.16675 8.50002V15.5C6.16675 15.997 6.46944 16.3776 6.80158 16.6394C7.14031 16.9063 7.5943 17.1212 8.1033 17.2909C9.12741 17.6322 10.5051 17.8334 12.0001 17.8334C13.4951 17.8334 14.8728 17.6322 15.8969 17.2909C16.4059 17.1212 16.8599 16.9063 17.1986 16.6394ZM7.33341 8.49986C7.33375 7.8556 9.42296 7.33335 12.0001 7.33335C14.5774 7.33335 16.6667 7.85569 16.6667 8.50002C16.6667 9.14435 14.5774 9.66669 12.0001 9.66669C9.42275 9.66669 7.33341 9.14435 7.33341 8.50002C7.33341 8.50001 7.33341 8.5 7.33341 8.49998C7.33341 8.49994 7.33341 8.4999 7.33341 8.49986ZM15.8969 10.2909C16.1729 10.1989 16.4327 10.0936 16.6667 9.97387V12.0002C16.6667 12.0008 16.6667 12.0034 16.665 12.0089C16.663 12.0154 16.6581 12.0283 16.6463 12.0475C16.6215 12.0879 16.571 12.1485 16.4764 12.223C16.2838 12.3748 15.969 12.537 15.5279 12.6841C14.6519 12.9761 13.4046 13.1667 12.0001 13.1667C10.5956 13.1667 9.34826 12.9761 8.47224 12.6841C8.03118 12.537 7.71632 12.3748 7.52372 12.223C7.42913 12.1485 7.37863 12.0879 7.35387 12.0475C7.34206 12.0283 7.33718 12.0154 7.3352 12.0089C7.33351 12.0034 7.33342 12.0007 7.33341 12V9.97387C7.56746 10.0936 7.8273 10.1989 8.1033 10.2909C9.12741 10.6322 10.5051 10.8334 12.0001 10.8334C13.4951 10.8334 14.8728 10.6322 15.8969 10.2909ZM15.8969 13.7909C16.1729 13.6989 16.4327 13.5936 16.6667 13.4739V15.5001C16.6667 15.5006 16.6668 15.5031 16.665 15.5089C16.663 15.5154 16.6581 15.5283 16.6463 15.5475C16.6215 15.5879 16.571 15.6485 16.4764 15.723C16.2838 15.8748 15.969 16.037 15.5279 16.1841C14.6519 16.4761 13.4046 16.6667 12.0001 16.6667C10.5956 16.6667 9.34826 16.4761 8.47224 16.1841C8.03118 16.037 7.71632 15.8748 7.52372 15.723C7.42913 15.6485 7.37863 15.5879 7.35387 15.5475C7.34206 15.5283 7.33718 15.5154 7.3352 15.5089C7.33341 15.5031 7.33341 15.5005 7.33341 15.5V13.4739C7.56746 13.5936 7.8273 13.6989 8.1033 13.7909C9.12741 14.1322 10.5051 14.3334 12.0001 14.3334C13.4951 14.3334 14.8728 14.1322 15.8969 13.7909Z" fill="white"/>
</svg>
`
          const icon_dashboard_svg = `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M3.33341 2.66669H20.6667C21.0349 2.66669 21.3334 2.96516 21.3334 3.33335V20.6667C21.3334 21.0349 21.0349 21.3334 20.6667 21.3334H3.33341C2.96522 21.3334 2.66675 21.0349 2.66675 20.6667V3.33335C2.66675 2.96516 2.96522 2.66669 3.33341 2.66669ZM4.66675 4.66669V19.3334H19.3334V4.66669H4.66675ZM8.7894 9.33335H7.7894C7.51326 9.33335 7.2894 9.55721 7.2894 9.83335V16.8334C7.2894 17.1095 7.51326 17.3334 7.7894 17.3334H8.7894C9.06554 17.3334 9.2894 17.1095 9.2894 16.8334V9.83335C9.2894 9.55721 9.06554 9.33335 8.7894 9.33335ZM12.5276 6.66669H11.5276C11.2515 6.66669 11.0276 6.88376 11.0276 7.15154V16.8485C11.0276 17.1163 11.2515 17.3334 11.5276 17.3334H12.5276C12.8038 17.3334 13.0276 17.1163 13.0276 16.8485V7.15154C13.0276 6.88376 12.8038 6.66669 12.5276 6.66669ZM16.1766 12H15.1766C14.9004 12 14.6766 12.2388 14.6766 12.5334V16.8C14.6766 17.0946 14.9004 17.3334 15.1766 17.3334H16.1766C16.4527 17.3334 16.6766 17.0946 16.6766 16.8V12.5334C16.6766 12.2388 16.4527 12 16.1766 12Z" fill="${mainColor}"/>
          <path d="M1 4C1 1.79086 2.79086 0 5 0H19C21.2091 0 23 1.79086 23 4V20C23 22.2091 21.2091 24 19 24H5C2.79086 24 1 22.2091 1 20V4Z" fill="${mainColor}"/>
          <path d="M6.45833 16.6667V11.7349C6.45833 11.3981 6.73816 11.125 7.08333 11.125H9.66667V6.78072C9.66667 6.4416 9.96514 6.16669 10.3333 6.16669H13.6667C14.0349 6.16669 14.3333 6.4416 14.3333 6.78072V9.66669H16.9167C17.2618 9.66669 17.5417 9.92785 17.5417 10.25V16.6667H17.8333C17.9944 16.6667 18.125 16.7973 18.125 16.9584V17.5417C18.125 17.7028 17.9944 17.8334 17.8333 17.8334H6.16667C6.00558 17.8334 5.875 17.7028 5.875 17.5417V16.9584C5.875 16.7973 6.00558 16.6667 6.16667 16.6667H6.45833ZM16.375 16.6667V10.8334H14.3333V16.6667H16.375ZM13.1667 16.6667V7.33335H10.8333V16.6667H13.1667ZM9.66667 16.6667V12.2917H7.625V16.6667H9.66667Z" fill="white"/>
          </svg>`
          const icon_data_visualization_svg = `<svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M0 3C0 1.34315 1.59845 0 3.57025 0H14.4298C16.4015 0 18 1.34315 18 3V15C18 16.6569 16.4015 18 14.4298 18H3.57025C1.59845 18 0 16.6569 0 15V3Z" fill="#00D6B9"/>
<path fill-rule="evenodd" clip-rule="evenodd" d="M5 5V11H13V5H5ZM4 4.5C4 4.22375 4.225 4 4.5 4H13.5C13.775 4 14 4.22375 14 4.5V11.5C14 11.7763 13.775 12 13.5 12H4.5C4.225 12 4 11.7763 4 11.5V4.5Z" fill="white"/>
<path d="M10.7192 6.39648L11.4262 7.10348L9.05873 9.47098L7.90423 8.31673L7.10348 9.11773L6.39648 8.41048L7.90423 6.90273L9.05849 8.05698L10.7192 6.39648Z" fill="white"/>
<path d="M11.5 13H6.5V14H11.5V13Z" fill="white"/>
</svg>
`
          const icon_dataset_svg = `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
<g clip-path="url(#clip0_181_24877)">
<path d="M0 4C0 1.79086 2.13127 0 4.76033 0H19.2397C21.8687 0 24 1.79086 24 4V20C24 22.2091 21.8687 24 19.2397 24H4.76033C2.13127 24 0 22.2091 0 20V4Z" fill="${mainColor}"/>
<path d="M4.66669 9.09589C4.66669 8.83447 4.81949 8.59716 5.05749 8.48898L11.7242 5.45868C11.8994 5.37901 12.1006 5.37901 12.2759 5.45868L18.9426 8.48898C19.1806 8.59716 19.3334 8.83447 19.3334 9.0959V15.5879C19.3334 15.8404 19.1907 16.0713 18.9648 16.1842L12.2982 19.5175C12.1105 19.6114 11.8896 19.6114 11.7019 19.5175L5.03521 16.1842C4.80936 16.0713 4.66669 15.8404 4.66669 15.5879V9.09589ZM16.8119 8.98512L12 6.7979L7.16215 8.99693L11.9733 11.0694L16.8119 8.98512ZM12.6667 12.2225V17.8426L18 15.1759V9.9251L12.6667 12.2225ZM6.00002 9.9481V15.1759L11.3334 17.8426V12.2455L6.00002 9.9481Z" fill="white"/>
</g>
<defs>
<clipPath id="clip0_181_24877">
<rect width="24" height="24" fill="white"/>
</clipPath>
</defs>
</svg>
`
          const icon_dashboard = svgToDataURI(icon_dashboard_svg)
          const icon_data_visualization = svgToDataURI(icon_data_visualization_svg)
          const icon_dataset = svgToDataURI(icon_dataset_svg)
          const icon_database = svgToDataURI(icon_database_svg)

          const imageType = {
            datasource: icon_database,
            dashboard: icon_dashboard,
            dataV: icon_data_visualization,
            dataset: icon_dataset
          }

          const stroke = (activeNode && api.value(8)) || !activeNode ? mainColor : '#c0c4cc'

          const val =
            isNaN(api.value(5)) || typeof api.value(5) === 'number'
              ? data.find(ele => ele[4] === api.value(4))[5]
              : api.value(5)

          return {
            type: 'group', // 当需要多个自定义拼接时，需要用group，此案例是文字和图形的拼接
            children: [
              {
                type: 'text',
                position: [startPoint[0] + gapDetail[api.value(6)], startPoint[1] - height / 2], // 相对位置
                z2: 10,
                style: {
                  text: contextVal!.measureText(val).width > width ? val.slice(0, 17) + '...' : val, // data中取值
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
                  fill: api.value(4) === activeId ? '#c2d4ff' : 'none',
                  stroke
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
          const categoryIndex = api.value(0)
          const categoryIndex2 = api.value(3)
          const startPoint = api.coord([api.value(1), categoryIndex])
          const endPoint = api.coord([api.value(2), categoryIndex2])
          function startPointX1() {
            if (api.value(5) === 'dataset') {
              return startPoint[0] + api.value(4)
            }

            if (datasourcePanel[current.queryType].includes(api.value(5))) {
              return startPoint[0] + api.value(4) + gapDetail.dataset
            }

            if (api.value(5) === current.queryType) {
              return 0
            }
          }

          const x1 = startPointX1()
          const mainColor = getCSSVariable()

          const stroke = (activeNode && api.value(7)) || !activeNode ? mainColor : '#c0c4cc'
          return {
            type: 'group', // 当需要多个自定义拼接时，需要用group，此案例是文字和图形的拼接
            children: [
              {
                type: 'bezierCurve',
                silent: true,
                shape: {
                  x1,
                  y1: startPoint[1],
                  x2: endPoint[0] + gapDetail[api.value(5)],
                  y2: endPoint[1],
                  cpx1: endPoint[0] + gapDetail[api.value(5)] - 50,
                  cpx2: endPoint[0] + gapDetail[api.value(5)] - 10,
                  cpy1: endPoint[1],
                  cpy2: endPoint[1],
                  percent: 1
                },
                style: {
                  stroke,
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
  myChart.setOption(option, true)
  if (props.detailDisabled) return
  myChart.on('click', function (params) {
    activeNode = {
      id: params.value[4].replaceAll('_', ''),
      type: params.value[6],
      name: params.value[5],
      index: params.value[0]
    }
    emits('clickNode', cloneDeep(activeNode))
    initEchart(params.value)
  })
}
const emits = defineEmits(['clickNode'])
defineExpose({
  getChartData,
  reRender
})
</script>

<template>
  <div :style="chartSize">
    <div id="main" :style="chartSizeMax" />
  </div>
</template>
