import request from '@/config/axios'
import dayjs from 'dayjs'


const nameTrim = (target: {}) => {
    if (target.name) {
        target.name = target.name.trim()
    }
}

export function formatDate(value, dateType) {
    if (!value) {
        return value
    }
    switch (dateType) {
        case 'year':
            return dayjs(value).format('YYYY')
        case 'month':
        case 'monthrange':
            return dayjs(value).format('YYYY-MM')
        case 'datetime':
        case 'datetimerange':
            return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
        default:
            return dayjs(value).format('YYYY-MM-DD')
    }
}

export interface ColumnItem {
    props: string
    label: string
    date: boolean
    dateType?: string
    type: string
    multiple: boolean
    rangeIndex?: number
    disabled?: boolean
}

export interface DataFillingOrFolder {
    name: string
    action?: string
    id?: number | string
    pid?: number | string
    nodeType: 'folder' | 'data-filling'
    union?: Array<{}>
    allFields?: Array<{}>
}

export interface Tree {
    name: string
    value?: string | number
    id: string | number
    nodeType: string
    createBy?: string
    level: number
    leaf?: boolean
    pid: string | number
    type?: string
    createTime: number
    children?: Tree[]
    request: any
}

export interface DfFormSetting {
    id?: string
    name?: string
    pid?: string
    datasource?: string
    datasourceName?: string
    tableName?: string
    forms: Array<DfFormItem>
    createIndex: boolean
    tableIndexes: Array<any>

    creator?: string
    updater?: string
    createTime?: number
    updateTime?: number
    weight?: number

    useExistsTable?: boolean
}

export interface DfFormItem {
    type: string
    typeName: string
    icon: string
    order?: number
    value?: any
    id?: string
    settings: FormItemSetting
    old?: boolean
    removed?: boolean
    extraDetails?: Array<any>
}

export interface OptionItem {
    fieldName: string,
    displayName: string
}

export interface FormItemSetting {
    name?: string
    placeholder?: string
    required?: boolean
    unique?: boolean
    inputType?: string
    optionSourceType?: 1 | 2
    optionDatasource?: number
    optionTable?: string
    optionColumn?: string
    optionOrder?: string
    multiple?: boolean
    dateType?: 'date' | 'daterange'
    rangeSeparator?: string
    startPlaceholder?: string
    endPlaceholder?: string
    options?: Array<FormItemSettingOptions>
    mapping?: {
        columnName?: string
        columnName1?: string
        columnName2?: string
        type?: string
        useExistsTable?: boolean
    },
    extraColumns?: Array<OptionItem>,
    enableExtraColumns?: boolean,
    enableDefaultTime?: boolean,
    enableCurrentTime?: boolean,
    defaultTime?: any,
    permissionsTree?: any
}

export interface FormItemSettingOptions {
    name: string
    value: string
}

export interface SimpleDatasource {
    id: string
    pid: string
    name: string
    type: string
    typeAlias: string
    status: string
    enableDataFill: boolean
}

export const listDataFillingForms = async (data): Promise<any> => {
    return request.post({url: '/data-filling/tree', data}).then(res => {
        return res?.data
    })
}

export const createFolder = (data = {}): Promise<any> => {
    return request
        .post({url: '/data-filling/save', data: {...data, nodeType: 'folder'}})
        .then(res => {
            return res?.data
        })
}
export const save = (data = {}): Promise<any> => {
    nameTrim(data)
    return request.post({url: '/data-filling/save', data}).then(res => {
        return res?.data
    })
}
export const update = (data = {}): Promise<any> => {
    nameTrim(data)
    return request.post({url: '/data-filling/update', data}).then(res => {
        return res?.data
    })
}

export const move = (data = {}): Promise<any> => {
    return request.post({url: '/data-filling/move', data}).then(res => {
        return res?.data
    })
}

