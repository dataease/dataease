export const setHtmlPageLang = (locale: string) => {
  document.querySelector('html')?.setAttribute('lang', locale)
}
