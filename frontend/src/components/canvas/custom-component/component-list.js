// 公共样式
export const commonStyle = {
  rotate: 0,
  opacity: 1
}

export const commonAttr = {
  animations: [],
  events: {},
  groupStyle: {}, // 当一个组件成为 Group 的子组件时使用
  isLock: false // 是否锁定组件
}

// 编辑器左侧组件列表
const list = [
  {
    id: '10001',
    component: 'v-text',
    label: '文字',
    propValue: '双击编辑文字',
    icon: 'wenben',
    type: 'other',
    style: {
      width: 200,
      height: 22,
      fontSize: 14,
      fontWeight: 500,
      lineHeight: '',
      letterSpacing: 0,
      textAlign: '',
      color: ''
    }
  },
  {
    id: '10002',
    component: 'v-button',
    label: '按钮',
    propValue: '按钮',
    icon: 'button',
    type: 'other',
    style: {
      width: 100,
      height: 34,
      borderWidth: '',
      borderColor: '',
      borderRadius: '',
      fontSize: 14,
      fontWeight: 500,
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
    type: 'other',
    propValue: require('@/components/canvas/assets/title.jpg'),
    style: {
      width: 300,
      height: 200,
      borderRadius: ''
    }
  },
  {
    id: '10003-1',
    component: 'Picture',
    label: '背景-科技1',
    icon: 'tupian',
    type: 'other',
    propValue: require('@/components/canvas/assets/bg-kj-1.jpg'),
    style: {
      width: 600,
      height: 300,
      borderRadius: ''
    }
  },
  {
    id: '10004',
    component: 'rect-shape',
    label: '矩形',
    propValue: '&nbsp;',
    icon: 'juxing',
    type: 'other',
    style: {
      width: 200,
      height: 200,
      fontSize: 14,
      fontWeight: 500,
      lineHeight: '',
      letterSpacing: 0,
      textAlign: 'center',
      color: '',
      borderColor: '#000',
      borderWidth: 1,
      backgroundColor: '',
      borderStyle: 'solid',
      verticalAlign: 'middle'
    }
  },
  {
    id: '10005',
    component: 'user-view',
    label: '用户视图',
    propValue: '',
    icon: 'juxing',
    type: 'view',
    style: {
      width: 200,
      height: 300,
      borderWidth: 1
    }
  }
]

for (let i = 0, len = list.length; i < len; i++) {
  const item = list[i]
  item.style = { ...commonStyle, ...item.style }
  list[i] = { ...commonAttr, ...item }
}

export default list
