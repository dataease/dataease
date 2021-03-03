import request from '@/utils/request'

export function userLists(page, size, data) {
  return request({
    url: 'api/user/userGrid/' + page + '/' + size,
    method: 'post',
    data
  })
}
