/* 前后端分离的登录方式 */
import {get, post, put} from "@/plugins/request"

export function login(data) {
  return post("/login", data)
}

export function logout() {
  return post("/logout")
}

export function getCurrentUser() {
  return get("/info")
}

export function updateInfo(data) {
  return put("/update", data)
}



