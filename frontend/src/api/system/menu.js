import request from '@/utils/request'

export function getMenusTree(pid) {
  return request({
    url: 'api/menu/childNodes/' + pid,
    method: 'post',
    loading: true
  })
}

export function getChild(id) {
  return request({
    url: '/api/menu/childMenus/' + id,
    method: 'post'
  })
}

export function buildMenus() {
  return request({
    url: 'api/dynamicMenu/menus',
    method: 'post'
  })
}

export function addMenu(data) {
  return request({
    url: '/api/menu/create',
    method: 'post',
    data
  })
}

export function delMenu(ids) {
  return request({
    url: '/api/menu/delete',
    method: 'post',
    data: ids
  })
}

export function editMenu(data) {
  return request({
    url: '/api/menu/update',
    method: 'post',
    data
  })
}

export function treeByMenuId(menuId) {
  return request({
    url: '/api/menu/nodesByMenuId/' + menuId,
    method: 'post'
  })
}

export function queryCondition(data) {
  return request({
    url: '/api/menu/search',
    method: 'post',
    data
  })
}

export default { addMenu, editMenu, delMenu, getMenusTree, getChild, treeByMenuId, queryCondition }
