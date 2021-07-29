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

export function loadGenerate(resourceId) {
  return request({
    url: 'api/link/currentGenerate/' + resourceId,
    method: 'post'
  })
}

export function loadResource(resourceId) {
  return request({
    url: 'api/link/resourceDetail/' + resourceId,
    method: 'post'
  })
}

export function viewInfo(id, data) {
  return request({
    url: 'api/link/viewDetail/' + id,
    method: 'post',
    timeout: 30000,
    hideMsg: true,
    data
  })
}
