/* 前后端分离的登录方式 */
import {get, post, put} from "@/plugins/request"

export function login(data) {
  return post("/samples/user-token/login", data)
}

export function logout() {
  return post("/samples/user-token/logout")
}

export function getCurrentUser() {
  return get("/samples/user-token/current")
}

export function updateInfo(data) {
  return put("/samples/user-token/update", data)
}



