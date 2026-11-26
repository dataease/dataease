import { createVNode, render } from 'vue'
import PluginConfirmationDialog from './PluginConfirmationDialog.vue'
import request from '@/config/axios'
import { ensureDekey, rsaEncryp } from '@/utils/encryption'
import CryptoJS from 'crypto-js/crypto-js'

export async function confirmPluginOperation(
  action: string,
  target: string,
  t: (key: string) => string,
  file?: File
): Promise<string> {
  let digest = ''
  if (file) {
    const hash = CryptoJS.algo.SHA256.create()
    for (let offset = 0; offset < file.size; offset += 4 * 1024 * 1024) {
      hash.update(
        CryptoJS.lib.WordArray.create(
          await file.slice(offset, offset + 4 * 1024 * 1024).arrayBuffer()
        )
      )
    }
    digest = hash.finalize().toString()
  }
  return new Promise<string>((resolve, reject) => {
    const host = document.createElement('div')
    document.body.appendChild(host)
    const cleanup = () => {
      render(null, host)
      host.remove()
    }
    render(
      createVNode(PluginConfirmationDialog, {
        t,
        operation:
          action === 'prepare'
            ? target
              ? 'update'
              : 'install'
            : action === 'install' || action === 'update' || action === 'uninstall'
            ? action
            : 'confirm',
        verify: async (password: string) => {
          await ensureDekey()
          const response = await request.post({
            url: '/plugin/authorize',
            handleErrorLocally: true,
            data: { password: rsaEncryp(password), action, target, digest }
          })
          return response.data
        },
        onConfirmed: (ticket: string) => {
          cleanup()
          resolve(ticket)
        },
        onCancel: () => {
          cleanup()
          reject('cancel')
        }
      }),
      host
    )
  })
}
