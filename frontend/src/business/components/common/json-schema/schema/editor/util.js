export function clearAttr(obj) {
  for(let key in obj){
    delete obj[key]
  }
}
/**
 * 快速拷贝两个对象的属性值
 * @param {*} source 
 * @param {*} target 
 */
export function copyAttr(source, target){
  Object.keys(target).forEach(key=>{target[key]=source[key]})
}

export function isNull(ele){
  if(typeof ele==='undefined'){
    return true;
  }else if(ele==null){
    return true;
  }else if(ele==''){
    return true;
  }
  return false;
}