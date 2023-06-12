import path from 'path'
import { readdirSync } from 'fs'

export interface Page {
  name: string
  path?: string // 如'pages/dev'
  template?: string
}

function readPages(srcDir: string): Page[] {
  const pagesDir = path.resolve(srcDir, 'pages')
  let pages: Page[] = readdirSync(pagesDir, { withFileTypes: true })
    .filter(o => o.isDirectory() && !/^[._]/.test(o.name) && !/^lib/.test(o.name))
    .map(o => ({ name: o.name, path: path.join('pages', o.name) }))
  if (!pages.length) {
    pages = [
      {
        name: 'index',
        path: ''
      }
    ]
  }

  return pages
}
export const ROOT_DIR = path.resolve(__dirname, '../')

export const PAGES = readPages(path.resolve(ROOT_DIR, 'src'))
