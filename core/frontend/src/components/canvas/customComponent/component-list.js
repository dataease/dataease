// 基础移动端定位样式
export const BASE_MOBILE_STYLE = {
  style: {
    width: 1600,
    height: 300,
    left: 0,
    top: 0,
    borderRadius: 3
  },
  x: 1,
  y: 1,
  sizex: 6,
  sizey: 4,
  miniSizex: 1,
  miniSizey: 1,
  auxiliaryMatrix: true
}

// 组件仪表板样式
export const COMMON_BACKGROUND_BASE = {
  backgroundColorSelect: true,
  color: '#FFFFFF',
  alpha: 100,
  borderRadius: 5,
  innerPadding: 0
}

// 组件仪表板样式
export const COMMON_BACKGROUND = {
  ...COMMON_BACKGROUND_BASE,
  enable: false,
  backgroundType: 'innerImage',
  innerImage: 'board/blue_1.svg',
  innerImageColor: '#1094E5',
  outerImage: null
}

// 空组件仪表板样式
export const COMMON_BACKGROUND_NONE = {
  enable: false,
  backgroundColorSelect: false,
  backgroundType: 'innerImage',
  color: '#FFFFFF',
  innerImage: 'board/blue_1.svg',
  innerImageColor: '#1094E5',
  outerImage: null,
  alpha: 100,
  borderRadius: 0,
  innerPadding: 0
}

// 公共样式
export const commonStyle = {
  rotate: 0,
  opacity: 1,
  borderStyle: 'solid',
  borderWidth: 0,
  borderRadius: 0
}

export const PIC_STYLE = {
  ...commonStyle,
  adaptation: 'adaptation'
}

export const commonAttr = {
  animations: [],
  events: {},
  groupStyle: {}, // 当一个组件成为 Group 的子组件时使用
  isLock: false // 是否锁定组件
}

// 超链接配置
export const HYPERLINKS = {
  openMode: '_blank',
  enable: false,
  content: 'http://'
}

