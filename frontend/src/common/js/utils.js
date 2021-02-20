import {
  LicenseKey,
  PROJECT_ID,
  REFRESH_SESSION_USER_URL,
  ROLE_ADMIN,
  ROLE_ORG_ADMIN,
  ROLE_TEST_MANAGER,
  ROLE_TEST_USER,
  ROLE_TEST_VIEWER,
  TokenKey
} from "./constants";
import axios from "axios";
import {jsPDF} from "jspdf";

export function hasRole(role) {
  let user = getCurrentUser();
  let roles = user.roles.map(r => r.id);
  return roles.indexOf(role) > -1;
}

// 是否含有某个角色
export function hasRoles(...roles) {
  let user = getCurrentUser();
  let rs = user.roles.map(r => r.id);
  for (let item of roles) {
    if (rs.indexOf(item) > -1) {
      return true;
    }
  }
  return false;
}

export function hasRolePermission(role) {
  let user = getCurrentUser();
  for (let ur of user.userRoles) {
    if (role === ur.roleId) {
      if (ur.roleId === ROLE_ADMIN) {
        return true;
      } else if (ur.roleId === ROLE_ORG_ADMIN && user.lastOrganizationId === ur.sourceId) {
        return true;
      } else if (user.lastWorkspaceId === ur.sourceId) {
        return true;
      }
    }
  }
  return false
}

export function hasLicense() {
  let v = localStorage.getItem(LicenseKey);
  return v === 'valid';
}

//是否含有对应组织或工作空间的角色
export function hasRolePermissions(...roles) {
  for (let role of roles) {
    if (hasRolePermission(role)) {
      return true;
    }
  }
  return false;
}

export function checkoutCurrentOrganization() {
  // 查看当前用户是否是 lastOrganizationId 的组织管理员
  return hasRolePermissions(ROLE_ORG_ADMIN);
}

export function checkoutCurrentWorkspace() {
  // 查看当前用户是否是 lastWorkspaceId 的工作空间用户
  return hasRolePermissions(ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER);
}

export function checkoutTestManagerOrTestUser() {
  return hasRolePermissions(ROLE_TEST_MANAGER, ROLE_TEST_USER);
}

export function getCurrentOrganizationId() {
  let user = getCurrentUser();
  return user.lastOrganizationId;
}

export function getCurrentWorkspaceId() {
  let user = getCurrentUser();
  return user.lastWorkspaceId;
}

export function getCurrentUser() {
  return JSON.parse(localStorage.getItem(TokenKey));
}

export function getCurrentProjectID() {
  return localStorage.getItem(PROJECT_ID);
}

export function saveLocalStorage(response) {
  // 登录信息保存 cookie
  localStorage.setItem(TokenKey, JSON.stringify(response.data));
  let rolesArray = response.data.roles;
  let roles = rolesArray.map(r => r.id);
  // 保存角色
  localStorage.setItem("roles", roles);
}

export function saveLicense(data) {
  // 保存License
  localStorage.setItem(LicenseKey, data);
}


export function refreshSessionAndCookies(sign, sourceId) {
  axios.post(REFRESH_SESSION_USER_URL + "/" + sign + "/" + sourceId).then(r => {
    saveLocalStorage(r.data);
    window.location.reload();
  })
}


export function jsonToMap(jsonStr) {
  let obj = JSON.parse(jsonStr);
  let strMap = new Map();
  for (let k of Object.keys(obj)) {
    strMap.set(k, obj[k]);
  }
  return strMap;
}

export function mapToJson(strMap) {
  let obj = Object.create(null);
  for (let [k, v] of strMap) {
    obj[k] = v;
  }
  return JSON.stringify(obj);
}

// 驼峰转换下划线
export function humpToLine(name) {
  return name.replace(/([A-Z])/g, "_$1").toLowerCase();
}

export function downloadFile(name, content) {
  const blob = new Blob([content]);
  if ("download" in document.createElement("a")) {
    // 非IE下载
    //  chrome/firefox
    let aTag = document.createElement('a');
    aTag.download = name;
    aTag.href = URL.createObjectURL(blob);
    aTag.click();
    URL.revokeObjectURL(aTag.href)
  } else {
    // IE10+下载
    navigator.msSaveBlob(blob, name)
  }
}

export function listenGoBack(callback) {
  //监听浏览器返回操作，关闭该对话框
  if (window.history && window.history.pushState) {
    history.pushState(null, null, document.URL);
    window.addEventListener('popstate', callback);
  }
}

