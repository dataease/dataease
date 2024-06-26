import { useI18n } from '@/hooks/web/useI18n'
import SnowflakeId from 'snowflake-id'
const snowflake = new SnowflakeId()

const { t } = useI18n()

const guid = () => {
  return snowflake.generate()
}

const timestampFormatDate = (timestamp, showMs?: boolean) => {
  if (!timestamp || timestamp === -1) {
    return '-'
  }

  const date = new Date(timestamp)

  const y = date.getFullYear()

  let MM = date.getMonth() + 1
  MM = (MM < 10 ? '0' + MM : MM) as number

  let d = date.getDate()
  d = (d < 10 ? '0' + d : d) as number

  let h = date.getHours()
  h = (h < 10 ? '0' + h : h) as number

  let m = date.getMinutes()
  m = (m < 10 ? '0' + m : m) as number

  let s = date.getSeconds()
  s = (s < 10 ? '0' + s : s) as number

  let format = y + '-' + MM + '-' + d + ' ' + h + ':' + m + ':' + s

  if (showMs === true) {
    const ms = date.getMilliseconds()
    format += ':' + ms
  }

  return format
}

const defaultValueScopeList = [
  { label: t('dataset.scope_edit'), value: 'EDIT' },
  { label: t('dataset.scope_all'), value: 'ALLSCOPE' }
]
const fieldOptions = [
  { label: t('dataset.text'), value: 'TEXT' },
  { label: t('dataset.value'), value: 'LONG' },
  {
    label: t('dataset.value') + '(' + t('dataset.float') + ')',
    value: 'DOUBLE'
  },
  { label: t('dataset.time_year'), value: 'DATETIME-YEAR' },
  {
    label: t('dataset.time_year_month'),
    value: 'DATETIME-YEAR-MONTH',
    children: [
      {
        value: 'YYYY-MM',
        label: 'YYYY-MM'
      },
      {
        value: 'YYYY/MM',
        label: 'YYYY/MM'
      }
    ]
  },
  {
    label: t('dataset.time_year_month_day'),
    value: 'DATETIME-YEAR-MONTH-DAY',
    children: [
      {
        value: 'YYYY-MM-DD',
        label: 'YYYY-MM-DD'
      },
      {
        value: 'YYYY/MM/DD',
        label: 'YYYY/MM/DD'
      }
    ]
  },
  {
    label: t('dataset.time_all'),
    value: 'DATETIME',
    children: [
      {
        value: 'YYYY-MM-DD HH:mm:ss',
        label: 'YYYY-MM-DD HH:MI:SS'
      },
      {
        value: 'YYYY/MM/DD HH:mm:ss',
        label: 'YYYY/MM/DD HH:MI:SS'
      }
    ]
  }
]

const getFieldName = (fields, name) => {
  let n = name
  n = n + '_copy'
  for (let i = 0; i < fields.length; i++) {
    const field = fields[i]
    if (field.name === n) {
      n = getFieldName(fields, n)
    }
  }
  return n
}

const timeTypes = [
  'yyyy-MM-dd',
  'yyyy/MM/dd',
  'yyyy-MM-dd HH:mm:ss',
  'yyyy/MM/dd HH:mm:ss',
  'custom'
]

type NodeType = 'db' | 'sql'
type UnionType = 'left' | 'right' | 'inner'
interface UnionField {
  currentField: Field
  parentField: Field
}
interface Node {
  tableName: string
  type: NodeType
  datasourceId: string
  id: string
  unionType: UnionType
  unionFields: UnionField[]
  info: string
  sqlVariableDetails: string
  currentDsFields: Field[]
  children?: Node[]
  confirm?: boolean
  isShadow?: boolean
  flag?: string
}

interface Field {
  checked: boolean
  deExtractType: number
  deType: number
  name: string
  type: string
  originName: string
  id: string
}

interface DataSource {
  id: string
  name: string
  children?: DataSource[]
}

export {
  NodeType,
  UnionType,
  UnionField,
  DataSource,
  Node,
  Field,
  timestampFormatDate,
  defaultValueScopeList,
  fieldOptions,
  guid,
  getFieldName,
  timeTypes
}
