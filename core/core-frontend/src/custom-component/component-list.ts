// 公共样式
import { deepCopy } from '@/utils/utils'
import { guid } from '@/views/visualized/data/dataset/form/util'
import { getViewConfig } from '@/views/chart/components/editor/util/chart'

export const commonStyle = {
  rotate: 0,
  opacity: 1
}

export const BASE_EVENTS = {
  checked: false,
  type: 'displayChange', // openHidden  jump
  jump: {
    value: null
  },
  displayChange: {
    value: true, // 事件当前值 false
    target: 'all'
  }
}

// 流媒体视频信息配置
export const STREAMMEDIALINKS = {
  videoType: 'flv',
  flv: {
    type: 'flv',
    isLive: false,
    cors: true, // 允许跨域
    loop: true,
    autoplay: false,
    url: null // 网络动画视频
  }
}

// 视频信息配置
export const VIDEO_LINKS_DE2 = {
  videoType: 'web',
  poster: null,
  web: {
    src: null, //视频源
    autoplay: true, // 如果true,浏览器准备好时开始回放。
    muted: true, // 默认情况下将会消除任何音频。
    loop: true, // 导致视频一结束就重新开始。
    preload: 'auto', // 建议浏览器在<video>加载元素后是否应该开始下载视频数据。auto浏览器选择最佳行为,立即开始加载视频（如果浏览器支持）
    language: 'zh-CN',
    fluid: true, // 当true时，Video.js player将拥有流体大小。换句话说，它将按比例缩放以适应其容器。
    notSupportedMessage: '此视频暂无法播放，请稍后再试', // 允许覆盖Video.js无法播放媒体源时显示的默认信息。
    controls: true,
    controlBar: {
      timeDivider: true,
      remainingTimeDisplay: false,
      fullscreenToggle: true // 全屏按钮
    }
  },
  rtmp: {
    sources: [
      {
        type: 'rtmp/mp4'
      }
    ],
    height: 300,
    techOrder: ['flash'],
    autoplay: false,
    controls: true,
    flash: {
      hls: {
        withCredentials: false
      }
    }
  }
}

// 视频信息配置
export const VIDEO_LINKS = {
  videoType: 'web',
  web: {
    autoplay: true,
    height: 300,
    muted: true,
    loop: true,
    controlBar: {
      timeDivider: false,
      durationDisplay: false,
      remainingTimeDisplay: false,
      currentTimeDisplay: false, // 当前时间
      volumeControl: false, // 声音控制键
      fullscreenToggle: false,
      pause: false
    },
    sources: [{}]
  },
  rtmp: {
    sources: [
      {
        type: 'rtmp/mp4'
      }
    ],
    height: 300,
    techOrder: ['flash'],
    autoplay: false,
    controls: true,
    flash: {
      hls: {
        withCredentials: false
      }
    }
  }
}

// 超链接配置
export const HYPERLINKS = {
  openMode: '_blank',
  enable: false,
  content: 'http://'
}

// 嵌套页面信息
export const FRAMELINKS = {
  src: ''
}

export const defaultStyleValue = {
  ...commonStyle,
  color: '',
  fontSize: 16,
  activeFontSize: 18,
  headHorizontalPosition: 'left',
  headFontColor: '#000000',
  headFontActiveColor: '#000000'
}

export const ACTION_SELECTION = {
  linkageActive: 'custom'
}

export const COMMON_COMPONENT_BACKGROUND_BASE = {
  backgroundColorSelect: true,
  backgroundImageEnable: false,
  backgroundType: 'innerImage',
  innerImage: 'board/board_1.svg',
  outerImage: null,
  innerPadding: 12,
  borderRadius: 0
}

export const COMMON_COMPONENT_BACKGROUND_LIGHT = {
  ...COMMON_COMPONENT_BACKGROUND_BASE,
  backgroundColor: 'rgba(255,255,255,1)',
  innerImageColor: 'rgba(16, 148, 229,1)'
}

export const COMMON_COMPONENT_BACKGROUND_DARK = {
  ...COMMON_COMPONENT_BACKGROUND_BASE,
  backgroundColor: 'rgba(19,28,66,1)',
  innerImageColor: '#1094E5'
}

export const COMMON_COMPONENT_BACKGROUND_SCREEN_DARK = {
  ...COMMON_COMPONENT_BACKGROUND_BASE,
  backgroundColorSelect: false,
  backgroundColor: '#131E42',
  innerImageColor: '#1094E5'
}

export const COMMON_COMPONENT_BACKGROUND_MAP = {
  light: COMMON_COMPONENT_BACKGROUND_LIGHT,
  dark: COMMON_COMPONENT_BACKGROUND_DARK
}

