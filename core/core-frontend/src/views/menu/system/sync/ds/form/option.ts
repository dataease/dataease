import { useI18n } from "@/hooks/web/useI18n";

const { t } = useI18n()

export type DsType =
    | "OLTP"
    | "OLAP"
    | "DL"
    | "OTHER"
    | "LOCAL"
    | "latestUse"
    | "all";

export const dsTypes = [
    {
        type: 'mysql',
        name: 'MySQL',
        catalog: 'OLTP',
        datasourceRole: 1,
        extraParams:
            'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
    },
    {
        type: 'doris',
        name: 'Apache Doris',
        catalog: 'OLAP',
        datasourceRole: 2,
        extraParams:
            'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
    },
    {
        type: 'sqlServer',
        name: 'SQL Server',
        catalog: 'OLTP',
        datasourceRole: 1,
        extraParams: 'encrypt=false'
    },
    {
        type: 'oracle',
        name: 'Oracle',
        catalog: 'OLTP',
        extraParams: '',
        datasourceRole: 1,
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
        type: 'db2',
        name: 'Db2',
        catalog: 'OLTP',
        extraParams: '',
        datasourceRole: 1,
    },
    {
        type: 'elasticsearch',
        name: 'Elasticsearch',
        catalog: 'OLTP',
        datasourceRole: 1,
        extraParams: ''
    }
]

export const typeList = ['OLTP', 'OLAP', 'DL', 'OTHER', 'LOCAL']

export const nameMap = {
    OLTP: 'OLTP',
    OLAP: 'OLAP',
    DL: t('datasource.dl'),
    OTHER: t('data_source.api_data'),
    LOCAL: t('datasource.local_file')
}

export const filterOption = [
    {
        type: 'enum',
        option: [],
        field: 'dsType',
        title: t('sync_datasource.ds_type'),
        operate: 'in'
    },
    {
        type: 'enum',
        option: [
            {
                id: 'Success',
                name: t('sync_datasource.valid')
            },
            {
                id: 'Error',
                name: t('sync_datasource.invalid')
            }
        ],
        field: 'status',
        title: t('sync_datasource.status'),
        operate: 'in'
    },
    {
        type: 'time',
        option: [],
        property: {
            showType:'datetimerange',
            format: 'YYYY-MM-DD HH:mm:ss',
            valueFormat: 'YYYY-MM-DD HH:mm:ss',
            rangeSeparator:"-",
            startPlaceholder: t('sync_datasource.start_time'),
            endPlaceholder: t('sync_datasource.end_time')
        },
        field: 'createTime',
        title: t('sync_datasource.create_time'),
        operate: 'between'
    }

]
