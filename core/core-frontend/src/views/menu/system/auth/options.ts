import { useCache } from '@/hooks/web/useCache'
import treeSort from "@/utils/treeSortUtils";
const { wsCache } = useCache()
export interface ColumnOption {
  type?: string
  label: string
  weightLevel: number
  children?: ColumnOption[]
}

export const formatExt = (num: number): number[] | null => {
  if (!num) {
    return null
  }
  const reversedStr = num.toString().split('').reverse().join('')
  const reversedNumArray = reversedStr?.split('')?.map(Number) ?? []
  return reversedNumArray;
}

export const whileLoop = (list, breakParent, cb) => {
  const stack = [...list]
  while(stack.length) {
    const item = stack.pop()
    const hasChildren = item.children?.length
    if (!hasChildren || !breakParent) {
      cb(item)
    }
    if (hasChildren) {
      item.children.forEach(kid => {
        stack.push(kid)
      })
    }
  }
}
export const extLevelArray = [4, 5, 6]
export const levelMatch = (item, level) => {
  const weight = item?.weight || 0
  const ext = item?.ext || 0
  if (!weight) {
    return false
  }
  const match = weight >= level
  if (extLevelArray.includes(level)) {
    if (weight === 1) {
      return false
    } else if (weight === 9) {
      return true
    } else {
      const extArray = formatExt(ext)
      const index = extLevelArray.indexOf(level)
      return extArray?.length && extArray.length > index && !!extArray[index]
    }
  }
  return match
}

export const standaloneNode = (level) => {
  const nodes = [4, 5, 6, 7]
  return nodes.includes(level)
}

export const getExtLevel = (level) => {
  return extLevelArray.includes(level) ? 4 : level
}

export const checkedStandalone = (ext, level) => {
  
  if (level === 9) {
    return 111
  } else if (level === 7 || level === 1) {
    return ext || 0
  }
  
  const index = extLevelArray.indexOf(level)
  if (index == -1) {
    return ext
  }
  const extArray = formatExt(ext)
  const extLen = extArray?.length || 0
  let len = extLevelArray.length
  const result: number[] = []
  for (let i = 0; i < len; i++) {
    result[i] = i === index ? 1: extArray && (extLen > i) ? extArray[i] : 0
  }
  const text = result.reverse().join('')
  if (text) {
    return parseInt(text)
  }
  return 0
}

export const unCheckedStandalone = (ext, level) => {
  if (level >= 7) {
    return ext
  } else if (level === 1) {
    return 0
  }

  const index = extLevelArray.indexOf(level)
  if (index == -1) {
    return ext
  }
  const extArray = formatExt(ext)
  const extLen = extArray?.length || 0
  let len = extLevelArray.length
  const result: number[] = []
  for (let i = 0; i < len; i++) {
    result[i] = i === index ? 0: extArray && (extLen > i) ? extArray[i] : 0
  }
  const text = result.reverse().join('')
  if (text) {
    return parseInt(text)
  }
  return 0
}

export const perChanged = (row, item) => {
  const rowWeight = row.weight || 0
  const rowExt = row.ext || 0
  const itemWeight = item.weight || 0
  const itemExt = item.ext || 0
  return rowWeight !== itemWeight || rowExt !== itemExt
}

export const customSort = (data, busiFlag) => {
  const busiMapping = {
    'panel': 'dashboard',
    'screen': 'dataV',
    'dataset': 'dataset',
    'datasource': 'datasource',
    'data_filling': 'dataFillingForm',
    'spreadsheet': 'spreadsheet'
  }
  const sortBusi = busiMapping[busiFlag]
  if (!sortBusi) {
    return data
  }
  const backSort = wsCache.get('TreeSort-backend')
  const sortList = ['time_asc', 'time_desc', 'name_asc', 'name_desc']
  let curSortType = sortList[Number(backSort) ?? 1]
  curSortType = wsCache.get(`TreeSort-${sortBusi}`) ?? curSortType
  if (!curSortType) {
    return data
  }
  return treeSort(data, curSortType)
}

export const isRoleCheckboxHidden = (
  row: any,
  weightLevel: number,
  activeAuth: string
): boolean => {
  if (row.type === 2) return false;
  if (activeAuth === 'menu') {
    return weightLevel > 2 && row.attrs.typeCode < 9;
  }
  // if (row.root) return false;
  if (row.attrs?.typeCode === 9) return false;
  if (row.attrs?.typeCode === 7) {
    return weightLevel === 9;
  }
  return weightLevel > 2;
};

export const getStringWidth = (value, fontSize) => {
  const tempElement = document.createElement('span');
  tempElement.style.fontSize = fontSize + 'px';
  tempElement.style.position = 'absolute';
  tempElement.style.visibility = 'hidden';
  tempElement.style.whiteSpace = 'nowrap';
  tempElement.textContent = value;
  document.body.appendChild(tempElement);
  const width = tempElement.offsetWidth;
  document.body.removeChild(tempElement);
  return width;
}