export const commonAttr = {
  animations: [],
  canvasId: 'canvas-main',
  events: BASE_EVENTS,
  groupStyle: {}, // 当一个组件成为 Group 的子组件时使用
  isLock: false, // 是否锁定组件
  maintainRadio: false, // 布局时保持宽高比例
  aspectRatio: 1, // 锁定时的宽高比例
  isShow: true, // 是否显示组件
  category: 'base', //组件类型 base 基础组件 hidden隐藏组件
  // 当前组件动作
  dragging: false,
  resizing: false,
  collapseName: [
    'position',
    'background',
    'style',
    'picture',
    'frameLinks',
    'videoLinks',
    'streamLinks'
  ], // 编辑组件时记录当前使用的是哪个折叠面板，再次回来时恢复上次打开的折叠面板，优化用户体验
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
    id: 100000001,
    component: 'GroupArea',
    name: '组合区域',
    label: '组合区域',
    propValue: '&nbsp;',
    icon: 'icon_graphical',
    innerType: 'GroupArea',
    style: {
      width: 200,
      height: 200
    }
  },
  {
    component: 'VQuery',
    name: '查询',
    label: '查询',
    propValue: '',
    icon: 'icon_search',
    innerType: 'VQuery',
    isHang: false,
    x: 1,
    y: 1,
    sizeX: 72,
    sizeY: 4,
    style: {
      width: 400,
      height: 100
    },
    request: {
      method: 'GET',
      data: [],
      url: '',
      series: false, // 是否定时发送请求
      time: 1000, // 定时更新时间
      paramType: '', // string object array
      requestCount: 0 // 请求次数限制，0 为无限
    },
    matrixStyle: {}
  },
  {
    component: 'UserView',
    name: '图表',
    label: '图表',
    propValue: { textValue: '' },
    icon: 'bar',
    innerType: 'bar',
    editing: false,
    canvasActive: false,
    actionSelection: ACTION_SELECTION,
    x: 1,
    y: 1,
    sizeX: 36,
    sizeY: 14,
    style: {
      width: 600,
      height: 300
    },
    matrixStyle: {}
  },
  {
    component: 'DeVideo',
    name: '视频',
    label: '视频',
    innerType: 'DeVideo',
    editing: false,
    canvasActive: false,
    icon: 'icon-video',
    x: 1,
    y: 1,
    sizeX: 36,
    sizeY: 14,
    style: {
      width: 600,
      height: 300
    },
    videoLinks: VIDEO_LINKS_DE2,
    matrixStyle: {}
  },
  {
    component: 'DeStreamMedia',
    name: '流媒体',
    label: '流媒体',
    innerType: 'DeStreamMedia',
    editing: false,
    canvasActive: false,
    icon: 'icon-stream',
    x: 1,
    y: 1,
    sizeX: 36,
    sizeY: 14,
    style: {
      width: 600,
      height: 300
    },
    streamMediaLinks: STREAMMEDIALINKS,
    matrixStyle: {}
  },
  {
    component: 'DeFrame',
    name: '网页',
    label: '网页',
    innerType: 'DeFrame',
    editing: false,
    canvasActive: false,
    icon: 'db-more-web',
    hyperlinks: HYPERLINKS,
    frameLinks: FRAMELINKS,
    x: 1,
    y: 1,
    sizeX: 36,
    sizeY: 14,
    style: {
      width: 600,
      height: 300
    },
    matrixStyle: {}
  },
  {
    component: 'DeTimeClock',
    name: '时间组件',
    label: '时间组件',
    icon: 'dv-more-time-clock',
    innerType: 'DeTimeClock',
    editing: false,
    canvasActive: false,
    x: 1,
    y: 1,
    sizeX: 36,
    sizeY: 12,
    propValue: {},
    style: {
      width: 300,
      height: 100,
      fontSize: 22,
      fontWeight: 'normal',
      fontStyle: 'normal',
      textAlign: 'center',
      color: '#000000'
    },
    formatInfo: {
      openMode: '0',
      showWeek: false,
      showDate: true,
      dateFormat: 'yyyy-MM-dd',
      timeFormat: 'hh:mm:ss'
    },
    matrixStyle: {}
  },
  {
    component: 'Picture',
    name: '图片',
    label: '图片',
    icon: 'dv-picture-real',
    innerType: 'Picture',
    editing: false,
    canvasActive: false,
    x: 1,
    y: 1,
    sizeX: 36,
    sizeY: 14,
    propValue: {
      url: '',
      flip: {
        horizontal: false,
        vertical: false
      }
    },
    style: {
      adaptation: 'adaptation',
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
    editing: false,
    canvasActive: false,
    x: 1,
    y: 1,
    sizeX: 10,
    sizeY: 10,
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
    editing: false,
    canvasActive: false,
    x: 1,
    y: 1,
    sizeX: 30,
    sizeY: 30,
    style: {
      width: 600,
      height: 300,
      color: 'rgb(255, 255, 255,1)'
    }
  },
  {
    component: 'RectShape',
    name: '矩形',
    label: '矩形',
    propValue: '&nbsp;',
    icon: 'icon_graphical',
    style: {
      width: 200,
      height: 200,
      borderWidth: 1,
      borderRadius: 5,
      borderStyle: 'solid',
      borderColor: '#cccccc',
      backgroundColor: 'rgba(236,231,231,0.1)'
    }
  },
  {
    component: 'CircleShape',
    name: '圆形',
    label: '圆形',
    propValue: '&nbsp;',
    icon: 'icon_graphical',
    style: {
      width: 200,
      height: 200,
      borderWidth: 1,
      borderStyle: 'solid',
      borderColor: '#cccccc',
      backgroundColor: 'rgba(236,231,231,0.1)'
    }
  },
  {
    component: 'SvgTriangle',
    name: '三角形',
    label: '三角形',
    icon: 'icon_graphical',
    propValue: '',
    style: {
      width: 200,
      height: 200,
      borderWidth: 1,
      borderColor: '#cccccc',
      backgroundColor: 'rgba(236,231,231,0.1)'
    }
  },
  {
    component: 'DeTabs',
    name: '选项卡',
    label: '选项卡',
    propValue: [
      {
        name: 'tab',
        title: '新建Tab',
        componentData: [],
        closable: true
      }
    ],
    icon: 'dv-tab',
    innerType: '',
    editing: false,
    canvasActive: false,
    x: 1,
    y: 1,
    sizeX: 36,
    sizeY: 14,
    style: {
      width: 600,
      height: 300,
      fontSize: 16,
      activeFontSize: 18,
      headHorizontalPosition: 'left',
      headFontColor: '#000000',
      headFontActiveColor: '#000000'
    }
  },
  {
    component: 'ScrollText',
    name: '跑马灯',
    label: '跑马灯',
    propValue: '双击编辑文字',
    innerType: 'ScrollText',
    icon: 'scroll-text',
    x: 1,
    y: 1,
    sizeX: 36,
    sizeY: 14,
    style: {
      width: 400,
      height: 80,
      fontSize: 14,
      fontWeight: 400,
      letterSpacing: 0,
      color: '',
      padding: 4,
      verticalAlign: 'middle',
      scrollSpeed: 0,
      fontFamily: 'Microsoft YaHei'
    }
  }
]

