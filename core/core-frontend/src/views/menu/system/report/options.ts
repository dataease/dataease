import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()

export const filterOption = [
    {
      type: 'enum',
      option: [
        {
          id: 2,
          name: t('dataset.completed')
        },
        {
          id: 3,
          name: t('dataset.error')
        }
      ],
      field: 'lastStatusList',
      title: t('report.last_exec_result'),
      operate: 'in'
    },
  
    {
      type: 'enum',
      option: [
        {
          id: 0,
          name: t('report.status_wait')
        },
        {
          id: 1,
          name: t('report.status_send')
        },
        {
          id: 2,
          name: t('report.status_stop')
        },
        {
          id: 3,
          name: t('report.status_finish')
        }
      ],
      field: 'statusList',
      title: t('report.task_status'),
      operate: 'in'
    },
    {
      type: 'time',
      option: [],
      property: {
        showType:'datetimerange',
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        rangeSeparator: "-",
        startPlaceholder: t('report.start_time'),
        endPlaceholder: t('report.end_time'),
      },
      field: 'timeList',
      title: t('common.create_time'),
      operate: 'between'
    }
  ]