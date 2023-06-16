<script lang="tsx" setup>
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { Icon } from '@/components/icon-custom'
import { ElIcon, ElDropdown, ElDropdownItem, ElDropdownMenu } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { ref, nextTick, shallowRef, reactive, computed, PropType, toRefs } from 'vue'
import { ElTree, ElMessage, ElMessageBox } from 'element-plus-secondary'
import { save } from '@/api/datasource'
import type { Action } from 'element-plus-secondary'
import { Base64 } from 'js-base64'
export interface Param {
  editType: number
  id: number
  table: {
    name: string
  }
}

export interface Field {
  accuracy: number
  originName: string
  fieldSize: number
  fieldType: string
  name: string
}
const props = defineProps({
  param: {
    required: false,
    default() {
      return reactive<{
        id: number
        name: string
        desc: string
        type: string
        editType: number
      }>({
        id: 0,
        name: '',
        desc: '',
        type: 'Excel',
        editType: 0
      })
    },
    type: Object
  },
  editDs: {
    required: false,
    default: false,
    type: Boolean
  }
})

const { param, editDs } = toRefs(props)

const dsFormDisabled = ref(false)

const { t } = useI18n()
const token = 'token'
const RefreshTokenKey = 'RefreshTokenKey'

const baseUrl = ''
const headers = {
  Authorization: token,
  'Accept-Language': 'zh'
}
const defaultProps = {
  label: 'excelLabel',
  children: 'sheets'
}

