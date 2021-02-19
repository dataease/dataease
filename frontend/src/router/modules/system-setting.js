import Layout from "@/business/app-layout/horizontal-layout";

const SystemSetting = {
  path: '/system-setting',
  component: Layout,
  name: 'SystemSetting',
  meta: {
    title: "route.system_setting",
    icon: 'el-icon-setting',
    roles: ['admin']
  },
  children: [
    {
      path: 'user-management',
      component: () => import('@/business/system-setting/UserManagement'),
      name: "UserManagement",
      meta: {
        title: "route.user_management",
        roles: ['admin']
      }
    },
    {
      path: 'params-setting',
      component: () => import('@/business/system-setting/ParamsSetting'),
      name: "ParamsSetting",
      meta: {
        title: "route.params_setting",
        roles: ['admin']
      }
    }
  ]
}
export default SystemSetting
