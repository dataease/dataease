import type { ILanguagePack } from '@univerjs/core'

const isLocaleObject = (value: unknown): value is ILanguagePack =>
  typeof value === 'object' && value !== null && !Array.isArray(value)

const mergeLocaleObject = (base: ILanguagePack, patch: ILanguagePack): ILanguagePack => {
  const result: ILanguagePack = { ...base }

  Object.entries(patch).forEach(([key, patchValue]) => {
    const baseValue = result[key]
    // Univer's mergeLocales only merges top-level namespaces. Compatibility patches
    // must merge recursively, otherwise a partial namespace erases official texts.
    result[key] =
      isLocaleObject(baseValue) && isLocaleObject(patchValue)
        ? mergeLocaleObject(baseValue, patchValue)
        : patchValue
  })

  return result
}

export const mergeLocalePatches = (...locales: ILanguagePack[]): ILanguagePack =>
  locales.reduce<ILanguagePack>(mergeLocaleObject, {})

interface MissingLocaleText {
  findReplacePartialSuccess: string
  formulaProgress: {
    analyzing: string
    calculating: string
    arrayAnalysis: string
    arrayCalculation: string
    done: string
  }
  filterConfirm: {
    error: string
    notAllowedToInsertRange: string
  }
  ribbonMenu: string
}

interface LocaleCompatibilityOptions {
  officialLocales: ILanguagePack[]
  conditionalFormattingIconSet: ILanguagePack
  permissionDialog: ILanguagePack
  numberFormatInfo: ILanguagePack
  mergeConfirm: ILanguagePack
  missingText: MissingLocaleText
}

/**
 * Univer 0.25.1 has several locale namespace mismatches and omits some core locale packs.
 * Keep the compatibility mapping isolated so it can be removed after the upstream keys align.
 */
export const createUniverLocaleCompatibilityPatch = (
  options: LocaleCompatibilityOptions
): ILanguagePack => {
  const {
    officialLocales,
    conditionalFormattingIconSet,
    permissionDialog,
    numberFormatInfo,
    mergeConfirm,
    missingText
  } = options

  return mergeLocalePatches(...officialLocales, {
    sheet: {
      cf: {
        iconSet: {
          direction: conditionalFormattingIconSet.direction,
          shape: conditionalFormattingIconSet.shape,
          mark: conditionalFormattingIconSet.mark,
          rank: conditionalFormattingIconSet.rank
        }
      }
    },
    'find-replace': {
      replace: {
        'partial-success': missingText.findReplacePartialSuccess
      }
    },
    'sheets-filter-ui': {
      permission: {
        filterErr: permissionDialog.filterErr
      }
    },
    'sheets-formula': {
      progress: {
        analyzing: missingText.formulaProgress.analyzing,
        calculating: missingText.formulaProgress.calculating,
        'array-analysis': missingText.formulaProgress.arrayAnalysis,
        'array-calculation': missingText.formulaProgress.arrayCalculation,
        done: missingText.formulaProgress.done
      }
    },
    'sheets-hyper-link-ui': {
      permission: {
        hyperLinkErr: permissionDialog.hyperLinkErr
      }
    },
    'sheets-ui': {
      filter: {
        confirm: {
          error: missingText.filterConfirm.error,
          notAllowedToInsertRange: missingText.filterConfirm.notAllowedToInsertRange
        }
      },
      info: {
        error: numberFormatInfo.error,
        forceStringInfo: numberFormatInfo.forceStringInfo
      },
      merge: {
        confirm: {
          warning: mergeConfirm.warning,
          dismantleMergeCellWarning: mergeConfirm.dismantleMergeCellWarning
        }
      }
    },
    ui: {
      ribbon: {
        menu: missingText.ribbonMenu
      }
    }
  })
}
