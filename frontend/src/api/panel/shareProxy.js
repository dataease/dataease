import request from '@/utils/request'
import { panelInit } from '@/components/canvas/utils/utils'
import store from '@/store'

export function proxyInitPanelData(panelId, proxy, callback) {
  // 加载视图数据
  findOne(panelId, proxy).then(response => {
    // 初始化视图data和style 数据
    panelInit(JSON.parse(response.data.panelData), JSON.parse(response.data.panelStyle))
    // 设置当前仪表板全局信息
    store.dispatch('panel/setPanelInfo', {
      id: response.data.id,
      name: response.data.name,
      privileges: response.data.privileges,
      proxy: proxy.userId,
      status: response.data.status,
      createBy: response.data.createBy,
      createTime: response.data.createTime,
      updateBy: response.data.updateBy,
      updateTime: response.data.updateTime
    })
    // 刷新联动信息
    getPanelAllLinkageInfo(panelId, proxy).then(rsp => {
      store.commit('setNowPanelTrackInfo', rsp.data)
    })
    // 刷新跳转信息
    queryPanelJumpInfo(panelId, proxy).then(rsp => {
      store.commit('setNowPanelJumpInfo', rsp.data)
    })
    callback && callback(response)
  })
}

export function findOne(id, data) {
  return request({
    url: '/panel/group/proxy/findOne/' + id,
    method: 'post',
    loading: true,
    data
  })
}

export function getPanelAllLinkageInfo(panelId, data) {
  return request({
    url: '/linkage/proxy/getPanelAllLinkageInfo/' + panelId,
    method: 'post',
    data
  })
}

export function queryPanelJumpInfo(panelId, data) {
  return request({
    url: '/linkJump/proxy/queryPanelJumpInfo/' + panelId,
    method: 'post',
    data
  })
}

