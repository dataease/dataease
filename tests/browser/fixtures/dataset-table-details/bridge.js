const fields = [
  {
    id: 'field-product',
    deType: 0,
    deExtractType: 0,
    name: 'product',
    originName: '商品品类(主维度: 手机数码/电脑办公/生鲜食品/运动户外/家居家纺)',
    type: 'LONGTEXT',
    description: '商品品类'
  },
  {
    id: 'field-member',
    deType: 0,
    deExtractType: 0,
    name: 'member',
    originName: '会员等级(分组维度: 普通会员/银卡会员/金卡会员/钻石会员)',
    type: 'LONGTEXT',
    description: '会员等级'
  },
  {
    id: 'field-delivery',
    deType: 3,
    deExtractType: 3,
    name: 'delivery',
    originName: '配送履约耗时(小时, 物流箱线图分析指标)',
    type: 'DECIMAL',
    description: '配送履约耗时'
  },
  {
    id: 'field-discount',
    deType: 3,
    deExtractType: 3,
    name: 'discount',
    originName: '优惠折扣金额(元)',
    type: 'DECIMAL',
    description: '优惠折扣金额'
  }
]

const rows = [
  {
    product: '手机数码',
    member: '普通会员',
    delivery: '2849.00000000',
    discount: '46866.370000000'
  },
  {
    product: '手机数码',
    member: '银卡会员',
    delivery: '1911.00000000',
    discount: '31932.360000000'
  },
  {
    product: '手机数码',
    member: '金卡会员',
    delivery: '1106.20000000',
    discount: '19526.180000000'
  },
  {
    product: '手机数码',
    member: '钻石会员',
    delivery: '552.50000000',
    discount: '14309.360000000'
  },
  {
    product: '电脑办公',
    member: '金卡会员',
    delivery: '1213.00000000',
    discount: '27534.060000000'
  },
  {
    product: '电脑办公',
    member: '银卡会员',
    delivery: '1709.60000000',
    discount: '42933.230000000'
  }
]

const messages = {
  'chart.data_preview': '数据预览',
  'dataset.field_details': '字段详情',
  'deDataset.search_fields': '搜索字段',
  'datasource.table_description': '源表备注',
  'data_set.refresh_data': '刷新',
  'data_set.field_name': '字段名称',
  'data_set.field_type': '字段类型',
  'data_set.field_notes': '字段备注'
}

const queryRequest = () => ({ finish() {}, isCurrent: () => true })

export const useLatestQuery = () => ({
  start: queryRequest,
  invalidate() {}
})

export const useI18n = () => ({ t: key => messages[key] || key })

export const getTableField = async () => fields

export const getPreviewData = async () => ({
  data: {
    fields: fields.map(field => ({
      ...field,
      dataeaseName: field.name
    })),
    data: rows
  }
})
