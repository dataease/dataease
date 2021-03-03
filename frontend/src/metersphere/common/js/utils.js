import {
  LicenseKey,
  PROJECT_ID,
  REFRESH_SESSION_USER_URL,
  ROLE_ADMIN,
  ROLE_ORG_ADMIN,
  ROLE_TEST_MANAGER,
  ROLE_TEST_USER,
  ROLE_TEST_VIEWER
} from './constants'
import axios from 'axios'

export function hasRole(role) {
  const user = getCurrentUser()
  const roles = user.roles.map(r => r.id)
  return roles.indexOf(role) > -1
}

// 是否含有某个角色
export function hasRoles(...roles) {
  const user = getCurrentUser()
  const rs = user.roles.map(r => r.id)
  for (const item of roles) {
    if (rs.indexOf(item) > -1) {
      return true
    }
  }
  return false
}

export function hasRolePermission(role) {
  const user = getCurrentUser()
  for (const ur of user.userRoles) {
    if (role === ur.roleId) {
      if (ur.roleId === ROLE_ADMIN) {
        return true
      } else if (ur.roleId === ROLE_ORG_ADMIN && user.lastOrganizationId === ur.sourceId) {
        return true
      } else if (user.lastWorkspaceId === ur.sourceId) {
        return true
      }
    }
  }
  return false
}

export function hasLicense() {
  const v = localStorage.getItem(LicenseKey)
  return v === 'valid'
}

// 是否含有对应组织或工作空间的角色
export function hasRolePermissions(...roles) {
  for (const role of roles) {
    if (hasRolePermission(role)) {
      return true
    }
  }
  return false
}

export function checkoutCurrentOrganization() {
  // 查看当前用户是否是 lastOrganizationId 的组织管理员
  return hasRolePermissions(ROLE_ORG_ADMIN)
}

export function checkoutCurrentWorkspace() {
  // 查看当前用户是否是 lastWorkspaceId 的工作空间用户
  return hasRolePermissions(ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER)
}

export function checkoutTestManagerOrTestUser() {
  return hasRolePermissions(ROLE_TEST_MANAGER, ROLE_TEST_USER)
}

export function getCurrentOrganizationId() {
  const user = getCurrentUser()
  return user.lastOrganizationId
}

export function getCurrentWorkspaceId() {
  const user = getCurrentUser()
  return user.lastWorkspaceId
}

export function getCurrentUser() {
  // return JSON.parse(localStorage.getItem(TokenKey));
  return getUserInfo()
}

export function getCurrentProjectID() {
  return localStorage.getItem(PROJECT_ID)
}

// export function saveLocalStorage(response) {
//   // 登录信息保存 cookie
//   //localStorage.setItem(TokenKey, JSON.stringify(response.data));
//   let rolesArray = response.data.roles;
//   let roles = rolesArray.map(r => r.id);
//   // 保存角色
//   localStorage.setItem("roles", roles);
// }

export function saveLocalStorage(response) {
  // 登录信息保存 cookie
  // localStorage.setItem(TokenKey, JSON.stringify(response.data));
  const rolesArray = response.data.roles
  const roles = rolesArray.map(r => r.id)
  // 保存角色
  localStorage.setItem('roles', roles)
}

export function saveLicense(data) {
  // 保存License
  localStorage.setItem(LicenseKey, data)
}

export function refreshSessionAndCookies(sign, sourceId) {
  axios.post(REFRESH_SESSION_USER_URL + '/' + sign + '/' + sourceId).then(r => {
    saveLocalStorage(r.data)
    window.location.reload()
  })
}

export function jsonToMap(jsonStr) {
  const obj = JSON.parse(jsonStr)
  const strMap = new Map()
  for (const k of Object.keys(obj)) {
    strMap.set(k, obj[k])
  }
  return strMap
}

export function mapToJson(strMap) {
  const obj = Object.create(null)
  for (const [k, v] of strMap) {
    obj[k] = v
  }
  return JSON.stringify(obj)
}

// 驼峰转换下划线
export function humpToLine(name) {
  return name.replace(/([A-Z])/g, '_$1').toLowerCase()
}

