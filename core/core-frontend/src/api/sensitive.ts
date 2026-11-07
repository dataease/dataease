import request from '@/config/axios'

export type SecretType =
  | 'DATASOURCE_PASSWORD'
  | 'DATASOURCE_SSH_PASSWORD'
  | 'DATASOURCE_SSH_KEY_PASSWORD'
  | 'DATASOURCE_REMOTE_PASSWORD'
  | 'ENGINE_PASSWORD'
  | 'SYNC_PASSWORD'

// A separate, short-lived response key is sent only inside the encrypted request.
export const revealSensitive = async (
  resourceId: string | number,
  secretType: SecretType,
  password: string
): Promise<string> => {
  const forge = (await import('node-forge')).default
  const bytes = crypto.getRandomValues(new Uint8Array(32))
  const key = String.fromCharCode(...bytes)
  try {
    const result = await request.post({
      url: '/sensitive/reveal',
      data: {
        resourceId: String(resourceId),
        secretType,
        password,
        responseKey: forge.util.encode64(key)
      },
      handleErrorLocally: true
    })
    const iv = forge.util.decode64(result.data.iv)
    const encrypted = forge.util.decode64(result.data.ciphertext)
    if (iv.length !== 12 || encrypted.length < 16) throw new Error('Invalid encrypted response')
    const decipher = forge.cipher.createDecipher('AES-GCM', key)
    decipher.start({
      iv,
      additionalData: 'dataease:sensitive-reveal:v1',
      tagLength: 128,
      tag: forge.util.createBuffer(encrypted.slice(-16))
    })
    decipher.update(forge.util.createBuffer(encrypted.slice(0, -16)))
    if (!decipher.finish()) throw new Error('Invalid encrypted response')
    return forge.util.decodeUtf8(decipher.output.getBytes())
  } finally {
    bytes.fill(0)
  }
}
