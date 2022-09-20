import request from '@/utils/request'
import { validateDs } from '@/api/system/datasource'

export function engineMode() {
  return request({
    url: '/engine/mode',
    method: 'get',
    loading: true
  })
}

export function engineInfo() {
  return request({
    url: '/engine/info',
    method: 'get',
    loading: true
  })
}

export function validate(data) {
  return request({
    url: '/engine/validate',
    method: 'post',
    loading: true,
    data
  })
}

export function save(data) {
  return request({
    url: '/engine/save',
    method: 'post',
    loading: true,
    data
  })
}


export function dbPreview(data) {
  return request({
    url: '/dataset/table/dbPreview',
    method: 'post',
    loading: true,
    data
  })
}