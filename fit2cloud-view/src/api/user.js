/* 前后端不分离的登录方式 */
import {get, post, put} from "@/plugins/request"

export function login(data) {
  return post("/samples/user/login", data)
}

export function logout() {
  return post("/samples/user/logout")
}

export function isLogin() {
  return get("/samples/user/is-login")
}

export function getCurrentUser() {
  return get("/samples/user/current")
}

export function updateInfo(id, data) {
  return put("/samples/user/info/update/" + id, data)
}



