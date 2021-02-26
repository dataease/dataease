const Chart = () => import('@/business/components/chart/Chart');
const ChartHome = () => import('@/business/components/chart/data/ChartHome');
const ChartEdit =() => import('@/business/components/chart/view/ChartEdit')

export default {
  path: "/chart",
  name: "ChartGroup",
  redirect: "/chart/home",
  components: {
    content: Chart,
  },
  children: [
    {
      path: 'home',
      name: 'home',
      component: ChartHome,
    },
    {
      path: 'chart-edit',
      name: 'chart-edit',
      component: ChartEdit,
    }
  ]
}
