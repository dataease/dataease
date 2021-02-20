
const TestTrack = () => import('@/business/components/track/TestTrack')
const TrackHome = () => import('@/business/components/track/home/TrackHome')
const TestCase = () => import('@/business/components/track/case/TestCase')
const TestPlan = () => import('@/business/components/track/plan/TestPlan')
const TestCaseReview = () => import('@/business/components/track/review/TestCaseReview')
const TestCaseReviewView = () => import('@/business/components/track/review/view/TestCaseReviewView')
const TestPlanView = () => import('@/business/components/track/plan/view/TestPlanView')
const reportListView = () => import('@/business/components/track/report/TestPlanReport')
// const reportListView = () => import('@/business/components/track/plan/TestPlan')

export default {
  path: "/track",
  name: "track",
  redirect: "/track/home",
  components: {
    content: TestTrack
  },
  children: [
    {
      path: 'home',
      name: 'trackHome',
      component: TrackHome,
    },
    {
      path: 'case/create',
      name: 'testCaseCreate',
      component: TestCase,
    },
    {
      path: 'case/:projectId',
      name: 'testCase',
      component: TestCase,
    },
    {
      path: 'case/edit/:caseId',
      name: 'testCaseEdit',
      component: TestCase,
    },
    {
      path: 'testPlan/reportList',
      name: 'testPlanReportList',
      component: reportListView,
    },
    {
      path: "plan/:type",
      name: "testPlan",
      component: TestPlan
    },
    {
      path: "plan/view/:planId",
      name: "planView",
      component: TestPlanView
    },
    {
      path: "plan/view/edit/:caseId",
      name: "planViewEdit",
      component: TestPlanView
    },
    // {
    //   path: "project/:type",
    //   name: "trackProject",
    //   component: MsProject
    // },
    {
      path: "review/:type",
      name: "testCaseReview",
      component: TestCaseReview
    },
    {
      path: "review/view/:reviewId",
      name: "testCaseReviewView",
      component: TestCaseReviewView
    },
  ]
}
