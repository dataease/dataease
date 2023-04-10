<script lang="ts" setup>
import { ref, nextTick, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import DatasetUnion from './DatasetUnion.vue'
interface DragEvent extends MouseEvent {
  dataTransfer: DataTransfer
}
const { t } = useI18n()

const datasetName = ref('新建数据源')
const originName = ref('')
const activeName = ref('')
const dataSource = ref('')
const searchTable = ref('')
const showInput = ref(false)
const dsLoading = ref(false)
const LeftWidth = ref(240)
const offsetX = ref(0)
const offsetY = ref(0)
const showLeft = ref(true)
const maskShow = ref(false)
const loading = ref(false)
const nameExist = ref(false)
const datasetType = ref('sql')
const editerName = ref()

const state = reactive({
  nameList: [],
  dataSourceList: [],
  tableData: [],
  table: {
    name: '',
    id: ''
  }
})

state.tableData = [
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'dataset_column_permissions',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'dataset_group',
    remark: '',
    enableCheck: false,
    datasetPath: '0-大数据集测试/test56/0wjh4_dataset_group'
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'dataset_row_permissions',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'dataset_row_permissions_tree',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'dataset_sql_log',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'dataset_table',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'dataset_table_field',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'dataset_table_function',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'dataset_table_incremental_config',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'dataset_table_task',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'dataset_table_task_log',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'dataset_table_union',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'datasource',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'de_driver',
    remark: '驱动',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'de_driver_details',
    remark: '驱动详情',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'de_engine',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_alt_4a',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_alt_4a_5a',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_alt_5a',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_alt_region',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_alt_tourist_attractions',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_alt_tourists_total',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_alt_tourists_type',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_domestic_epidemic',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_gdp_2021',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_gdp_by_city',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_gdp_by_city_top10',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_gdp_by_industry',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_gdp_district_top100',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_gdp_history',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_hntv_age',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_hntv_keywords',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_hntv_labels',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_hntv_media',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_hntv_messages',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_hntv_region',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_hntv_shows',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_hntv_topics',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_sales_dashboard',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_stny_carbon dioxide_emissions',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_stny_carbon_emission_trend',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_stny_disposable_energy',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_stny_energy_consumption_proportion',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_stny_energy_consumption_total',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'demo_stny_province_city_index',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'file_content',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'file_metadata',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'license',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'my_plugin',
    remark: '插件表',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_app_template',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_app_template_log',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_design',
    remark: '仪表板和组件的关联关系 组件分为普通视图和系统组件',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_group',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_group_extend',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_group_extend_data',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_link',
    remark: '仪表板链接',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_link_jump',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_link_jump_info',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_link_jump_target_view_info',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_link_mapping',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_outer_params',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_outer_params_info',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_outer_params_target_view_info',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_pdf_template',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_share',
    remark: '仪表板分享',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_store',
    remark: '仪表板收藏',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_subject',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_template',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_view',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_view_linkage',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_view_linkage_field',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'panel_watermark',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'plugin_sys_menu',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'qrtz_blob_triggers',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'qrtz_calendars',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'qrtz_cron_triggers',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'qrtz_fired_triggers',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'qrtz_job_details',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'qrtz_locks',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'qrtz_paused_trigger_grps',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'qrtz_scheduler_state',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'qrtz_simple_triggers',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'qrtz_simprop_triggers',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'qrtz_triggers',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'schedule',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_auth',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_auth_detail',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_background_image',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_dept',
    remark: '组织机构',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_external_token',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_log',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_login_limit',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_menu',
    remark: '系统菜单',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_msg',
    remark: '消息通知表',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_msg_channel',
    remark: '消息渠道表',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_msg_setting',
    remark: '消息设置表',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_msg_type',
    remark: '消息类型表',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_param_assist',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_role',
    remark: '角色表',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_roles_menus',
    remark: '角色菜单关联',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_startup_job',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_task',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_task_email',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_task_instance',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_theme',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_theme_item',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_user',
    remark: '系统用户',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_user_assist',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'sys_users_roles',
    remark: '用户角色关联',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'system_parameter',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'task_instance',
    remark: '',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'user_key',
    remark: '用户KEY',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'v_auth_model',
    remark: 'VIEW',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'v_auth_privilege',
    remark: 'VIEW',
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '986e4e63-bdb9-41fc-9695-512cea79ae59',
    name: 'v_history_chart_view',
    remark: 'VIEW',
    enableCheck: true,
    datasetPath: null
  }
]

const dragstart = (e: DragEvent, ele: string) => {
  offsetX.value = e.offsetX
  offsetY.value = e.offsetY
  e.dataTransfer.setData('text/plain', ele.name)
  maskShow.value = true
}
const setActiveName = ({ name, datasourceId, enableCheck }) => {
  if (!enableCheck) return
  activeName.value = name
}

const showTableNameWithComment = t => {
  if (t.remark) {
    return `${t.name}(${t.remark})`
  } else {
    return `${t.name}`
  }
}

const mousedownDrag = () => {
  document.querySelector('.dataset-db').addEventListener('mousemove', calculateHeight)
}
const mouseupDrag = () => {
  document.querySelector('.dataset-db').removeEventListener('mousemove', calculateHeight)
}
const calculateHeight = (e: MouseEvent) => {
  if (e.pageX < 240) {
    LeftWidth.value = 240
    return
  }
  if (e.pageX > 500) {
    LeftWidth.value = 500
    return
  }
  LeftWidth.value = e.pageX
}

const nameExistValidator = () => {
  if (!state.nameList || state.nameList.length === 0) {
    nameExist.value = false
    return
  }
  nameExist.value = state.nameList.some(
    name => name === state.table.name && name !== originName.value
  )
}

