import { useI18n } from '@/hooks/web/useI18n'
import request from '@/config/axios'
const { t } = useI18n()

export interface ShortcutRequest {
  keyword?: string
  type?: string
  asc?: boolean
}
interface TableColumn {
  field: string
  label: string
  type?: string
}
interface BusiRecord {
  columnList: TableColumn[]
  url: string
  busiList: string[]
  dataCache?: any[]
}
class ShortcutOption {
  busiFlag: string
  busiRecordMap: {
    recent: BusiRecord
    store: BusiRecord
    // share: BusiRecord
  }

  constructor() {
    this.busiFlag = 'recent'
    this.busiRecordMap = {
      recent: {
        url: '/dataVisualization/findRecent',
        busiList: ['panel', 'screen', 'dataset', 'datasource'],
        dataCache: [],
        columnList: [
          { field: 'type', label: t('datasource.type') },
          { field: 'creator', label: t('visualization.create_by') },
          { field: 'lastEditor', label: '最近编辑人' },
          { field: 'lastEditTime', label: '最近编辑时间', type: 'time' }
        ]
      },
      store: {
        url: '/store/query',
        busiList: ['panel', 'screen'],
        dataCache: [],
        columnList: [
          { field: 'type', label: t('datasource.type') },
          { field: 'creator', label: t('visualization.create_by') },
          { field: 'lastEditor', label: '最近编辑人' },
          { field: 'lastEditTime', label: '最近编辑时间', type: 'time' }
        ]
      }
    }
  }
  getColmunList() {
    return this.busiRecordMap[this.busiFlag].columnList
  }
  loadData(param: ShortcutRequest): Promise<IResponse> {
    const url = this.busiRecordMap[this.busiFlag].url
    if (this.emptyParam(param) && this.getCacheData()?.length) {
      return new Promise((res, rej) => {
        const result = {
          code: 200,
          data: this.getCacheData(),
          msg: null
        }
        return res(result)
      })
    }
    return request.post({ url, data: param }).then(res => {
      const data = res.data
      if (this.emptyParam(param)) {
        this.busiRecordMap[this.busiFlag].dataCache = data
      }
      return res
    })
  }
  getCacheData() {
    return this.busiRecordMap[this.busiFlag].dataCache
  }
  emptyParam(param: ShortcutRequest) {
    return param.asc == null && !param.keyword && !param.type
  }
  setBusiFlag(busiFlag) {
    this.busiFlag = busiFlag
  }
  getBusiList() {
    const all = 'all_types'
    return [all, ...this.busiRecordMap[this.busiFlag].busiList]
  }
}

export const shortcutOption = new ShortcutOption()
