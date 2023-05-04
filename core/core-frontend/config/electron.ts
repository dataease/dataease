import electron from 'vite-plugin-electron'
import electronRenderer from 'vite-plugin-electron-renderer'
export default {
  plugins: [
    electron({
      entry: 'electron-main/index.ts' // 主进程文件
    }),
    electronRenderer()
  ]
}