export function removeGoBackListener(callback) {
  window.removeEventListener('popstate', callback);
}

export const uuid = function () {
  let d = new Date().getTime();
  let d2 = (performance && performance.now && (performance.now() * 1000)) || 0;
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    let r = Math.random() * 16;
    if (d > 0) {
      r = (d + r) % 16 | 0;
      d = Math.floor(d / 16);
    } else {
      r = (d2 + r) % 16 | 0;
      d2 = Math.floor(d2 / 16);
    }
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
  });
};

export function getUUID() {
  function S4() {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
  }

  return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4() + S4() + S4());
}


export function exportPdf(name, canvasList) {

  let pdf = new jsPDF('', 'pt', 'a4');

  // 当前页面的当前高度
  let currentHeight = 0;
  for (let canvas of canvasList) {
    if (canvas) {

      let contentWidth = canvas.width;
      let contentHeight = canvas.height;

      //a4纸的尺寸[595.28,841.89]
      let a4Width = 592.28;
      let a4Height = 841.89;

      // html页面生成的canvas在pdf中图片的宽高
      let imgWidth = a4Width;
      let imgHeight = a4Width / contentWidth * contentHeight;

      let pageData = canvas.toDataURL('image/jpeg', 1.0);

      // 当前图片的剩余高度
      let leftHeight = imgHeight;

      // 当前页面的剩余高度
      let blankHeight = a4Height - currentHeight;

      if (leftHeight > blankHeight) {
        //页面偏移
        let position = 0;
        while (leftHeight > 0) {
          // 本次添加占用的高度
          let occupation = a4Height - currentHeight;
          pdf.addImage(pageData, 'JPEG', 0, position + currentHeight, imgWidth, imgHeight);
          currentHeight = leftHeight;
          leftHeight -= occupation;
          position -= occupation;
          //避免添加空白页
          if (leftHeight > 0) {
            pdf.addPage();
            currentHeight = 0;
          }
        }
      } else {
        pdf.addImage(pageData, 'JPEG', 0, currentHeight, imgWidth, imgHeight);
        currentHeight += imgHeight;
      }
    }
  }

  pdf.save(name.replace(" ", "_") + '.pdf');

}

export function windowPrint(id, zoom) {
  //根据div标签ID拿到div中的局部内容
  let bdhtml = window.document.body.innerHTML;
  let el = document.getElementById(id);
  var jubuData = el.innerHTML;
  document.getElementsByTagName('body')[0].style.zoom = zoom;
  //把获取的 局部div内容赋给body标签, 相当于重置了 body里的内容
  window.document.body.innerHTML = jubuData;
  //调用打印功能
  window.print();
  window.document.body.innerHTML = bdhtml;//重新给页面内容赋值；
  return false;
}

export function getBodyUploadFiles(obj, runData) {
  let bodyUploadFiles = [];
  obj.bodyUploadIds = [];
  if (runData) {
    if (runData instanceof Array) {
      runData.forEach(request => {
        _getBodyUploadFiles(request, bodyUploadFiles, obj);
      });
    } else {
      _getBodyUploadFiles(runData, bodyUploadFiles, obj);
    }
  }
  return bodyUploadFiles;
}

export function _getBodyUploadFiles(request, bodyUploadFiles, obj) {
  let body = null;
  if (request.hashTree && request.hashTree.length > 0 && request.hashTree[0].body) {
    body = request.hashTree[0].body;
  } else if (request.body) {
    body = request.body;
  }
  if (body) {
    if (body.kvs) {
      body.kvs.forEach(param => {
        if (param.files) {
          param.files.forEach(item => {
            if (item.file) {
              if (!item.id) {
                let fileId = getUUID().substring(0, 12);
                item.name = item.file.name;
                item.id = fileId;
              }
              obj.bodyUploadIds.push(item.id);
              bodyUploadFiles.push(item.file);
            }
          });
        }
      });
    }
    if (body.binary) {
      body.binary.forEach(param => {
        if (param.files) {
          param.files.forEach(item => {
            if (item.file) {
              if (!item.id) {
                let fileId = getUUID().substring(0, 12);
                item.name = item.file.name;
                item.id = fileId;
              }
              obj.bodyUploadIds.push(item.id);
              bodyUploadFiles.push(item.file);
            }
          });
        }
      });
    }
  }
}
export function handleCtrlSEvent(event, func) {
  if (event.keyCode === 83 && event.ctrlKey) {
    // console.log('拦截到 ctrl + s');//ctrl+s
    func();
    event.preventDefault();
    event.returnValue = false;
    return false;
  }
}
