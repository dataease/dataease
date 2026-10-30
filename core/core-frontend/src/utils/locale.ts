export const DEFAULT_LOCALE = 'zh-CN'
export const BUILTIN_LOCALES = ['zh-CN', 'en', 'tw']
let supportedLocales = [...BUILTIN_LOCALES]

export const setSupportedLocales = (languages: string[]) => {
  supportedLocales = [...new Set([...BUILTIN_LOCALES, ...languages])]
}

export const getBrowserLanguages = (): string[] => {
  if (typeof navigator === 'undefined') return []
  return [...new Set([...(navigator.languages || []), navigator.language].filter(Boolean))]
}

const normalizeTag = (language: string) => language.trim().replace(/_/g, '-').toLowerCase()

const LANGUAGE_ALIASES: Record<string, string> = {
  zh: 'zh-CN',
  'zh-hans': 'zh-CN',
  'zh-hant': 'tw',
  'zh-tw': 'tw',
  'zh-hk': 'tw',
  'zh-mo': 'tw',
  'en-us': 'en',
  'en-gb': 'en'
}

// Explicit changes only accept supported identifiers or known aliases.
export const matchSupportedLocale = (
  language: unknown,
  supported = supportedLocales
): string | undefined => {
  if (typeof language !== 'string' || !language.trim()) return
  const tag = normalizeTag(language)
  const exact = supported.find(item => normalizeTag(item) === tag)
  if (exact) return exact
  const alias = LANGUAGE_ALIASES[tag]
  return alias && supported.includes(alias) ? alias : undefined
}

export const matchLocale = (
  language: unknown,
  supported = supportedLocales
): string | undefined => {
  const exact = matchSupportedLocale(language, supported)
  if (exact) return exact
  if (typeof language !== 'string' || !language.trim()) return
  const tag = normalizeTag(language)
  const parts = tag.split('-')
  let alias: string
  if (parts[0] === 'zh') {
    // Script takes precedence over region (for example zh-Hans-HK).
    alias = parts.includes('hans')
      ? 'zh-CN'
      : parts.includes('hant') || parts.some(part => ['tw', 'hk', 'mo'].includes(part))
      ? 'tw'
      : 'zh-CN'
  } else if (parts[0] === 'en') {
    alias = 'en'
  }
  if (alias && supported.includes(alias)) return alias
  // Match a regional browser preference to an available base language.
  return supported.find(item => normalizeTag(item) === parts[0])
}

export const resolveLocale = (candidates: unknown[], supported = supportedLocales): string => {
  for (const candidate of candidates) {
    const match = matchLocale(candidate, supported)
    if (match) return match
  }
  return DEFAULT_LOCALE
}
