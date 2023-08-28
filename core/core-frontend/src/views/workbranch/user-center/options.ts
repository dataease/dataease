import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()

export interface Tree {
  id: string
  name: string
  readonly: boolean
  children?: Tree[]
  disabled: boolean
  root: boolean
}

export const columnNames = [
  {
    label: 'ID',
    props: 'account'
  },
  {
    label: 'user.name',
    props: 'name'
  },
  {
    label: 'user.role',
    props: 'roleItems'
  },
  {
    label: 'common.email',
    props: 'email'
  },

  {
    label: 'user.state',
    props: 'enable'
  },
  {
    label: 'common.create_time',
    props: 'createTime'
  }
]

export const filterOption = [
  {
    type: 'enum',
    option: [
      {
        id: true,
        name: t('commons.enable')
      },
      {
        id: false,
        name: t('commons.disable')
      }
    ],
    field: 'status',
    title: t('commons.status'),
    operate: 'in'
  },

  {
    type: 'tree-select',
    option: [
      {
        value: 'admin',
        label: t('role.org_admin'),
        children: [],
        disabled: false
      },
      {
        value: 'readonly',
        label: t('role.average_role'),
        children: [],
        disabled: false
      }
    ],
    field: 'rid',
    title: t('commons.role'),
    operate: 'in'
  }
]

export const groupBy = (list: Tree[]) => {
  const map = new Map()
  list.forEach(item => {
    const readonly = item.readonly
    let arr = map.get(readonly)
    if (!arr) {
      arr = []
    }
    arr.push({ value: item.id, label: item.name, disabled: false })
    map.set(readonly, arr)
  })
  return map
}
