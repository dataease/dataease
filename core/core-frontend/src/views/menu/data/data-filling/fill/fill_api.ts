import request from '@/config/axios'

export const loadUserFillingTask = (data, goPage, pageSize) => {
    return request.post({url: `/data-filling/user-task/page/${goPage}/${pageSize}`, data})
}

export const countDfTodoList = () => {
    return request.post({url: `/data-filling/user-task/todo/count`, data: {}})
}

export const loadUserTaskDataList = (id) => {
    return request.get({url: `/data-filling/user-task/list/${id}`})
}

export const saveTaskRowData = (id, data): Promise<any> => {
    return request
        .post({
            url: `/data-filling/user-task/saveData/${id}`,
            data
        })
}
export const appendTaskRowData = (id, data): Promise<any> => {
    return request
        .post({
            url: `/data-filling/user-task/appendData/${id}`,
            data
        })
}

export const userTaskDeleteRowData = (taskInstanceId, dataId): Promise<any> => {
    return request
        .get({
            url: `/data-filling/user-task/${taskInstanceId}/deleteData/${dataId}`,
        })
}


export const appendConfirmUpload = (id, formId, data) => {
    return request.post({
        url: `/data-filling/user-task/appendData/${id}/form/${formId}/confirmUpload`,
        data
    })
}

export const listDfPlugins = (): Promise<any> => request.get({url: '/xpackComponent/dfPlugins'})
export const getDfPlugin = (type): Promise<any> => request.get({url: `/xpackComponent/dfPlugin/${type}`})



