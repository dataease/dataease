import request from '@/common/js/request'

export function requestHome(data) {
    let url = '/mobile/home/query'
    if (data && data.type === 2) {
        url = 'mobile/home/queryShares'
    }
    return request({
      url: url,
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
  
    