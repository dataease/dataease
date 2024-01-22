import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()

export const dsTypes = [
  {
    type: 'db2',
    name: 'Db2',
    catalog: 'OLTP',
    extraParams: ''
  },
  {
    type: 'mysql',
    name: 'MySQL',
    catalog: 'OLTP',
    extraParams:
      'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
  },
  {
    type: 'TiDB',
    name: 'TiDB',
    catalog: 'OLTP',
    extraParams:
      'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
  },
  {
    type: 'impala',
    name: 'Apache Impala',
    catalog: 'OLAP',
    extraParams: 'AuthMech=0'
  },
  {
    type: 'mariadb',
    name: 'MariaDB',
    catalog: 'OLTP',
    extraParams:
      'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
  },
  {
    type: 'doris',
    name: 'Apache Doris',
    catalog: 'OLAP',
    extraParams:
      'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
  },
  {
    type: 'StarRocks',
    name: 'StarRocks',
    catalog: 'OLAP',
    extraParams:
      'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
  },
  {
    type: 'pg',
    name: 'PostgreSQL',
    catalog: 'OLTP',
    extraParams: ''
  },
  {
    type: 'sqlServer',
    name: 'SQL Server',
    catalog: 'OLTP',
    extraParams: ''
  },
  {
    type: 'oracle',
    name: 'Oracle',
    catalog: 'OLTP',
    extraParams: '',
    charset: [
      'Default',
      'GBK',
      'BIG5',
      'ISO-8859-1',
      'UTF-8',
      'UTF-16',
      'CP850',
      'EUC_JP',
      'EUC_KR'
    ],
    targetCharset: ['Default', 'GBK', 'UTF-8']
  },
  {
    type: 'mongo',
    name: 'Mongodb-BI',
    catalog: 'OLTP',
    extraParams: 'rebuildschema=true&authSource=admin'
  },
  {
    type: 'ck',
    name: 'ClickHouse',
    catalog: 'OLAP',
    extraParams: ''
  },
  {
    type: 'redshift',
    name: 'AWS Redshift',
    catalog: 'DL',
    extraParams: ''
  },
  {
    type: 'API',
    name: 'API',
    catalog: 'OTHER',
    extraParams: ''
  },
  {
    type: 'Excel',
    name: 'Excel',
    catalog: 'LOCAL',
    extraParams: ''
  }
]

export const typeList = ['OLTP', 'OLAP', 'DL', 'OTHER', 'LOCAL']
export const nameMap = {
  OLTP: 'OLTP',
  OLAP: 'OLAP',
  DL: t('datasource.dl'),
  OTHER: 'API数据',
  LOCAL: t('datasource.local_file')
}
