import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()

export const filterOption = [
  {
    type: 'enum',
    option: [
      {
        id: 'pc',
        name: 'PC'
      },
      {
        id: 'mobile',
        name: t('operate_log.mobile')
      }
    ],
    field: 'client',
    title: t('operate_log.client'),
    operate: 'in'
  },    
  {
    type: 'tree-select',
    option: [],
    field: 'op',
    title: t('operate_log.type'),
    operate: 'in',
    property: {
      placeholder: t('operate_log.type'),
    }
  },    
  {
    type: 'time',
    option: [],
    field: 'time',
    title: t('operate_log.time'),
    operate: 'between',
    property: {
      showType: 'datetimerange',
      format: 'YYYY-MM-DD HH:mm:ss',
      valueFormat: 'x'
    }
  },
  {
    type: 'tree-select',
    option: [],
    field: 'oid',
    title: t('operate_log.organization'),
    operate: 'in',
    property: {
      checkStrictly: true,
      checkOnClickNode: false,
      placeholder: t('commons.organization')
    }
  }
]