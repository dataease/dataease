import { createVNode, render } from 'vue'
import PluginConfirmationDialog from './PluginConfirmationDialog.vue'
import request from '@/config/axios'
import { ensureDekey, rsaEncryp } from '@/utils/encryption'
import CryptoJS from 'crypto-js/crypto-js'

export interface PluginOperationAuthorization {
  sessionId: string
  ticket: string
  expiresAt: number
}

export const pluginOperationHeaders = (authorization: PluginOperationAuthorization) => ({
  'X-DE-Plugin-Session': authorization.sessionId,
  'X-DE-Plugin-Confirmation': authorization.ticket
})

export async function confirmPluginOperation(
  action: string,
  target: string,
  t: (key: string) => string,
  file?: File,
  sessionId?: string
): Promise<PluginOperationAuthorization> {
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
  return new Promise<PluginOperationAuthorization>((resolve, reject) => {
    let authorization: PluginOperationAuthorization
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
            headers: sessionId ? { 'X-DE-Plugin-Session': sessionId } : {},
            data: { password: rsaEncryp(password), action, target, digest }
          })
          const grant = response.data
          if (
            !grant ||
            typeof grant.sessionId !== 'string' ||
            !grant.sessionId ||
            typeof grant.ticket !== 'string' ||
            !grant.ticket ||
            typeof grant.expiresAt !== 'number' ||
            (sessionId && grant.sessionId !== sessionId)
          )
            throw new Error('Invalid plugin authorization')
          authorization = grant
          return grant.ticket
        },
        onConfirmed: () => {
          cleanup()
          resolve(authorization)
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
