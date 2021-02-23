const DataSet = () => import('@/business/components/dataset/DataSet');
const DataSetHome = () => import('@/business/components/dataset/data/DataHome');
const DataSetAddDB = () => import('@/business/components/dataset/data/AddDB');
const DataSetTable = () => import('@/business/components/dataset/data/ViewTable');
// const PerformanceTestHome = () => import('@/business/components/performance/home/PerformanceTestHome')
// const EditPerformanceTest = () => import('@/business/components/performance/test/EditPerformanceTest')
// const PerformanceTestList = () => import('@/business/components/performance/test/PerformanceTestList')
// const PerformanceTestReport = () => import('@/business/components/performance/report/PerformanceTestReport')
// const PerformanceChart = () => import('@/business/components/performance/report/components/PerformanceChart')
// const PerformanceReportView = () => import('@/business/components/performance/report/PerformanceReportView')

export default {
  path: "/dataset",
  name: "dataset",
  redirect: "/dataset/home",
  components: {
    content: DataSet
  },
  children: [
    {
      path: 'home',
      name: 'home',
      component: DataSetHome,
    },
    {
      path: 'add_db',
      name: 'add_db',
      component: DataSetAddDB,
    },
    {
      path: 'table',
      name: 'table',
      component: DataSetTable,
    },
    // {
    //   path: 'test/create',
    //   name: "createPerTest",
    //   component: EditPerformanceTest,
    // },
    // {
    //   path: "test/edit/:testId",
    //   name: "editPerTest",
    //   component: EditPerformanceTest,
    //   props: {
    //     content: (route) => {
    //       return {
    //         ...route.params
    //       }
    //     }
    //   }
    // },
    // {
    //   path: "test/:projectId",
    //   name: "perPlan",
    //   component: PerformanceTestList
    // },
    // {
    //   path: "project/:type",
    //   name: "perProject",
    //   component: MsProject
    // },
    // {
    //   path: "report/:type",
    //   name: "perReport",
    //   component: PerformanceTestReport
    // },
    // {
    //   path: "chart",
    //   name: "perChart",
    //   component: PerformanceChart
    // },
    // {
    //   path: "report/view/:reportId",
    //   name: "perReportView",
    //   component: PerformanceReportView
    // }
  ]
}
