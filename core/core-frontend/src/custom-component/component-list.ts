// 公共样式
import { deepCopy } from '@/utils/utils'

export const commonStyle = {
  rotate: 0,
  opacity: 1
}

export const COMMON_COMPONENT_BACKGROUND_BASE = {
  backgroundColorSelect: true,
  alpha: 100,
  backgroundImageEnable: false,
  backgroundType: 'innerImage',
  innerImage: 'board/board_1.svg',
  outerImage: null,
  innerPadding: 0,
  borderRadius: 0
}

export const COMMON_COMPONENT_BACKGROUND_LIGHT = {
  ...COMMON_COMPONENT_BACKGROUND_BASE,
  backgroundColor: '#ffffff',
  innerImageColor: '#1094E5'
}

export const COMMON_COMPONENT_BACKGROUND_DARK = {
  ...COMMON_COMPONENT_BACKGROUND_BASE,
  backgroundColor: '#131E42',
  innerImageColor: '#1094E5'
}

export const COMMON_COMPONENT_BACKGROUND_MAP = {
  light: COMMON_COMPONENT_BACKGROUND_LIGHT,
  dark: COMMON_COMPONENT_BACKGROUND_DARK
}

export const commonAttr = {
  animations: [],
  events: {},
  groupStyle: {}, // 当一个组件成为 Group 的子组件时使用
  isLock: false, // 是否锁定组件
  isShow: true, // 是否显示组件
  collapseName: 'position', // 编辑组件时记录当前使用的是哪个折叠面板，再次回来时恢复上次打开的折叠面板，优化用户体验
  linkage: {
    duration: 0, // 过渡持续时间
    data: [
      // 组件联动
      {
        id: '', // 联动的组件 id
        label: '', // 联动的组件名称
        event: '', // 监听事件
        style: [{ key: '', value: '' }] // 监听的事件触发时，需要改变的属性
      }
    ]
  }
}

// 编辑器左侧组件列表
const list = [
  {
    component: 'VText',
    name: '文本',
    label: '文本',
    propValue: '双击编辑文字',
    icon: 'other_text',
    innerType: 'VText',
    x: 1,
    y: 1,
    sizeX: 10,
    sizeY: 5,
    request: {
      method: 'GET',
      data: [],
      url: '',
      series: false, // 是否定时发送请求
      time: 1000, // 定时更新时间
      paramType: '', // string object array
      requestCount: 0 // 请求次数限制，0 为无限
    },
    style: {
      width: 200,
      height: 100,
      fontSize: 14,
      fontWeight: 400,
      lineHeight: 1,
      letterSpacing: 0,
      textAlign: 'center',
      color: ''
    },
    matrixStyle: {}
  },
  {
    component: 'VQuery',
    name: '查询',
    label: '查询',
    propValue: '',
    icon: 'other_text',
    innerType: 'VQuery',
    x: 1,
    y: 1,
    sizeX: 10,
    sizeY: 5,
    request: {
      method: 'GET',
      data: [],
      url: '',
      series: false, // 是否定时发送请求
      time: 1000, // 定时更新时间
      paramType: '', // string object array
      requestCount: 0 // 请求次数限制，0 为无限
    },
    style: {
      width: 800,
      height: 150
    },
    matrixStyle: {}
  },
  {
    component: 'UserView',
    name: '视图',
    label: '视图',
    propValue: '',
    icon: 'bar',
    innerType: 'bar',
    x: 1,
    y: 1,
    sizeX: 10,
    sizeY: 5,
    style: {
      width: 200,
      height: 200
    },
    matrixStyle: {}
  },
  {
    component: 'Picture',
    name: '图片',
    label: '图片',
    icon: 'other_media',
    innerType: 'Picture',
    x: 1,
    y: 1,
    sizeX: 10,
    sizeY: 5,
    propValue: {
      url: '',
      flip: {
        horizontal: false,
        vertical: false
      }
    },
    style: {
      width: 300,
      height: 200
    },
    matrixStyle: {}
  },
  {
    component: 'CanvasIcon',
    name: '图标',
    label: '图标',
    propValue: '',
    icon: 'other_material_icon',
    innerType: '',
    x: 1,
    y: 1,
    sizeX: 5,
    sizeY: 5,
    style: {
      width: 40,
      height: 40,
      color: ''
    }
  },
  {
    component: 'CanvasBoard',
    name: '边框',
    label: '边框',
    propValue: '',
    icon: 'other_material_board',
    innerType: '',
    x: 1,
    y: 1,
    sizeX: 15,
    sizeY: 15,
    style: {
      width: 150,
      height: 100,
      color: ''
    }
  }
]

for (let i = 0, len = list.length; i < len; i++) {
  const item = list[i]
  item.style = { ...commonStyle, ...item.style }
  item['commonBackground'] = deepCopy(COMMON_COMPONENT_BACKGROUND_BASE)
  list[i] = { ...commonAttr, ...item }
}

export function findNewComponentFromList(componentName, innerType, curOriginThemes) {
  let newComponent
  list.forEach(comp => {
    if (comp.component === componentName) {
      newComponent = deepCopy(comp)
      ;(newComponent['commonBackground'] = deepCopy(
        COMMON_COMPONENT_BACKGROUND_MAP[curOriginThemes.value]
      )),
        (newComponent.innerType = innerType)
    }
  })
  return newComponent
}

export default list
