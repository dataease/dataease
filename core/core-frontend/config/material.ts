import { existsSync } from 'fs'
import { resolve } from 'path'
import type { Plugin } from 'vite'

// The public frontend only knows the extension contract. Proprietary UI stays in XPack.
export function materialExtension(): Plugin {
  const entry = resolve(
    __dirname,
    '../../../de-xpack/xpack-base/src/main/frontend/material/index.ts'
  )
  const fallback = '\0dataease-material-unavailable'
  const enabled = '\0dataease-material-enabled'
  return {
    name: 'dataease-material-extension',
    resolveId(id) {
      if (id === 'virtual:dataease-material') return existsSync(entry) ? entry : fallback
      if (id === 'virtual:dataease-material-enabled') return enabled
    },
    load(id) {
      if (id === fallback) return 'export default { render() { return null } }'
      if (id === enabled) return `export default ${existsSync(entry)}`
    }
  }
}
