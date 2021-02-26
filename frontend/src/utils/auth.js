import Cookies from 'js-cookie'


const TokenKey = 'Authorization'
const tokenCookieExpires = 1

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token, rememberMe) {
  if (rememberMe) {
    return Cookies.set(TokenKey, token, { expires: tokenCookieExpires })
  } else return Cookies.set(TokenKey, token)
}

export function tokenKey(){
    return TokenKey
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}
