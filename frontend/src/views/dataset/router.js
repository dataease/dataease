const DataSet = () => import('./index')
const DataSetHome = () => import('./data/DataHome')
const DataSetTable = () => import('./data/ViewTable')
const DataSetAddDB = () => import('./add/AddDB')

export default {
  path: '/dataset',
  name: 'dataset',
  redirect: '/dataset/home',
  components: {
    content: DataSet
  },
  children: [
    {
      path: 'home',
      name: 'home',
      component: DataSetHome
    },
    {
      path: 'add_db',
      name: 'add_db',
      component: DataSetAddDB
    },
    {
      path: 'table',
      name: 'table',
      component: DataSetTable
    }
  ]
}
