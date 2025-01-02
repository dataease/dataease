import request from '@/utils/request'

export function saveForm(data) {
  return request({
    url: 'dataFilling/form/save',
    method: 'post',
    loading: true,
    data
  })
}
export function updateForm(data) {
  return request({
    url: 'dataFilling/form/update',
    method: 'post',
    loading: true,
    data
  })
}
export function updateFormName(data) {
  return request({
    url: 'dataFilling/form/updateName',
    method: 'post',
    loading: true,
    data
  })
}
export function moveForm(data) {
  return request({
    url: 'dataFilling/form/move',
    method: 'post',
    loading: true,
    data
  })
}

export function listForm(data) {
  return request({
    url: 'dataFilling/form/tree',
    method: 'post',
    loading: true,
    data
  })
}

export function deleteForm(id) {
  return request({
    url: 'dataFilling/form/delete/' + id,
    method: 'post',
    loading: true
  })
}
export function getForm(id) {
  return request({
    url: 'dataFilling/form/get/' + id,
    method: 'post',
    loading: true
  })
}
export function deleteData(formId, rowId) {
  return request({
    url: 'dataFilling/form/' + formId + '/delete/' + rowId,
    method: 'post',
    loading: true
  })
}
export function downloadTemplate(formId) {
  return request({
    url: 'dataFilling/form/' + formId + '/excel/template',
    method: 'post',
    loading: true,
    responseType: 'blob'
  })
}
export function getWithPrivileges(id) {
  return request({
    url: 'dataFilling/manage/form/' + id,
    method: 'post',
    loading: true
  })
}
export function searchTable(id, data) {
  return request({
    url: 'dataFilling/form/' + id + '/tableData',
    method: 'post',
    loading: true,
    data
  })
}
export function searchCommitLogs(formId, data, goPage, pageSize) {
  return request({
    url: 'dataFilling/form/' + formId + '/commitLog/' + goPage + '/' + pageSize,
    method: 'post',
    loading: true,
    data
  })
}

export function searchFormTasks(formId, data, goPage, pageSize) {
  return request({
    url: 'dataFilling/form/' + formId + '/task/' + goPage + '/' + pageSize,
    method: 'post',
    loading: true,
    data
  })
}

export function searchFormMyTasks(data, goPage, pageSize, type) {
  return request({
    url: 'dataFilling/myTask/' + type + '/' + goPage + '/' + pageSize,
    method: 'post',
    loading: true,
    data
  })
}
export function saveFormTasks(formId, data) {
  return request({
    url: 'dataFilling/form/' + formId + '/task/save',
    method: 'post',
    loading: true,
    data
  })
}
export function deleteFormTasks(id) {
  return request({
    url: 'dataFilling/form/task/' + id + '/delete',
    method: 'post',
    loading: true
  })
}
export function enableFormTasks(id) {
  return request({
    url: 'dataFilling/form/task/' + id + '/enable',
    method: 'post',
    loading: true
  })
}
export function disableFormTasks(id) {
  return request({
    url: 'dataFilling/form/task/' + id + '/disable',
    method: 'post',
    loading: true
  })
}

export function newFormRowData(formId, data) {
  return request({
    url: 'dataFilling/form/' + formId + '/rowData/save',
    method: 'post',
    loading: true,
    data
  })
}

export function saveFormRowData(formId, id, data) {
  return request({
    url: 'dataFilling/form/' + formId + '/rowData/save/' + id,
    method: 'post',
    loading: true,
    data
  })
}

export function userFillFormData(userTaskId, data) {
  return request({
    url: `dataFilling/myTask/fill/${userTaskId}`,
    method: 'post',
    loading: true,
    data
  })
}

export function getTableColumnData(optionDatasource, optionTable, optionColumn, optionOrder, query) {
  return request({
    url: `dataFilling/form/${optionDatasource}/options`,
    method: 'post',
    loading: true,
    data: {
      optionTable: optionTable,
      optionColumn: optionColumn,
      optionOrder: optionOrder,
      query: query
    }
  })
}
