// 基础移动端定位样式
export const BASE_MOBILE_STYLE = {
  style: {
    width: 1600,
    height: 300,
    borderRadius: 5
  },
  x: 1,
  y: 1,
  sizex: 6,
  sizey: 4,
  auxiliaryMatrix: true
}

// 公共样式
export const commonStyle = {
  rotate: 0,
  opacity: 1,
  borderStyle: 'solid',
  borderWidth: 0,
  borderRadius: 0
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
      fullscreenToggle: false
    },
    sources: [{
    }]
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
    id: '10004',
    component: 'rect-shape',
    type: 'rect-shape',
    label: '矩形',
    icon: 'iconfont icon-juxing',
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
    component: 'picture-add',
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
  }
]

export const dateList = [
  {
    id: '30001',
    component: 'de-show-date',
    type: 'de-show-date',
    label: '时间',
    icon: 'iconfont icon-shijian',
    defaultClass: 'text-filter'
  }
]

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
    style: {
      width: 400,
      height: 100,
      fontSize: 22,
      fontWeight: 400,
      lineHeight: '',
      letterSpacing: 0,
      textAlign: 'center',
      color: '#000000',
      verticalAlign: 'middle',
      backgroundColor: '#ffffff',
      borderRadius: 0
    },
    hyperlinks: HYPERLINKS,
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 2
  },
  {
    id: '10002',
    component: 'v-button',
    label: '按钮',
    propValue: '按钮',
    icon: 'button',
    type: 'v-button',
    mobileStyle: BASE_MOBILE_STYLE,
    style: {
      width: 100,
      height: 34,
      borderWidth: '',
      borderColor: '',
      borderRadius: '',
      fontSize: 14,
      fontWeight: 400,
      lineHeight: '',
      letterSpacing: 0,
      textAlign: '',
      color: '',
      backgroundColor: ''
    }
  },
  {
    id: '10003',
    component: 'Picture',
    label: '图片',
    icon: 'tupian',
    type: 'Picture',
    propValue: require('@/components/canvas/assets/title.jpg'),
    mobileStyle: BASE_MOBILE_STYLE,
    style: {
      width: 300,
      height: 200,
      borderRadius: ''
    },
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 6
  },
  {
    id: '10003-1',
    component: 'Picture',
    label: '背景-科技1',
    icon: 'tupian',
    type: 'Picture',
    propValue: require('@/components/canvas/assets/bg-kj-1.jpg'),
    mobileStyle: BASE_MOBILE_STYLE,
    style: {
      borderStyle: 'solid',
      borderWidth: 0,
      width: 600,
      height: 300,
      borderRadius: ''
    }
  },
  {
    id: '10004',
    component: 'rect-shape',
    label: '矩形',
    propValue: '',
    icon: 'juxing',
    type: 'rect-shape',
    mobileStyle: BASE_MOBILE_STYLE,
    style: {
      width: 300,
      height: 200,
      borderStyle: 'solid',
      borderWidth: 0,
      borderColor: '#000000',
      backgroundColor: '#ffffff',
      borderRadius: 0
    },
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 6
  },
  {
    id: '10005',
    component: 'user-view',
    label: '用户视图',
    propValue: '',
    icon: 'juxing',
    type: 'view',
    mobileStyle: BASE_MOBILE_STYLE,
    style: {
      width: 300,
      height: 200,
      borderRadius: ''
    },
    x: 1,
    y: 36,
    sizex: 10,
    sizey: 6,
    auxiliaryMatrix: true
  },
  {
    id: '10006',
    component: 'de-tabs',
    label: '选项卡',
    propValue: '',
    icon: 'tabs',
    type: 'de-tabs',
    mobileStyle: BASE_MOBILE_STYLE,
    style: {
      width: 200,
      height: 200,
      borderStyle: 'solid',
      borderWidth: 0,
      borderColor: '#000000',
      backgroundColor: '#ffffff',
      borderRadius: 0
    },
    options: {
      tabList: [{
        title: 'Tab1',
        name: '1',
        content: null
      }]
    },
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 10
  },
  {
    id: '30001',
    component: 'de-show-date',
    label: '时间',
    propValue: '',
    icon: 'shijian',
    type: 'de-show-date',
    mobileStyle: BASE_MOBILE_STYLE,
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
      backgroundColor: '#ffffff',
      borderStyle: 'solid',
      borderColor: '#000000',
      borderRadius: 0,
      time_margin: 0,
      padding: 10
    },
    formatInfo: {
      openMode: '0',
      showWeek: false,
      showDate: true,
      dateFormat: 'yyyy年-MM月-dd日',
      timeFormat: 'hh:mm:ss'
    },
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 2
  },
  {
    id: '20001',
    component: 'picture-add',
    type: 'picture-add',
    label: '图片',
    icon: 'iconfont icon-picture',
    defaultClass: 'text-filter',
    mobileStyle: BASE_MOBILE_STYLE,
    style: {
      width: 400,
      height: 200,
      borderRadius: ''
    },
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 5
  },
  {
    id: '20002',
    component: 'de-video',
    type: 'de-video',
    label: '',
    icon: 'iconfont icon-picture',
    defaultClass: 'text-filter',
    mobileStyle: BASE_MOBILE_STYLE,
    style: {
      width: 400,
      height: 200,
      borderRadius: ''
    },
    videoLinks: VIDEOLINKS,
    x: 1,
    y: 1,
    sizex: 10,
    sizey: 5
  }
]

for (let i = 0, len = list.length; i < len; i++) {
  const item = list[i]
  item.style = { ...commonStyle, ...item.style }
  list[i] = { ...commonAttr, ...item }
}

export default list
