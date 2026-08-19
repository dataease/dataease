import type { IAccessor } from '@univerjs/core'
import { LocaleService, UniverInstanceType } from '@univerjs/core'
import { FunctionType, type IFunctionInfo } from '@univerjs/engine-formula'
import { IDescriptionService } from '@univerjs/sheets-formula'
import { InsertFunctionOperation } from '@univerjs/sheets-formula-ui'
import type { IMenuSelectorItem } from '@univerjs/ui'
import { getMenuHiddenObservable, MenuItemType } from '@univerjs/ui'
import {
  DATAEASE_FUNCTION_DROPDOWN_COMPONENT,
  DATAEASE_FUNCTION_DROPDOWN_MENU_ID
} from '../config/ribbon-config'

const COMMON_FUNCTIONS = ['SUMIF', 'SUM', 'AVERAGE', 'IF', 'COUNT', 'SIN', 'MAX']

type DataEaseFunctionItem = {
  name: string
  desc: string
}

type DataEaseFunctionCategory = {
  key: string
  label: string
  functions: DataEaseFunctionItem[]
}

type FunctionCategoryConfig = {
  key: string
  localeKey: string
  type?: FunctionType
  commonFunctions?: string[]
}

const FUNCTION_CATEGORY_CONFIGS: FunctionCategoryConfig[] = [
  {
    key: 'common',
    localeKey: 'sheets-formula-ui.insert.common',
    commonFunctions: COMMON_FUNCTIONS
  },
  {
    key: 'financial',
    localeKey: 'sheets-formula-ui.functionType.financial',
    type: FunctionType.Financial
  },
  {
    key: 'logical',
    localeKey: 'sheets-formula-ui.functionType.logical',
    type: FunctionType.Logical
  },
  {
    key: 'text',
    localeKey: 'sheets-formula-ui.functionType.text',
    type: FunctionType.Text
  },
  {
    key: 'date',
    localeKey: 'sheets-formula-ui.functionType.date',
    type: FunctionType.Date
  },
  {
    key: 'lookup',
    localeKey: 'sheets-formula-ui.functionType.lookup',
    type: FunctionType.Lookup
  },
  {
    key: 'math',
    localeKey: 'sheets-formula-ui.functionType.math',
    type: FunctionType.Math
  },
  {
    key: 'statistical',
    localeKey: 'sheets-formula-ui.functionType.statistical',
    type: FunctionType.Statistical
  },
  {
    key: 'engineering',
    localeKey: 'sheets-formula-ui.functionType.engineering',
    type: FunctionType.Engineering
  },
  {
    key: 'information',
    localeKey: 'sheets-formula-ui.functionType.information',
    type: FunctionType.Information
  },
  {
    key: 'database',
    localeKey: 'sheets-formula-ui.functionType.database',
    type: FunctionType.Database
  }
]

const getCommonFunctions = (
  descriptionService: IDescriptionService,
  localeService: LocaleService,
  functionNames: string[]
): DataEaseFunctionItem[] => {
  const describedFunctions = functionNames
    .map(name => descriptionService.getFunctionInfo(name))
    .filter((item): item is IFunctionInfo => !!item)
    .map(item => ({
      name: item.functionName,
      desc: localeService.t(item.description || '')
    }))

  if (describedFunctions.length > 0) {
    return describedFunctions
  }

  return functionNames.map(name => ({
    name,
    desc: ''
  }))
}

const getTypedFunctions = (
  descriptionService: IDescriptionService,
  localeService: LocaleService,
  type: FunctionType
): DataEaseFunctionItem[] =>
  descriptionService.getSearchListByType(type).map(item => ({
    name: item.name,
    desc: localeService.t(item.desc || '')
  }))

const getFunctionCategories = (accessor: IAccessor): DataEaseFunctionCategory[] => {
  const descriptionService = accessor.get(IDescriptionService)
  const localeService = accessor.get(LocaleService)

  return FUNCTION_CATEGORY_CONFIGS
    .map(config => {
      const functions = config.commonFunctions
        ? getCommonFunctions(descriptionService, localeService, config.commonFunctions)
        : getTypedFunctions(descriptionService, localeService, config.type as FunctionType)

      return {
        key: config.key,
        label: localeService.t(config.localeKey),
        functions
      }
    })
    .filter(category => category.functions.length > 0)
}

export function DataEaseFunctionDropdownMenuFactory(
  accessor: IAccessor
): IMenuSelectorItem<string> {
  let categories: DataEaseFunctionCategory[] = []

  try {
    categories = getFunctionCategories(accessor)
  } catch {
    categories = [
      {
        key: 'common',
        label: '常用函数',
        functions: COMMON_FUNCTIONS.map(name => ({
          name,
          desc: ''
        }))
      }
    ]
  }

  return {
    id: DATAEASE_FUNCTION_DROPDOWN_MENU_ID,
    commandId: InsertFunctionOperation.id,
    type: MenuItemType.SELECTOR,
    icon: 'FunctionIcon',
    tooltip: '函数',
    slot: true,
    selections: [
      {
        label: {
          name: DATAEASE_FUNCTION_DROPDOWN_COMPONENT,
          hoverable: false,
          selectable: false,
          props: {
            categories
          } as any
        },
        value: ''
      }
    ],
    hidden$: getMenuHiddenObservable(accessor, UniverInstanceType.UNIVER_SHEET)
  }
}
