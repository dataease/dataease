/*
 * @moduleName:通用工具类
 * @Author: dawdler
 * @Date: 2019-01-09 15:30:18
 * @LastModifiedBy: dawdler
 * @LastEditTime: 2020-12-26 14:06:09
 */
export default {
  getTreeData,
  each
}
/*
each(arr, (item, children) => {
    item.value = xx;
    // 该item 包含children，因此直接赋值,不需要单独处理children里面的值
});
 * [_each description]  倒查、展平、数据回调返回回当前一条数据和子集
 * @param  {[Array]}   data     [description]
 * @param  {Function}  callback [description]
 * @param  {String}    childName[description]
 * @return {[Array]}            [description]
 * 默认使用副本,在callback处理数据，如果不使用副本，那么需要重新对treeData赋值
    treeData = each(treeData, (item, children) => {
        item.value = xx;
    });
 */
export function each(data, callback, childName = 'children') {
  let current
  let children
  for (let i = 0, len = data.length; i < len; i++) {
    current = data[i]
    children = []
    if (current[childName] && current[childName].length > 0) {
      children = current[childName]
    }
    callback && callback(current, children)
    if (children.length > 0) {
      each(children, callback, childName)
    }
  }
}

/**
 * @Author yihuang",
 * @param data     数据
 * @param id       要比对的名称
 * @param val      要比对的值
 * @param name     要返回的名称
 * @param children 子集名称
 * @param isRow    是否返回这一行的数据
 * @注 迭代判断多层
 * //=======================
 * 返回这一条数据的中文名
 * let name=utils.getTreeData(arr, 'flowId', item.decategoryId, 'name');
 * //=======================
 * 返回所有匹配的数据
 * let arr=utils.getTreeData(arr, 'flowId', item.decategoryId, 'name','children',true);
 */
export function getTreeData(data, id = 'id', val = '', name = 'name', children = 'children', isRow = false) {
  const arr = []
  each(
    data,
    item => {
      if (item[id] === val) {
        arr.push(item)
      }
    },
    children
  )
  return arr.length > 0 ? (isRow ? arr : arr[0][name]) : null
}
export function guid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}
