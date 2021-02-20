const requireContext = require.context('@/business/components/xpack/', true, /router\.js$/)

export default {
  path: "/setting",
  name: "Setting",
  components: {
    content: () => import('@/business/components/settings/Setting')
  },
  children: [
    {
      path: 'user',
      component: () => import('@/business/components/settings/system/User'),
      meta: {system: true, title: 'commons.user'}
    },
    {
      path: 'organization',
      component: () => import('@/business/components/settings/system/Organization'),
      meta: {system: true, title: 'commons.organization'}
    },
    {
      path: 'systemworkspace',
      component: () => import('@/business/components/settings/system/SystemWorkspace'),
      meta: {system: true, title: 'commons.workspace'}
    },
    {
      path: 'testresourcepool',
      component: () => import('@/business/components/settings/system/TestResourcePool'),
      meta: {system: true, title: 'commons.test_resource_pool'}
    },
    {
      path: 'systemparametersetting',
      component: () => import('@/business/components/settings/system/SystemParameterSetting'),
      meta: {system: true, title: 'commons.system_parameter_setting'}
    },
    ...requireContext.keys().map(key => requireContext(key).system),
    ...requireContext.keys().map(key => requireContext(key).license),
    {
      path: 'organizationpmnmember',
      component: () => import('@/business/components/settings/organization/OrganizationMember'),
      meta: {organization: true, title: 'commons.member'}
    },
    {
      path: 'organizationworkspace',
      component: () => import('@/business/components/settings/organization/OrganizationWorkspace'),
      meta: {organization: true, title: 'commons.workspace'}
    },
    {
      path: 'serviceintegration',
      component: () => import('@/business/components/settings/organization/ServiceIntegration'),
      meta: {organization: true, title: 'organization.service_integration'}
    },
    {
      path: 'messagesettings',
      component: () => import('@/business/components/settings/organization/MessageSettings'),
      meta: {organization: true, title: 'organization.message_settings'}
    },
    {
      path: 'member',
      component: () => import('@/business/components/settings/workspace/WorkspaceMember'),
      meta: {workspace: true, title: 'commons.member'}
    },
    {
      path: 'testcase/report/template',
      name: 'testCaseReportTemplate',
      component: () => import('@/business/components/settings/workspace/TestCaseReportTemplate'),
      meta: {workspace: true, title: 'test_track.plan_view.report_template'}
    },
    {
      path: 'personsetting',
      component: () => import('@/business/components/settings/personal/PersonSetting'),
      meta: {person: true, title: 'commons.personal_setting'}
    },
    {
      path: 'apikeys',
      component: () => import('@/business/components/settings/personal/ApiKeys'),
      meta: {
        person: true,
        title: 'commons.api_keys',
        roles: ['test_manager', 'test_user', 'test_viewer', 'org_admin', 'admin']
      }
    },
    {
      path: 'project/:type',
      component: () => import('@/business/components/settings/project/MsProject'),
      meta: {project: true, title: 'project.manager'}
    },

  ]
}
