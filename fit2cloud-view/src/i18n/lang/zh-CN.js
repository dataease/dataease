import el from "element-ui/lib/locale/lang/zh-CN"; // 加载element的内容
import fu from "fit2cloud-ui/src/locale/lang/zh-CN"; // 加载fit2cloud的内容

const message = {
  commons: {
    message_box: {
      alert: "警告",
      confirm: "确认",
      prompt: "提示",
    },
    button: {
      login: "登录",
      ok: "确定",
      save: "保存",
      delete: "删除",
      cancel: "取消",
      return: "返回",
    },
    msg: {
      success: "{0}成功",
      op_success: "操作成功",
      save_success: "保存成功",
      delete_success: "删除成功",
    },
    validate: {
      limit: '长度在 {0} 到 {1} 个字符',
      input: "请输入{0}",
      select: "请选择{0}",
    },
    personal: {
      personal_information: "个人信息",
      help_documentation: "帮助文档",
      exit_system: "退出系统",
    }
  },
  login: {
    username: "用户名",
    password: "密码",
    title: "登录 FIT2CLOUD",
    welcome: "欢迎回来，请输入用户名和密码登录",
    expires: '认证信息已过期，请重新登录',
  },
  route: {
    system_setting: "系统设置",
    user_management: "用户管理",
    params_setting: "参数设置",
  },
}

export default {
  ...el,
  ...fu,
  ...message
};
