const {success, error} = require("./result-holder")

/* 前后端不分离的接口，用Session验证登录*/
let currentUser

const users = {
  admin: {
    id: "admin",
    name: 'Administrator',
    email: "admin@fit2cloud.com",
    roles: ['admin'],
    language: "zh-CN"
  },
  editor: {
    id: "editor",
    name: 'Editor',
    email: "editor@fit2cloud.com",
    roles: ['editor'],
    language: "zh-CN"
  },
  readonly: {
    id: "readonly",
    name: 'Readonly User',
    email: "readonly@fit2cloud.com",
    roles: ['readonly'],
    language: "zh-CN"
  }
}

module.exports = [
  // user login
  {
    url: '/samples/user/login',
    type: 'post',
    response: config => {
      const {username} = config.body
      const user = users[username];

      // mock error
      if (!user) {
        return error("用户名或密码错误")
      }
      currentUser = user;
      return success(user)
    }
  },

  {
    url: '/samples/user/is-login',
    type: 'get',
    response: () => {
      if (currentUser) {
        return success()
      } else {
        return error()
      }
    }
  },

  // get user info
  {
    url: '/samples/user/current',
    type: 'get',
    response: () => {
      const info = currentUser

      // mock error
      if (!info) {
        return error("用户未登录")
      }

      return success(info)
    }
  },

  // update user info
  {
    url: '/samples/user/info/update',
    type: 'put',
    response: config => {
      const {language} = config.body
      if (currentUser) {
        currentUser.language = language;
      }
      return success(currentUser)
    }
  },

  // user logout
  {
    url: '/samples/user/logout',
    type: 'post',
    response: () => {
      currentUser = undefined;
      return success()
    }
  }
]
