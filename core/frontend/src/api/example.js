import request from '@/utils/request'

const example = {

  // 获取员工列表
  staffList(data) {
    return request.post(`/staff/findPage?pageNo=${data.pageNo - 1}&pageSize=${data.pageSize}`, {
      name: data.name,
      idCard: data.idCard,
      mobile: data.mobile,
      company: data.company,
      department: data.department,
      status: data.status,
      reportTime: data.reportTime
    })
  },

  // 删除员工信息
  deleteByStaffInfo(id) {
    return request.get('/staff/deleteById', {
      params: {
        id: id
      }
    })
  }
}

export default example