export function downloadFile(name, content) {
  const blob = new Blob([content])
  if ('download' in document.createElement('a')) {
    // 非IE下载
    //  chrome/firefox
    const aTag = document.createElement('a')
    aTag.download = name
    aTag.href = URL.createObjectURL(blob)
    aTag.click()
    URL.revokeObjectURL(aTag.href)
  } else {
    // IE10+下载
    navigator.msSaveBlob(blob, name)
  }
}

export function listenGoBack(callback) {
  // 监听浏览器返回操作，关闭该对话框
  if (window.history && window.history.pushState) {
    history.pushState(null, null, document.URL)
    window.addEventListener('popstate', callback)
  }
}

export function removeGoBackListener(callback) {
  window.removeEventListener('popstate', callback)
}

export const uuid = function() {
  let d = new Date().getTime()
  let d2 = (performance && performance.now && (performance.now() * 1000)) || 0
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    let r = Math.random() * 16
    if (d > 0) {
      r = (d + r) % 16 | 0
      d = Math.floor(d / 16)
    } else {
      r = (d2 + r) % 16 | 0
      d2 = Math.floor(d2 / 16)
    }
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16)
  })
}

export function getUUID() {
  function S4() {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1)
  }

  return (S4() + S4() + '-' + S4() + '-' + S4() + '-' + S4() + '-' + S4() + S4() + S4())
}

export function windowPrint(id, zoom) {
  // 根据div标签ID拿到div中的局部内容
  const bdhtml = window.document.body.innerHTML
  const el = document.getElementById(id)
  var jubuData = el.innerHTML
  document.getElementsByTagName('body')[0].style.zoom = zoom
  // 把获取的 局部div内容赋给body标签, 相当于重置了 body里的内容
  window.document.body.innerHTML = jubuData
  // 调用打印功能
  window.print()
  window.document.body.innerHTML = bdhtml// 重新给页面内容赋值；
  return false
}

export function getBodyUploadFiles(obj, runData) {
  const bodyUploadFiles = []
  obj.bodyUploadIds = []
  if (runData) {
    if (runData instanceof Array) {
      runData.forEach(request => {
        _getBodyUploadFiles(request, bodyUploadFiles, obj)
      })
    } else {
      _getBodyUploadFiles(runData, bodyUploadFiles, obj)
    }
  }
  return bodyUploadFiles
}

export function _getBodyUploadFiles(request, bodyUploadFiles, obj) {
  let body = null
  if (request.hashTree && request.hashTree.length > 0 && request.hashTree[0].body) {
    body = request.hashTree[0].body
  } else if (request.body) {
    body = request.body
  }
  if (body) {
    if (body.kvs) {
      body.kvs.forEach(param => {
        if (param.files) {
          param.files.forEach(item => {
            if (item.file) {
              if (!item.id) {
                const fileId = getUUID().substring(0, 12)
                item.name = item.file.name
                item.id = fileId
              }
              obj.bodyUploadIds.push(item.id)
              bodyUploadFiles.push(item.file)
            }
          })
        }
      })
    }
    if (body.binary) {
      body.binary.forEach(param => {
        if (param.files) {
          param.files.forEach(item => {
            if (item.file) {
              if (!item.id) {
                const fileId = getUUID().substring(0, 12)
                item.name = item.file.name
                item.id = fileId
              }
              obj.bodyUploadIds.push(item.id)
              bodyUploadFiles.push(item.file)
            }
          })
        }
      })
    }
  }
}
export function handleCtrlSEvent(event, func) {
  if (event.keyCode === 83 && event.ctrlKey) {
    // console.log('拦截到 ctrl + s');//ctrl+s
    func()
    event.preventDefault()
    event.returnValue = false
    return false
  }
}

// ---------------- dataEase新增
export function setUserInfo(userInfo) {
  localStorage.setItem('userInfo', JSON.stringify(userInfo))
}
export function getUserInfo() {
  return JSON.parse(localStorage.getItem('userInfo'))
}
export function getRoles() {
  const uinfo = getUserInfo()
  return uinfo && uinfo['roles']
}