for (let i = 0, len = list.length; i < len; i++) {
  const item = list[i]
  item.style = { ...commonStyle, ...item.style }
  item['commonBackground'] = deepCopy(COMMON_COMPONENT_BACKGROUND_BASE)
  item['state'] = 'prepare'
  list[i] = { ...commonAttr, ...item }
}

export function findNewComponentFromList(
  componentName,
  innerType,
  curOriginThemes,
  staticMap?: object
) {
  const isPlugin = !!staticMap
  let newComponent
  list.forEach(comp => {
    if (comp.component === componentName) {
      newComponent = deepCopy(comp)
      newComponent['commonBackground'] = deepCopy(
        COMMON_COMPONENT_BACKGROUND_MAP[curOriginThemes.value]
      )
      newComponent.innerType = innerType
      if (comp.component === 'DeTabs') {
        newComponent.propValue[0].name = guid()
      }
    }
  })

  if (componentName === 'UserView') {
    const viewConfig = getViewConfig(innerType)
    newComponent.name = viewConfig?.title
    newComponent.label = viewConfig?.title
    newComponent.render = viewConfig?.render
    newComponent.isPlugin = !!isPlugin
    if (isPlugin) {
      newComponent.staticMap = staticMap
    }
  }
  return newComponent
}

export function findBaseDeFaultAttr(componentName) {
  let result = {}
  list.forEach(comp => {
    if (comp.component === componentName) {
      const stylePropertyInner = []
      Object.keys(comp.style).forEach(styleKey => {
        stylePropertyInner.push(styleKey)
      })
      result = {
        properties: ['common-style', 'background-overall-component'],
        propertyInner: {
          'common-style': stylePropertyInner,
          'background-overall-component': ['all']
        },
        value: comp.name,
        componentType: componentName
      }
    }
  })
  return result
}

export default list
