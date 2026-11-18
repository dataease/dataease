export interface Language {
  el: Recordable
  name: string
}

export interface LocaleDropdownType {
  lang: string
  custom?: boolean
  name?: string
  elLocale?: Language
}