export const reName = (data = {}): Promise<any> => {
    nameTrim(data)
    return request.post({url: '/data-filling/rename', data}).then(res => {
        return res?.data
    })
}
export const listDatasourceList = (): Promise<Array<SimpleDatasource>> => {
    return request.get({url: '/data-filling/datasource/list'}).then(res => {
        return res?.data
    })
}
export const listAllDatasourceList = (): Promise<Array<SimpleDatasource>> => {
    return request.get({url: '/data-filling/datasource/listAll'}).then(res => {
        return res?.data
    })
}

export const getDataFilling = async (id: string): Promise<any> => {
    return request.get({url: `/data-filling/get/${id}`}).then(res => {
        return res?.data
    })
}

export const getDataFillingTemplateSettings = async (userTaskId: string): Promise<any> => {
    return request.get({url: `/data-filling/template/${userTaskId}`}).then(res => {
        return res?.data
    })
}

export const deleteById = (id: string): Promise<any> => {
    return request.get({url: '/data-filling/delete/' + id})
}

export const deleteRowData = (formId: string, id: number): Promise<any> => {
    return request.get({url: `/data-filling/form/${formId}/delete/${id}`})
}
export const truncateRowData = (formId: string): Promise<any> => {
    return request.get({url: `/data-filling/form/${formId}/truncate`})
}

export const batchDeleteRowData = (formId: string, data: Array<any>): Promise<any> => {
    return request.post({url: `/data-filling/form/${formId}/batch-delete`, data})
}

export const getTableColumnDataPreview = (
    optionDatasource,
    optionTable,
    optionColumn,
    optionOrder,
    permissionsTree
): Promise<any> => {
    return request.post({
        url: `/data-filling/form/${optionDatasource}/options/preview`,
        data: {
            optionTable: optionTable,
            optionColumn: optionColumn,
            optionOrder: optionOrder,
            permissionsTree: permissionsTree
        }
    })
}

export const getTableColumnData = (
    formId,
    fieldId
): Promise<any> => {
    return request.get({
        url: `/data-filling/form/${formId}/field/${fieldId}/options`
    })
}

export const getExtraDetailsApi = (data: {
    value: any,
    columnId: string,
    formId: string
}): Promise<any> => {
    return request.post({url: `/data-filling/form/extraDetails`, data})
}

export const getExtraDetailsApiPreview = (data: {
    optionDatasource: string,
    optionTable: string,
    optionColumn: string,
    extraColumns: Array<string>,
    value: any,
}): Promise<any> => {
    return request.post({url: `/data-filling/form/extraDetails/preview`, data})
}


export const searchTable = (id, data): Promise<any> => {
    return request.post({
        url: '/data-filling/form/' + id + '/tableData',
        data
    })
}

export const saveFormRowData = (formId, data): Promise<any> => {
    return request
        .post({
            url: '/data-filling/form/' + formId + '/rowData/save',
            data
        })
        .then(res => {
            return res?.data
        })
}

export const saveTask = (data): Promise<any> => {
    return request.post({
        url: `/data-filling/task/save`,
        data
    })
}

export const getTaskInfo = (taskId): Promise<any> => {
    return request.get({
        url: `/data-filling/task/info/${taskId}`
    })
}

export const downloadExcelTemplate = (formId) =>
    request.get({url: `/data-filling/form/${formId}/excelTemplate`, responseType: 'blob'})

export const uploadDfExcel = (formId, data) => {
    return request.post({
        url: `/data-filling/form/${formId}/uploadFile`,
        data,
        loading: true,
        headersType: 'multipart/form-data;'
    })
}

export const confirmUpload = (formId, data) => {
    return request.post({
        url: `/data-filling/form/${formId}/confirmUpload`,
        data
    })
}

export const innerExport = async (formId, isDataEaseBi): Promise<IResponse> => {
    return request.post({
        url: `/data-filling/innerExport/${isDataEaseBi}/${formId}`,
        method: 'post',
        data: {},
        loading: true,
        responseType: 'blob'
    })
}

export const listBuiltInDatasourceTables = async (data = {}): Promise<IResponse> => {
    return request.post({url: '/data-filling/getBuiltInTables', data}).then(res => {
        return res
    })
}

export const getBuiltInTableField = (tableName: string) => request.post({
    url: '/data-filling/getBuiltInTableField',
    data: {tableName: tableName}
})
