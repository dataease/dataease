import request from '@/utils/request'

export function validate(data) {
  return request({
    url: 'api/link/validate',
    method: 'post',
    loading: true,
    hideMsg: true,
    data
  })
}

export function validatePwd(data) {
  return request({
    url: 'api/link/validatePwd',
    method: 'post',
    data
  })
}

export function setPwd(data) {
  return request({
    url: 'api/link/resetPwd',
    method: 'post',
    data
  })
}

export function setOverTime(data) {
  return request({
    url: 'api/link/resetOverTime',
    method: 'post',
    data
  })
}

export function switchValid(data) {
  return request({
    url: 'api/link/switchLink',
    method: 'post',
    data
  })
}

export function switchEnablePwd(data) {
  return request({
    url: 'api/link/enablePwd',
    method: 'post',
    data
  })
}

export function viewLinkLog(data) {
  return request({
    url: 'api/link/viewLog',
    method: 'post',
    loading: true,
    data
  })
}

export function loadGenerate(resourceId) {
  return request({
    url: 'api/link/currentGenerate/' + resourceId,
    method: 'post'
  })
}

export function loadResource(resourceId,userId) {
  return request({
    url: 'api/link/resourceDetail/' + resourceId+'/'+ userId,
    method: 'get'
  })
}

export function viewInfo(id, panelId, data) {
  return request({
    url: 'api/link/viewDetail/' + id + '/' + panelId,
    method: 'post',
    hideMsg: true,
    data
  })
}

export function shortUrl(data) {
  return request({
    url: 'api/link/shortUrl',
    method: 'post',
    data
  })
}
