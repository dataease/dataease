import request from '@/common/js/request'

export function requestHome(data) {
    return request({
      url: '/mobile/home/query',
      method: 'post',
      loading: true,
	  data
    })
}

export function requestDir(data) {
    return request({
      url: '/mobile/dir/query',
      method: 'post',
      loading: true,
      data
    })
}

export function requestMe() {
    return request({
      url: '/mobile/me/query',
      method: 'post',
      loading: true
    })
}

export function linkInfo(panelId) {
    return request({
      url: '/api/link/currentGenerate/'+panelId,
      method: 'post',
      loading: true
    })
}

export function switchLink(data) {
    return request({
      url: '/api/link/switchLink',
      method: 'post',
      loading: true,
      data
    })
}

export function star(panelId) {
    return request({
      url: '/api/store/'+panelId,
      method: 'post',
      loading: true
    })
}

export function unstar(panelId) {
    return request({
      url: '/api/store/remove/'+panelId,
      method: 'post',
      loading: true
    })
}
export function starStatus(panelId) {
    return request({
      url: '/api/store/status/' + panelId,
      method: 'post',
      loading: true
    })
  }
  
    