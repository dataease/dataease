import { app, BrowserWindow } from 'electron'
import * as path from 'path'

// 本地启动的vue项目路径
const vueProjectAddress = 'http://localhost:5173'
/**
 * 创建一个新的窗口
 */
const createWindow = () => {
  const win = new BrowserWindow({
    webPreferences: {
      contextIsolation: false, // 是否开启隔离上下文
      nodeIntegration: true // 渲染进程使用Node API
    }
  })
  if (app.isPackaged) {
    win.loadFile(path.join(__dirname, '../index.html'))
  } else {
    win.loadURL(vueProjectAddress)
  }
}
// 打开窗口
app.whenReady().then(() => {
  createWindow() // 创建窗口
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})
// 关闭窗口
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})