const mockData = {
  id: 'd648c91d-3f4f-478b-804f-fef773d10e2d',
  excelLabel: '测试Excel导入2.xlsx',
  sheets: [
    {
      excelLabel: 'Sheet1',
      data: [
        [
          '2020年',
          '77620.2',
          '77790.6',
          '13552.1',
          '53302.5',
          '3662.5',
          '4664.7',
          '47.5',
          '217.9',
          '77620.2',
          '1422.1',
          '52353.4',
          '1011.1',
          '1751',
          '3169',
          '6517',
          '11396.5',
          '74386.7',
          '49120',
          '3233.5'
        ],
        [
          '2019年',
          '74866.3',
          '75034.3',
          '13044.4',
          '52201.5',
          '3483.5',
          '4060.3',
          '48.6',
          '216.5',
          '74866.1',
          '1336.2',
          '50698.3',
          '991.2',
          '1752.3',
          '3187.1',
          '6263.8',
          '10637.2',
          '71536',
          '47368.2',
          '3330.1'
        ],
        [
          '2018年',
          '71509.2',
          '71661.3',
          '12317.9',
          '50963.2',
          '2943.6',
          '3659.7',
          '56.9',
          '209.1',
          '71508.2',
          '1242.5',
          '49094.9',
          '887.8',
          '1608.5',
          '2900.4',
          '5716.5',
          '10057.6',
          '68156.5',
          '45743.2',
          '3351.7'
        ],
        [
          '2017年',
          '65914',
          '66044.5',
          '11978.7',
          '47546',
          '2480.7',
          '2972.3',
          '64.2',
          '194.7',
          '65914',
          '1175.1',
          '46052.8',
          '789.2',
          '1418',
          '2526.6',
          '4880.6',
          '9071.6',
          '62718.1',
          '42857',
          '3195.8'
        ],
        [
          '2016年',
          '61204.4',
          '61331.6',
          '11840.5',
          '44370.7',
          '2132.9',
          '2370.7',
          '61.9',
          '189.1',
          '61205.1',
          '1091.9',
          '42996.9',
          '725.6',
          '1251.5',
          '2323.8',
          '4394.8',
          '8420.6',
          '58142.2',
          '39934',
          '3062.9'
        ],
        [
          '2015年',
          '58021.3',
          '58145.7',
          '11302.7',
          '42841.9',
          '1707.9',
          '1857.7',
          '62.1',
          '186.5',
          '58020',
          '1039.8',
          '41550',
          '698.7',
          '1125.6',
          '2122',
          '3918.6',
          '7565.2',
          '55032.1',
          '38562.1',
          '2987.9'
        ],
        [
          '2014年',
          '57830.5',
          '57944.6',
          '10728.8',
          '44001.1',
          '1325.4',
          '1599.8',
          '67.5',
          '181.6',
          '57829.7',
          '1013.4',
          '42248.7',
          '721.7',
          '1059.2',
          '1995.6',
          '3615',
          '7176.1',
          '54729.8',
          '39148.8',
          '3099.9'
        ],
        [
          '2013年',
          '54204.1',
          '54316.4',
          '9202.9',
          '42470.1',
          '1116.1',
          '1412',
          '74.4',
          '186.7',
          '54203.4',
          '1026.9',
          '39236.9',
          '675.1',
          '1000.9',
          '1876.9',
          '3397.6',
          '6989.2',
          '51062.7',
          '36096.2',
          '3140.7'
        ],
        [
          '2012年',
          '49767.7',
          '49875.5',
          '8721.1',
          '38928.1',
          '973.9',
          '959.8',
          '68.7',
          '176.5',
          '49762.6',
          '1012.6',
          '36232.2',
          '608.4',
          '915.4',
          '1691.5',
          '3083.6',
          '6219',
          '46866.5',
          '33336.1',
          '2896.2'
        ]
      ],
      fields: [
        {
          fieldName: '指标',
          remarks: '指标',
          fieldType: 'TEXT',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '电力可供量(亿千瓦小时)',
          remarks: '电力可供量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '电力生产量(亿千瓦小时)',
          remarks: '电力生产量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '水电生产电力量(亿千瓦小时)',
          remarks: '水电生产电力量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '火电生产电力量(亿千瓦小时)',
          remarks: '火电生产电力量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '核电生产电力量(亿千瓦小时)',
          remarks: '核电生产电力量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '风电生产电力量(亿千瓦小时)',
          remarks: '风电生产电力量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '电力进口量(亿千瓦小时)',
          remarks: '电力进口量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '电力出口量(亿千瓦小时)',
          remarks: '电力出口量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '电力能源消费总量(亿千瓦小时)',
          remarks: '电力能源消费总量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '农、林、牧、渔业电力消费总量(亿千瓦小时)',
          remarks: '农、林、牧、渔业电力消费总量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '工业电力消费总量(亿千瓦小时)',
          remarks: '工业电力消费总量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '建筑业电力消费总量(亿千瓦小时)',
          remarks: '建筑业电力消费总量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '交通运输、仓储和邮政业电力消费总量(亿千瓦小时)',
          remarks: '交通运输、仓储和邮政业电力消费总量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '批发和零售业、住宿和餐饮业电力消费总量(亿千瓦小时)',
          remarks: '批发和零售业、住宿和餐饮业电力消费总量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '其他电力消费总量(亿千瓦小时)',
          remarks: '其他电力消费总量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '居民生活电力消费总量(亿千瓦小时)',
          remarks: '居民生活电力消费总量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '电力终端消费量(亿千瓦小时)',
          remarks: '电力终端消费量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '工业终端电力消费量(亿千瓦小时)',
          remarks: '工业终端电力消费量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        },
        {
          fieldName: '输配电损失量(亿千瓦小时)',
          remarks: '输配电损失量(亿千瓦小时)',
          fieldType: 'DOUBLE',
          fieldSize: 65533,
          accuracy: 0
        }
      ],
      jsonArray: [
        {
          '农、林、牧、渔业电力消费总量(亿千瓦小时)': '1422.1',
          '工业终端电力消费量(亿千瓦小时)': '49120',
          '核电生产电力量(亿千瓦小时)': '3662.5',
          '其他电力消费总量(亿千瓦小时)': '6517',
          '电力进口量(亿千瓦小时)': '47.5',
          '电力生产量(亿千瓦小时)': '77790.6',
          '风电生产电力量(亿千瓦小时)': '4664.7',
          '电力可供量(亿千瓦小时)': '77620.2',
          '电力出口量(亿千瓦小时)': '217.9',
          指标: '2020年',
          '交通运输、仓储和邮政业电力消费总量(亿千瓦小时)': '1751',
          '居民生活电力消费总量(亿千瓦小时)': '11396.5',
          '电力终端消费量(亿千瓦小时)': '74386.7',
          '电力能源消费总量(亿千瓦小时)': '77620.2',
          '批发和零售业、住宿和餐饮业电力消费总量(亿千瓦小时)': '3169',
          '火电生产电力量(亿千瓦小时)': '53302.5',
          '工业电力消费总量(亿千瓦小时)': '52353.4',
          '输配电损失量(亿千瓦小时)': '3233.5',
          '建筑业电力消费总量(亿千瓦小时)': '1011.1',
          '水电生产电力量(亿千瓦小时)': '13552.1'
        },
        {
          '农、林、牧、渔业电力消费总量(亿千瓦小时)': '1336.2',
          '工业终端电力消费量(亿千瓦小时)': '47368.2',
          '核电生产电力量(亿千瓦小时)': '3483.5',
          '其他电力消费总量(亿千瓦小时)': '6263.8',
          '电力进口量(亿千瓦小时)': '48.6',
          '电力生产量(亿千瓦小时)': '75034.3',
          '风电生产电力量(亿千瓦小时)': '4060.3',
          '电力可供量(亿千瓦小时)': '74866.3',
          '电力出口量(亿千瓦小时)': '216.5',
          指标: '2019年',
          '交通运输、仓储和邮政业电力消费总量(亿千瓦小时)': '1752.3',
          '居民生活电力消费总量(亿千瓦小时)': '10637.2',
          '电力终端消费量(亿千瓦小时)': '71536',
          '电力能源消费总量(亿千瓦小时)': '74866.1',
          '批发和零售业、住宿和餐饮业电力消费总量(亿千瓦小时)': '3187.1',
          '火电生产电力量(亿千瓦小时)': '52201.5',
          '工业电力消费总量(亿千瓦小时)': '50698.3',
          '输配电损失量(亿千瓦小时)': '3330.1',
          '建筑业电力消费总量(亿千瓦小时)': '991.2',
          '水电生产电力量(亿千瓦小时)': '13044.4'
        },
        {
          '农、林、牧、渔业电力消费总量(亿千瓦小时)': '1242.5',
          '工业终端电力消费量(亿千瓦小时)': '45743.2',
          '核电生产电力量(亿千瓦小时)': '2943.6',
          '其他电力消费总量(亿千瓦小时)': '5716.5',
          '电力进口量(亿千瓦小时)': '56.9',
          '电力生产量(亿千瓦小时)': '71661.3',
          '风电生产电力量(亿千瓦小时)': '3659.7',
          '电力可供量(亿千瓦小时)': '71509.2',
          '电力出口量(亿千瓦小时)': '209.1',
          指标: '2018年',
          '交通运输、仓储和邮政业电力消费总量(亿千瓦小时)': '1608.5',
          '居民生活电力消费总量(亿千瓦小时)': '10057.6',
          '电力终端消费量(亿千瓦小时)': '68156.5',
          '电力能源消费总量(亿千瓦小时)': '71508.2',
          '批发和零售业、住宿和餐饮业电力消费总量(亿千瓦小时)': '2900.4',
          '火电生产电力量(亿千瓦小时)': '50963.2',
          '工业电力消费总量(亿千瓦小时)': '49094.9',
          '输配电损失量(亿千瓦小时)': '3351.7',
          '建筑业电力消费总量(亿千瓦小时)': '887.8',
          '水电生产电力量(亿千瓦小时)': '12317.9'
        },
        {
          '农、林、牧、渔业电力消费总量(亿千瓦小时)': '1175.1',
          '工业终端电力消费量(亿千瓦小时)': '42857',
          '核电生产电力量(亿千瓦小时)': '2480.7',
          '其他电力消费总量(亿千瓦小时)': '4880.6',
          '电力进口量(亿千瓦小时)': '64.2',
          '电力生产量(亿千瓦小时)': '66044.5',
          '风电生产电力量(亿千瓦小时)': '2972.3',
          '电力可供量(亿千瓦小时)': '65914',
          '电力出口量(亿千瓦小时)': '194.7',
          指标: '2017年',
          '交通运输、仓储和邮政业电力消费总量(亿千瓦小时)': '1418',
          '居民生活电力消费总量(亿千瓦小时)': '9071.6',
          '电力终端消费量(亿千瓦小时)': '62718.1',
          '电力能源消费总量(亿千瓦小时)': '65914',
          '批发和零售业、住宿和餐饮业电力消费总量(亿千瓦小时)': '2526.6',
          '火电生产电力量(亿千瓦小时)': '47546',
          '工业电力消费总量(亿千瓦小时)': '46052.8',
          '输配电损失量(亿千瓦小时)': '3195.8',
          '建筑业电力消费总量(亿千瓦小时)': '789.2',
          '水电生产电力量(亿千瓦小时)': '11978.7'
        },
        {
          '农、林、牧、渔业电力消费总量(亿千瓦小时)': '1091.9',
          '工业终端电力消费量(亿千瓦小时)': '39934',
          '核电生产电力量(亿千瓦小时)': '2132.9',
          '其他电力消费总量(亿千瓦小时)': '4394.8',
          '电力进口量(亿千瓦小时)': '61.9',
          '电力生产量(亿千瓦小时)': '61331.6',
          '风电生产电力量(亿千瓦小时)': '2370.7',
          '电力可供量(亿千瓦小时)': '61204.4',
          '电力出口量(亿千瓦小时)': '189.1',
          指标: '2016年',
          '交通运输、仓储和邮政业电力消费总量(亿千瓦小时)': '1251.5',
          '居民生活电力消费总量(亿千瓦小时)': '8420.6',
          '电力终端消费量(亿千瓦小时)': '58142.2',
          '电力能源消费总量(亿千瓦小时)': '61205.1',
          '批发和零售业、住宿和餐饮业电力消费总量(亿千瓦小时)': '2323.8',
          '火电生产电力量(亿千瓦小时)': '44370.7',
          '工业电力消费总量(亿千瓦小时)': '42996.9',
          '输配电损失量(亿千瓦小时)': '3062.9',
          '建筑业电力消费总量(亿千瓦小时)': '725.6',
          '水电生产电力量(亿千瓦小时)': '11840.5'
        },
        {
          '农、林、牧、渔业电力消费总量(亿千瓦小时)': '1039.8',
          '工业终端电力消费量(亿千瓦小时)': '38562.1',
          '核电生产电力量(亿千瓦小时)': '1707.9',
          '其他电力消费总量(亿千瓦小时)': '3918.6',
          '电力进口量(亿千瓦小时)': '62.1',
          '电力生产量(亿千瓦小时)': '58145.7',
          '风电生产电力量(亿千瓦小时)': '1857.7',
          '电力可供量(亿千瓦小时)': '58021.3',
          '电力出口量(亿千瓦小时)': '186.5',
          指标: '2015年',
          '交通运输、仓储和邮政业电力消费总量(亿千瓦小时)': '1125.6',
          '居民生活电力消费总量(亿千瓦小时)': '7565.2',
          '电力终端消费量(亿千瓦小时)': '55032.1',
          '电力能源消费总量(亿千瓦小时)': '58020',
          '批发和零售业、住宿和餐饮业电力消费总量(亿千瓦小时)': '2122',
          '火电生产电力量(亿千瓦小时)': '42841.9',
          '工业电力消费总量(亿千瓦小时)': '41550',
          '输配电损失量(亿千瓦小时)': '2987.9',
          '建筑业电力消费总量(亿千瓦小时)': '698.7',
          '水电生产电力量(亿千瓦小时)': '11302.7'
        },
        {
          '农、林、牧、渔业电力消费总量(亿千瓦小时)': '1013.4',
          '工业终端电力消费量(亿千瓦小时)': '39148.8',
          '核电生产电力量(亿千瓦小时)': '1325.4',
          '其他电力消费总量(亿千瓦小时)': '3615',
          '电力进口量(亿千瓦小时)': '67.5',
          '电力生产量(亿千瓦小时)': '57944.6',
          '风电生产电力量(亿千瓦小时)': '1599.8',
          '电力可供量(亿千瓦小时)': '57830.5',
          '电力出口量(亿千瓦小时)': '181.6',
          指标: '2014年',
          '交通运输、仓储和邮政业电力消费总量(亿千瓦小时)': '1059.2',
          '居民生活电力消费总量(亿千瓦小时)': '7176.1',
          '电力终端消费量(亿千瓦小时)': '54729.8',
          '电力能源消费总量(亿千瓦小时)': '57829.7',
          '批发和零售业、住宿和餐饮业电力消费总量(亿千瓦小时)': '1995.6',
          '火电生产电力量(亿千瓦小时)': '44001.1',
          '工业电力消费总量(亿千瓦小时)': '42248.7',
          '输配电损失量(亿千瓦小时)': '3099.9',
          '建筑业电力消费总量(亿千瓦小时)': '721.7',
          '水电生产电力量(亿千瓦小时)': '10728.8'
        },
        {
          '农、林、牧、渔业电力消费总量(亿千瓦小时)': '1026.9',
          '工业终端电力消费量(亿千瓦小时)': '36096.2',
          '核电生产电力量(亿千瓦小时)': '1116.1',
          '其他电力消费总量(亿千瓦小时)': '3397.6',
          '电力进口量(亿千瓦小时)': '74.4',
          '电力生产量(亿千瓦小时)': '54316.4',
          '风电生产电力量(亿千瓦小时)': '1412',
          '电力可供量(亿千瓦小时)': '54204.1',
          '电力出口量(亿千瓦小时)': '186.7',
          指标: '2013年',
          '交通运输、仓储和邮政业电力消费总量(亿千瓦小时)': '1000.9',
          '居民生活电力消费总量(亿千瓦小时)': '6989.2',
          '电力终端消费量(亿千瓦小时)': '51062.7',
          '电力能源消费总量(亿千瓦小时)': '54203.4',
          '批发和零售业、住宿和餐饮业电力消费总量(亿千瓦小时)': '1876.9',
          '火电生产电力量(亿千瓦小时)': '42470.1',
          '工业电力消费总量(亿千瓦小时)': '39236.9',
          '输配电损失量(亿千瓦小时)': '3140.7',
          '建筑业电力消费总量(亿千瓦小时)': '675.1',
          '水电生产电力量(亿千瓦小时)': '9202.9'
        },
        {
          '农、林、牧、渔业电力消费总量(亿千瓦小时)': '1012.6',
          '工业终端电力消费量(亿千瓦小时)': '33336.1',
          '核电生产电力量(亿千瓦小时)': '973.9',
          '其他电力消费总量(亿千瓦小时)': '3083.6',
          '电力进口量(亿千瓦小时)': '68.7',
          '电力生产量(亿千瓦小时)': '49875.5',
          '风电生产电力量(亿千瓦小时)': '959.8',
          '电力可供量(亿千瓦小时)': '49767.7',
          '电力出口量(亿千瓦小时)': '176.5',
          指标: '2012年',
          '交通运输、仓储和邮政业电力消费总量(亿千瓦小时)': '915.4',
          '居民生活电力消费总量(亿千瓦小时)': '6219',
          '电力终端消费量(亿千瓦小时)': '46866.5',
          '电力能源消费总量(亿千瓦小时)': '49762.6',
          '批发和零售业、住宿和餐饮业电力消费总量(亿千瓦小时)': '1691.5',
          '火电生产电力量(亿千瓦小时)': '38928.1',
          '工业电力消费总量(亿千瓦小时)': '36232.2',
          '输配电损失量(亿千瓦小时)': '2896.2',
          '建筑业电力消费总量(亿千瓦小时)': '608.4',
          '水电生产电力量(亿千瓦小时)': '8721.1'
        }
      ],
      tableName: '测试Excel导入2',
      sheetExcelId: 'd648c91d-3f4f-478b-804f-fef773d10e2d',
      id: 'd9e64c0b-9a2a-4d55-b5c6-4f7760cbc05e',
      path: '/opt/dataease/data/kettle/admin/d648c91d-3f4f-478b-804f-fef773d10e2d.xlsx',
      fieldsMd5: 'c7e2ec2237cf0e0d61a400b09f3bd61c',
      changeFiled: false,
      effectExtField: false,
      sheet: true
    }
  ],
  path: '/opt/dataease/data/kettle/admin/d648c91d-3f4f-478b-804f-fef773d10e2d.xlsx',
  sheet: false
}

const num = ref(0)

const nameList = []
const originName = ''
const loading = ref(false)
const columns = shallowRef([])
const fieldOptions = [
  { label: t('dataset.text'), value: 'TEXT' },
  { label: t('dataset.time'), value: 'DATETIME' },
  { label: t('dataset.value'), value: 'LONG' },
  {
    label: t('dataset.value') + '(' + t('dataset.float') + ')',
    value: 'DOUBLE'
  }
]
const sheetObj = reactive({
  tableName: ' ',
  sheetExcelId: '',
  fields: [],
  jsonArray: [],
  nameExist: false,
  empty: '',
  overLength: false
})
const state = reactive({
  excelData: [],
  defaultExpandedKeys: [],
  defaultCheckedKeys: [],
  fileList: [],
  sheets: []
})

const nameListCopy = computed(() => {
  return nameList.filter(ele => ele !== originName)
})
const uploading = ref(false)
const tree = ref<InstanceType<typeof ElTree>>()
const editeTableField = ref(false)

const handleCheckChange = (data, checked) => {
  if (checked) {
    state.defaultCheckedKeys.push(data.id)
    handleNodeClick(data)
  } else {
    let index = state.defaultCheckedKeys.findIndex(id => {
      if (id === data.id) {
        return
      }
    })
    state.defaultCheckedKeys.splice(index, 1)
  }
  validateName()
  const labelList = tree.value.getCheckedNodes().map(ele => ele.id)
  const excelList = state.excelData.map(ele => ele.id)
  num.value = labelList.filter(ele => !excelList.includes(ele)).length
}
const nameExistValidator = (ele, checkList) => {
  ele.nameExist =
    nameListCopy.value.concat(checkList).filter(name => name === ele.tableName).length > 1
}
const validateName = () => {
  const checkList = tree.value.getCheckedNodes().map(ele => ele.tableName)
  state.excelData
    .reduce((pre, next) => pre.concat(next.sheets), [])
    .forEach(ele => {
      if (checkList.includes(ele.tableName)) {
        nameExistValidator(ele, checkList)
        nameLengthValidator(ele)
      } else {
        Object.assign(ele, {
          nameExist: false,
          empty: false,
          overLength: false
        })
      }
    })
}
const fieldType = {
  TEXT: 'field_text',
  DATETIME: 'field_time',
  LONG: 'field_value',
  DOUBLE: 'field_value'
}

const generateColumns = (arr: Field[]) =>
  arr.map(ele => ({
    key: ele.originName,
    fieldType: ele.fieldType,
    dataKey: ele.originName,
    title: ele.name,
    width: 150,
    headerCellRenderer: ({ column }) => (
      <div style={{ width: '100%', display: 'flex', alignItems: 'center' }}>
        <ElDropdown
          placement="bottom-start"
          trigger="click"
          key={column.fieldName}
          onCommand={type => handleCommand(type, column)}
        >
          {{
            default: () => {
              return (
                <ElIcon>
                  <Icon name={fieldType[column.fieldType]}></Icon>
                </ElIcon>
              )
            },
            dropdown: () => {
              return (
                <ElDropdownMenu>
                  {fieldOptions.map(ele => {
                    return (
                      <ElDropdownItem key={ele.value} command={ele.value}>
                        <ElIcon>
                          <Icon name={fieldType[ele.value]}></Icon>
                        </ElIcon>
                        <span>{ele.label}</span>
                      </ElDropdownItem>
                    )
                  })}
                </ElDropdownMenu>
              )
            }
          }}
        </ElDropdown>
        {column.title}
      </div>
    )
  }))
const nameLengthValidator = ele => {
  Object.assign(ele, {
    empty: !ele.tableName.length,
    overLength: ele.tableName.length > 50
  })
}
const handleNodeClick = data => {
  console.log(data)
  if (data.sheet) {
    Object.assign(sheetObj, data)
    columns.value = generateColumns(data.fields)
  }
  console.log(columns.value)
}
const handleCommand = (type, field) => {
  sheetObj.fields.some(ele => {
    if (ele.fieldName === field.dataKey) {
      ele.fieldType = type
      return true
    }
  })
  columns.value.some(ele => {
    if (ele.dataKey === field.dataKey) {
      ele.fieldType = type
      return true
    }
  })
  changeDatasetName()
}

const changeDatasetName = () => {
  for (let i = 0; i < state.excelData.length; i++) {
    if (state.excelData[i].excelId === sheetObj.sheetExcelId) {
      for (let j = 0; j < state.excelData[i].sheets.length; j++) {
        if (state.excelData[i].sheets[j].excelId === sheetObj.sheetId) {
          state.excelData[i].sheets[j] = sheetObj
        }
      }
    }
  }
  validateName()
}

const beforeUpload = () => {
  console.log(param)
  uploading.value = true
}

const uploadFail = response => {
  let myError = response.toString()
  myError = myError.replace('Error: ', '')
}

const excelSave = () => {
  editeTableField.value = false
}

const uploadSuccess = (response, _, fileList) => {
  editeTableField.value = true
  uploading.value = false
  state.excelData.push(response.data)
  console.log(state.excelData)
  // state.defaultExpandedKeys.push(response.data.id)
  // state.defaultCheckedKeys.push(response.data.sheets[0].id)
  nextTick(() => {
    // tree.value.setCheckedKeys(state.defaultCheckedKeys)
  })
  state.fileList = fileList
  // if (response.headers[RefreshTokenKey]) {
  // const refreshToken = response.headers[RefreshTokenKey]
  // setToken(refreshToken)
  // store.dispatch('user/refreshToken', refreshToken)
  // }
}
// uploadSuccess({ data: mockData, headers: {} }, '', [])

const initEditForm = () => {
  dsFormDisabled.value = true
}

const saveExcelDs = () => {
  let validate = true
  let selectedSheet = []
  let sheetFileMd5 = []
  let effectExtField = false
  let changeFiled = false
  let selectNode = tree.value.getCheckedNodes()
  if (!props.param.id && selectNode.some(ele => ele.nameExist)) {
    ElMessage({
      message: t('deDataset.cannot_be_duplicate'),
      type: 'error'
    })
    return
  }
  for (let i = 0; i < selectNode.length; i++) {
    if (selectNode[i].sheet) {
      if (!selectNode[i].tableName || selectNode[i].tableName === '') {
        validate = false
        ElMessage({
          message: t('dataset.pls_input_name'),
          type: 'error'
        })
        return
      }
      if (selectNode[i].tableName.length > 50 && !props.param.id) {
        validate = false
        ElMessage({
          message: t('dataset.char_can_not_more_50'),
          type: 'error'
        })
        return
      }
      if (selectNode[i].effectExtField) {
        effectExtField = true
      }
      if (selectNode[i].changeFiled) {
        changeFiled = true
      }
      selectedSheet.push(selectNode[i])
      sheetFileMd5.push(selectNode[i].fieldsMd5)
    }
  }
  if (!selectedSheet.length) {
    ElMessage({
      message: t('dataset.ple_select_excel'),
      type: 'error'
    })
    return
  }
  if (!validate) {
    return
  }

  let table = {}
  if (!props.param.id) {
    table = {
      id: props.param.id,
      name: props.param.name,
      type: 'Excel',
      sheets: selectedSheet,
      editType: 0
    }
  } else {
    table = {
      id: props.param.id,
      name: props.param.name,
      type: 'Excel',
      sheets: selectedSheet,
      editType: props.param.editType ? props.param.editType : 0
    }
  }

  if (props.param.editType === 0 && props.param.id && (effectExtField || changeFiled)) {
    ElMessageBox.confirm(t('deDataset.replace_the_data'), {
      confirmButtonText: t('common.confirm'),
      tip: '替换可能会影响自定义数据集、关联数据集、仪表板等，是否替换？',
      cancelButtonText: 'Cancel',
      confirmButtonType: 'primary',
      type: 'warning',
      autofocus: false,
      showClose: false,
      callback: (action: Action) => {
        if (action === 'confirm') {
          saveExcelData(sheetFileMd5, table)
        }
      }
    })
  } else {
    saveExcelData(sheetFileMd5, table)
  }
}

const saveExcelData = (sheetFileMd5, table) => {
  table.configuration = Base64.encode(JSON.stringify(table.sheets))
  if (new Set(sheetFileMd5).size !== sheetFileMd5.length && !props.param.id) {
    ElMessageBox.confirm(t('dataset.merge_title'), {
      confirmButtonText: t('dataset.merge'),
      tip: t('dataset.task.excel_replace_msg'),
      cancelButtonText: t('dataset.no_merge'),
      confirmButtonType: 'primary',
      type: 'warning',
      autofocus: false,
      callback: (action: Action) => {
        if (action === 'close') return
        loading.value = true
        table.mergeSheet = action === 'confirm'
        if (action === 'confirm') {
          save(table)
            .then(() => {
              ElMessage({
                message: t('dataset.set_saved_successfully'),
                type: 'success'
              })
            })
            .finally(() => {
              loading.value = false
            })
        }

        if (action === 'cancel') {
          save(table)
            .then(() => {
              ElMessage({
                message: t('dataset.set_saved_successfully'),
                type: 'success'
              })
            })
            .finally(() => {
              loading.value = false
            })
        }
      }
    })
  } else {
    if (loading.value) return
    loading.value = true
    save(table)
      .then(() => {
        ElMessage({
          message: t('dataset.set_saved_successfully'),
          type: 'success'
        })
      })
      .finally(() => {
        loading.value = false
      })
  }
}

const editForm = () => {
  dsFormDisabled.value = false
}

const cancel = () => {
  dsFormDisabled.value = true
}

defineExpose({
  initEditForm,
  saveExcelDs
})
</script>

<template>
  <div class="excel-detail">
    <div class="detail-inner">
      <div class="detail-operate" v-show="editDs">
        <el-button v-show="!dsFormDisabled" @click="() => cancel()">{{
          t('common.cancel')
        }}</el-button>
        <el-button v-show="dsFormDisabled" type="primary" @click="() => editForm()">{{
          t('common.edit')
        }}</el-button>
        <el-button v-show="!dsFormDisabled" type="primary" @click="() => saveExcelDs()">{{
          t('common.sure')
        }}</el-button>
      </div>
      <el-form :disabled="dsFormDisabled" label-position="top">
        <el-form-item :label="t('common.name')">
          <el-input v-model="param.name" :placeholder="t('datasource.input_name')" />
        </el-form-item>
        <el-form-item class="upload-excel">
          <el-upload
            :action="baseUrl + 'api/datasource/uploadFile'"
            :multiple="false"
            :show-file-list="false"
            :file-list="state.fileList"
            :data="param"
            accept=".xls,.xlsx,.csv"
            :before-upload="beforeUpload"
            :on-success="uploadSuccess"
            :on-error="uploadFail"
            name="file"
            :headers="headers"
          >
            <template #trigger>
              <el-input style="width: 100%" readonly placeholder="点击选择文件">
                <template #prefix>
                  <el-icon>
                    <Icon name="icon_search-outline_outlined"></Icon>
                  </el-icon>
                </template>
              </el-input>
            </template>
          </el-upload>
          <p style="width: 100%">温馨提示: 请上传csv,xlsx,xls格式的文件</p>
        </el-form-item>
      </el-form>
      <el-dialog
        fullscreen
        class="excel-dialog-fullscreen"
        append-to-body
        v-model="editeTableField"
      >
        <div class="table-checkbox-list">
          <el-tree
            ref="tree"
            class="de-tree"
            :data="state.excelData"
            :default-expanded-keys="state.defaultExpandedKeys"
            :default-checked-keys="state.defaultCheckedKeys"
            node-key="id"
            :props="defaultProps"
            show-checkbox
            highlight-current
            @node-click="handleNodeClick"
            @check-change="handleCheckChange"
          >
            <template #default="{ data }">
              <span :title="data.excelLabel" class="custom-tree-node">
                <span class="label">{{ data.excelLabel }}</span>
                <span class="error-name-exist">
                  <el-icon>
                    <Icon name="ds-icon-scene"></Icon>
                  </el-icon>
                </span>
              </span>
            </template>
          </el-tree>
        </div>
        <div class="table-detail">
          <empty-background
            v-if="!state.excelData.length"
            :description="t('deDataset.excel_data_first')"
            img-type="table"
          />

          <template v-else>
            <div class="dataset">
              <span class="name">{{ t('dataset.name') }}</span>
              <el-input
                v-model="sheetObj.tableName"
                :placeholder="t('common.name')"
                size="small"
                @change="changeDatasetName"
              />
              <div
                v-if="
                  (sheetObj.nameExist && !param.datasourceId) ||
                  sheetObj.empty ||
                  sheetObj.overLength
                "
                style="top: 52px; left: 107px"
                class="el-form-item__error"
              >
                {{
                  t(
                    sheetObj.nameExist
                      ? 'deDataset.already_exists'
                      : sheetObj.overLength
                      ? 'dataset.char_can_not_more_50'
                      : 'dataset.pls_input_name'
                  )
                }}
              </div>
            </div>
            <div class="data">
              <div class="result-num">
                {{ `${t('dataset.preview_show')} 1000 ${t('dataset.preview_item')}` }}
              </div>
              <div class="info-table">
                <el-auto-resizer>
                  <template #default="{ height, width }">
                    <el-table-v2
                      :columns="columns"
                      header-class="header-cell"
                      :data="sheetObj.jsonArray"
                      :width="width"
                      :height="height"
                      fixed
                    />
                  </template>
                </el-auto-resizer>
              </div>
            </div>
          </template>
        </div>
        <div class="footer">
          <span> 已选: {{ num }} 张表 </span>
          &nbsp;
          <el-button type="primary" @click="excelSave"> 确定 </el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<style lang="less">
.excel-detail {
  display: flex;
  justify-content: center;
  .ed-form {
    width: 50%;
    .upload-excel {
      .ed-form-item__content > div,
      .ed-upload {
        width: 100% !important;
      }
    }
  }
  .detail-inner {
    width: 600px;

    .title-form_primary {
      margin: 16px 0;
    }

    .detail-operate {
      text-align: right;
      padding: 8px 0;
    }

    .flex-space {
      display: flex;
      align-items: center;
    }
  }
}
.excel-dialog-fullscreen {
  .ed-dialog__body::after {
    content: '';
    display: block;
    visibility: hidden;
    clear: both;
  }
  .table-checkbox-list {
    width: 240px;
    float: left;
    height: calc(100vh - 100px);
    overflow-y: auto;
    border-right: 1px solid #cccccc;

    .custom-tree-node {
      position: relative;
      width: 80%;
      display: flex;

      .label {
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
        width: 85%;
      }
    }

    .error-name-exist {
      position: absolute;
      top: 0;
      right: 0;
    }

    .item {
      height: 40px;
      width: 215px;
      border-radius: 4px;
      display: flex;
      align-items: center;
      box-sizing: border-box;
      padding: 12px;

      &:hover {
        background: rgba(31, 35, 41, 0.1);
      }

      &.active {
        background-color: var(--deWhiteHover, #3370ff);
        color: var(--primary, #3370ff);
      }

      .ed-checkbox__label {
        overflow: hidden;
      }
    }
  }
  .table-detail {
    font-family: PingFang SC;
    height: calc(100vh - 100px);
    float: right;
    width: calc(100% - 240px);

    .dataset {
      padding: 21px 24px;
      width: 100%;
      border-bottom: 1px solid rgba(31, 35, 41, 0.15);
      display: flex;
      align-items: center;
      position: relative;

      .name {
        font-size: 14px;
        font-weight: 400;
        color: var(--deTextPrimary, #1f2329);
      }

      .ed-input {
        width: 420px;
        margin-left: 12px;
      }
    }

    .data {
      padding: 16px 24px;
      box-sizing: border-box;
      height: calc(100% - 80px);
      overflow-y: auto;

      .result-num {
        font-family: PingFang SC;
        font-size: 14px;
        font-weight: 400;
        color: var(--deTextSecondary, #646a73);
        margin-bottom: 16px;
      }

      .type-switch {
        padding: 2px 1.5px;
        display: inline-block;
        cursor: pointer;

        i {
          margin-left: 4px;
          font-size: 12px;
        }

        &:hover {
          background: rgba(31, 35, 41, 0.1);
          border-radius: 4px;
        }
      }
    }

    .info-table {
      height: calc(100% - 200px);
    }
  }

  .footer {
    float: right;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: flex-end;
  }
}
</style>