const nameBlur = () => {
  nameExistValidator()
  showInput.value = nameExist.value
}

const datasetSave = () => {
  console.log('datasetSave')
}
const handleClick = () => {
  showInput.value = true
  nextTick(() => {
    editerName.value.focus()
  })
}
</script>

<template>
  <div class="de-dataset-form" v-loading="loading">
    <div class="top">
      <span class="name">
        <el-icon>
          <Icon :name="`de-${datasetType}-new`"></Icon>
        </el-icon>
        <template v-if="showInput">
          <el-input ref="editerName" v-model="state.table.name" @blur="nameBlur" />
          <div v-if="nameExist" style="left: 55px" class="el-form-item__error">
            {{ t('deDataset.already_exists') }}
          </div>
        </template>
        <template v-else>
          <span style="margin: 0 5px">{{ datasetName }}</span>
          <el-icon style="margin-left: 5px" @click="handleClick">
            <Icon name="icon_edit_outlined"></Icon>
          </el-icon>
        </template>
      </span>
      <span class="oprate">
        <el-button type="primary" @click="datasetSave">保存</el-button>
      </span>
    </div>
    <div class="container dataset-db" @mouseup="mouseupDrag">
      <p v-show="!showLeft" class="arrow-right" @click="showLeft = true">
        <el-icon>
          <Icon name="icon_down-right_outlined"></Icon>
        </el-icon>
      </p>
      <div
        v-show="showLeft"
        :style="{ left: LeftWidth + 'px' }"
        class="drag-left"
        @mousedown="mousedownDrag"
      />
      <div
        v-loading="dsLoading"
        v-show="showLeft"
        class="table-list"
        :style="{ width: LeftWidth + 'px' }"
      >
        <p class="select-ds">
          选择数据源
          <el-icon @click="showLeft = false">
            <Icon name="icon_up-left_outlined"></Icon>
          </el-icon>
        </p>
        <el-select
          v-model="dataSource"
          class="ds-list"
          filterable
          :placeholder="t('dataset.pls_slc_data_source')"
          size="small"
        >
          <el-option
            v-for="item in state.dataSourceList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <p class="select-ds">{{ t('datasource.data_table') }}</p>
        <el-input
          v-model="searchTable"
          class="search"
          :placeholder="t('deDataset.by_table_name')"
          clearable
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
        <div v-if="!state.tableData.length && searchTable !== ''" class="el-empty">
          <div class="el-empty__description" style="margin-top: 80px; color: #5e6d82">
            没有找到相关内容
          </div>
        </div>
        <div v-else class="table-checkbox-list">
          <template v-for="ele in state.tableData" :key="ele.name">
            <el-tooltip
              :disabled="ele.enableCheck"
              effect="dark"
              :content="t('dataset.table_already_add_to') + ': ' + ele.datasetPath"
              placement="right"
            >
              <div
                :class="[{ active: activeName === ele.name, 'not-allow': !ele.enableCheck }]"
                class="list-item_primary"
                :title="ele.name"
                @dragstart="$event => dragstart($event, ele)"
                @dragend="maskShow = false"
                draggable="true"
                @click="setActiveName(ele)"
              >
                <span class="label">{{ showTableNameWithComment(ele) }}</span>
                <span v-if="ele.nameExist" class="error-name-exist">
                  <el-icon>
                    <Icon name="exclamationmark"></Icon>
                  </el-icon>
                </span>
              </div>
            </el-tooltip>
          </template>
        </div>
      </div>
      <dataset-union :maskShow="maskShow" :offsetX="offsetX" :offsetY="offsetY"></dataset-union>
    </div>
  </div>
</template>

<style lang="less" scoped>
.de-dataset-form {
  .top {
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    box-shadow: 0 2px 2px 0 rgb(0 0 0 / 10%);

    .name {
      font-family: PingFang SC;
      font-size: 16px;
      font-weight: 500;
      display: flex;
      align-items: center;
      width: 50%;
      position: relative;

      .el-input {
        min-width: 96px;
        .el-input__inner {
          line-height: 24px;
          height: 24px;
        }
      }
      i {
        cursor: pointer;
      }
    }
  }

  .container {
    width: 100%;
    height: calc(100vh - 56px);
    position: relative;
    .drag-left {
      position: absolute;
      height: calc(100vh - 56px);
      width: 2px;
      top: 0;
      z-index: 5;
      cursor: col-resize;
    }

    .arrow-right {
      position: absolute;
      top: 15px;
      z-index: 2;
      cursor: pointer;
      margin: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      left: 0;
      height: 24px;
      width: 20px;
      box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);
      border: 1px solid var(--deCardStrokeColor, #dee0e3);
      border-top-right-radius: 13px;
      border-bottom-right-radius: 13px;
    }

    .table-list {
      p {
        margin: 0;
      }

      height: 100%;
      width: 240px;
      padding: 16px 12px;
      font-family: PingFang SC;
      border-right: 1px solid rgba(31, 35, 41, 0.15);

      .select-ds {
        font-size: 14px;
        font-weight: 500;
        display: flex;
        justify-content: space-between;
        color: var(--deTextPrimary, #1f2329);

        i {
          cursor: pointer;
          font-size: 12px;
          color: var(--deTextPlaceholder, #8f959e);
        }
      }

      .search {
        margin: 12px 0;
      }

      .ds-list {
        margin: 12px 0 24px 0;
        width: 100%;
      }

      .table-checkbox-list {
        height: calc(100% - 190px);
        overflow-y: auto;

        .not-allow {
          cursor: not-allowed;
          color: var(--deTextDisable, #bbbfc4);
        }
      }
    }
  }

  .dataset-db {
    display: flex;
  }
}
</style>
