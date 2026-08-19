import request from '@/config/axios'

export const queryUserApi = () => {
    return request.get({url: `/user/org/option`})
}

export const isOrgAdminApi = () => {
    return request.get({url: `/user/orgAdmin`})
}

export const userOptionApi = () => {
    return request.get({url: `/user/byCurOrg`})
}

export const logMsgApi = data => {
    return request.post({url: '/data-filling/task/logMsg', data})
}

export const taskPager = (formId, data, goPage, pageSize) => {
    return request.post({url: `/data-filling/form/${formId}/task/page/${goPage}/${pageSize}`, data})
}

export const commitLogPager = (data, goPage, pageSize) => {
    return request.post({url: `/data-filling/log/page/${goPage}/${pageSize}`, data})
}

export const clearLog = (data) => {
    return request.post({url: `/data-filling/log/clear`, data})
}

export const subTaskPager = (data, goPage, pageSize) => {
    return request.post({url: `/data-filling/sub-task/page/${goPage}/${pageSize}`, data})
}
export const stopTaskApi = (formId, id) => {
    return request.get({url: `/data-filling/form/${formId}/task/${id}/stop`})
}
export const startTaskApi = (formId, id) => {
    return request.get({url: `/data-filling/form/${formId}/task/${id}/start`})
}
export const executeTaskApi = (id, formId, endTime) => {
    return request.post({url: `/data-filling/task/executeNow`, data:{id, formId, endTime}})
}

export const deleteDfTask = (formId, ids: Array<string>) => {
    return request.post({url: `/data-filling/form/${formId}/task/delete`, data: ids})
}
export const deleteDfSubTask = (formId, ids: Array<string>) => {
    return request.post({url: `/data-filling/form/${formId}/sub-task/delete`, data: ids})
}

export const getTaskUserList = (id, type) => {
    return request.get({url: `/data-filling/sub-task/${id}/users/list/${type}`})
}