// 视频信息配置
export const VIDEOLINKS = {
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
    sources: [{
      type: 'rtmp/mp4'
    }],
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

// 流媒体视频信息配置
export const STREAMMEDIALINKS = {
  videoType: 'flv',
  flv: {
    type: 'flv',
    isLive: false,
    cors: true, // 允许跨域
    loop: true,
    autoplay: false
    // url: null // 网络动画视频
  }
}

// 嵌套页面信息
export const FRAMELINKS = {
  src: ''
}

export const assistList = [
  {
    id: '10001',
    component: 'v-text',
    type: 'v-text',
    label: '文字',
    icon: 'iconfont icon-text',
    defaultClass: 'text-filter'
  },
  {
    id: '10002',
    component: 'de-rich-text',
    type: 'de-rich-text',
    label: '富文本',
    icon: 'iconfont icon-fuwenbenkuang',
    defaultClass: 'text-filter'
  },
  {
    id: '10004',
    component: 'rect-shape',
    type: 'rect-shape',
    label: '矩形',
    icon: 'iconfont icon-juxing1',
    defaultClass: 'text-filter'
  },
  {
    id: '10006',
    component: 'de-tabs',
    type: 'de-tabs',
    label: '选项卡',
    icon: 'iconfont icon-tabs',
    defaultClass: 'text-filter'
  }

]

export const pictureList = [
  {
    id: '20001',
    component: 'Picture',
    type: 'picture-add',
    label: '图片',
    icon: 'iconfont icon-picture',
    defaultClass: 'text-filter'
  },
  {
    id: '20002',
    component: 'video',
    type: 'video',
    label: '视频',
    icon: 'iconfont icon-video',
    defaultClass: 'text-filter'
  },
  {
    id: '20003',
    component: 'stream-media',
    type: 'stream-media',
    label: '流媒体',
    icon: 'iconfont icon-a-liumeitimeitiliebiao',
    defaultClass: 'text-filter'
  }
]

export const tabUseList = [
  {
    id: '20002',
    component: 'video',
    type: 'video',
    label: '视频',
    icon: 'iconfont icon-video',
    defaultClass: 'text-filter'
  },
  {
    id: '20003',
    component: 'stream-media',
    type: 'stream-media',
    label: '流媒体',
    icon: 'iconfont icon-a-liumeitimeitiliebiao',
    defaultClass: 'text-filter'
  },
  {
    id: '30002',
    component: 'de-frame',
    type: 'de-frame',
    label: '网页',
    icon: 'iconfont icon-iframe',
    defaultClass: 'text-filter'
  }
]

export const otherList = [
  {
    id: '30001',
    component: 'de-show-date',
    type: 'de-show-date',
    label: '时间',
    icon: 'iconfont icon-shijian',
    defaultClass: 'text-filter'
  },
  {
    id: '30002',
    component: 'de-frame',
    type: 'de-frame',
    label: '网页',
    icon: 'iconfont icon-iframe',
    defaultClass: 'text-filter'
  }
]

export const USER_VIEW = {
  id: '10005',
  component: 'user-view',
  label: '用户视图',
  propValue: '',
  icon: 'juxing',
  type: 'view',
  mobileStyle: BASE_MOBILE_STYLE,
  hyperlinks: HYPERLINKS,
  style: {
    width: 300,
    height: 200
  },
  x: 1,
  y: 108,
  sizex: 12,
  sizey: 6,
  auxiliaryMatrix: true,
  miniSizex: 1,
  miniSizey: 1
}

// 编辑器左侧组件列表
const list = [
  {
    id: '10001',
    component: 'v-text',
    label: '文字',
    propValue: '双击输入文字',
    icon: 'wenben',
    type: 'v-text',
    mobileStyle: BASE_MOBILE_STYLE,
    hyperlinks: HYPERLINKS,
    style: {
      width: 400,
      height: 100,
      fontSize: 22,
      fontWeight: 400,
      lineHeight: '',
      letterSpacing: 0,
      textAlign: 'center',
      color: '#000000',
      verticalAlign: 'middle'
    },
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 2,
    miniSizex: 1,
    miniSizey: 1
  },
  {
    id: '10002',
    component: 'de-rich-text',
    label: '富文本',
    propValue: '双击进入编辑状态',
    icon: 'icon-fuwenbenkuang',
    type: 'de-rich-text',
    mobileStyle: BASE_MOBILE_STYLE,
    hyperlinks: HYPERLINKS,
    style: {
      width: 400,
      height: 100
    },
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 2,
    miniSizex: 1,
    miniSizey: 1
  },
  {
    id: '10003',
    component: 'Picture',
    label: '图片',
    icon: 'tupian',
    type: 'Picture',
    propValue: require('@/components/canvas/assets/title.jpg'),
    mobileStyle: BASE_MOBILE_STYLE,
    hyperlinks: HYPERLINKS,
    style: {
      width: 300,
      height: 200
    },
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 6,
    miniSizex: 1,
    miniSizey: 1
  },
  {
    id: '10003-1',
    component: 'Picture',
    label: '背景-科技1',
    icon: 'tupian',
    type: 'Picture',
    propValue: require('@/components/canvas/assets/bg-kj-1.jpg'),
    mobileStyle: BASE_MOBILE_STYLE,
    hyperlinks: HYPERLINKS,
    style: {
      borderStyle: 'solid',
      borderWidth: 0,
      width: 600,
      height: 300,
      borderRadius: ''
    },
    miniSizex: 1,
    miniSizey: 1
  },
  {
    id: '10004',
    component: 'rect-shape',
    label: '矩形',
    propValue: '',
    icon: 'juxing',
    type: 'rect-shape',
    mobileStyle: BASE_MOBILE_STYLE,
    hyperlinks: HYPERLINKS,
    style: {
      width: 300,
      height: 200,
      borderStyle: 'solid',
      borderWidth: 0,
      borderColor: '#000000'
    },
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 6,
    miniSizex: 1,
    miniSizey: 1
  },
  USER_VIEW,
  {
    id: '10006',
    component: 'de-tabs',
    label: '选项卡',
    propValue: '',
    icon: 'tabs',
    type: 'de-tabs',
    mobileStyle: BASE_MOBILE_STYLE,
    hyperlinks: HYPERLINKS,
    style: {
      width: 400,
      height: 400,
      borderStyle: 'solid',
      borderWidth: 0,
      borderColor: '#000000',
      fontSize: 16,
      activeFontSize: 18,
      carouselEnable: false,
      switchTime: 5
    },
    options: {
      tabList: [{
        title: 'Tab1',
        name: '1',
        content: { type: 'canvas' }
      }]
    },
    x: 1,
    y: 1,
    sizex: 12,
    sizey: 10,
    miniSizex: 1,
    miniSizey: 1
  },
  {
    id: '30001',
    component: 'de-show-date',
    label: '时间',
    propValue: '',
    icon: 'shijian',
    type: 'de-show-date',
    mobileStyle: BASE_MOBILE_STYLE,
    hyperlinks: HYPERLINKS,
    style: {
      width: 250,
      height: 100,
      fontSize: 22,
      fontWeight: 400,
      lineHeight: '',
      letterSpacing: 0,
      textAlign: 'center',
      color: '#000000',
      verticalAlign: 'middle',
      borderStyle: 'solid',
      borderColor: '#000000',
      time_margin: 0
    },
    formatInfo: {
      openMode: '0',
      showWeek: false,
      showDate: true,
      dateFormat: 'yyyy-MM-dd',
      timeFormat: 'hh:mm:ss'
    },
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 2,
    miniSizex: 1,
    miniSizey: 1
  },
  {
    id: '30002',
    component: 'de-frame',
    type: 'de-frame',
    label: '',
    icon: 'iconfont icon-iframe',
    defaultClass: 'text-filter',
    mobileStyle: BASE_MOBILE_STYLE,
    hyperlinks: HYPERLINKS,
    style: {
      width: 400,
      height: 200
    },
    frameLinks: FRAMELINKS,
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 5,
    miniSizex: 1,
    miniSizey: 1
  },
  {
    id: '20001',
    component: 'Picture',
    type: 'picture-add',
    label: '图片',
    icon: 'iconfont icon-picture',
    defaultClass: 'text-filter',
    mobileStyle: BASE_MOBILE_STYLE,
    hyperlinks: HYPERLINKS,
    style: {
      width: 400,
      height: 200,
      adaptation: 'adaptation'
    },
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 5,
    miniSizex: 1,
    miniSizey: 1
  },
  {
    id: '20002',
    component: 'de-video',
    type: 'de-video',
    label: '',
    icon: 'iconfont icon-picture',
    defaultClass: 'text-filter',
    mobileStyle: BASE_MOBILE_STYLE,
    hyperlinks: HYPERLINKS,
    style: {
      width: 400,
      height: 200
    },
    videoLinks: VIDEOLINKS,
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 5,
    miniSizex: 1,
    miniSizey: 1
  },
  {
    id: '20003',
    component: 'de-stream-media',
    type: 'de-stream-media',
    label: '',
    icon: 'iconfont icon-picture',
    defaultClass: 'text-filter',
    mobileStyle: BASE_MOBILE_STYLE,
    hyperlinks: HYPERLINKS,
    style: {
      width: 400,
      height: 200
    },
    streamMediaLinks: STREAMMEDIALINKS,
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 5,
    miniSizex: 1,
    miniSizey: 1
  }
]

for (let i = 0, len = list.length; i < len; i++) {
  const item = list[i]
  item.style = { ...commonStyle, ...item.style }
  list[i] = { ...commonAttr, ...item }
}

export default list
