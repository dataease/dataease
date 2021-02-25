const Chart = () => import('@/business/components/chart/Chart');
const ChartHome = () => import('@/business/components/chart/data/ChartHome');

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
    }
  ]
}
