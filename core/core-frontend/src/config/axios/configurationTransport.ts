const protectedPaths = new Set([
  '/datasource/checkRepeat',
  '/datasource/save',
  '/datasource/update',
  '/datasource/validate',
  '/datasource/getSchema',
  '/datasource/checkApiDatasource',
  '/datasource/loadRemoteFile',
  '/datasource/multidimensionalTables',
  '/engine/save',
  '/engine/validate',
  '/sync/datasource/save',
  '/sync/datasource/update',
  '/sync/datasource/validate',
  '/sync/datasource/getSchema',
  '/sync/datasource/fields',
  '/sync/task/add',
  '/sync/task/update',
  '/sync/task/target/keyPolicy'
])

interface Challenge {
  version: number
  requestId: string
  publicKey: string
}

export const needsConfigurationEncryption = (url: string, method: string): boolean =>
  method.toLowerCase() === 'post' && protectedPaths.has(url)

export const sealConfiguration = async (data: unknown, path: string, challenge: Challenge) => {
  if (challenge.version !== 1 || !challenge.requestId || !challenge.publicKey) {
    throw new Error('Invalid configuration encryption challenge')
  }
  if (!globalThis.crypto?.getRandomValues) throw new Error('Secure randomness unavailable')
  // node-forge is already bundled; Web Crypto subtle is unavailable on ordinary HTTP origins.
  const forge = (await import('node-forge')).default
  const random = (length: number): string =>
    String.fromCharCode(...crypto.getRandomValues(new Uint8Array(length)))
  const key = random(32)
  const iv = random(12)
  const publicKey = forge.pki.publicKeyFromAsn1(
    forge.asn1.fromDer(forge.util.decode64(challenge.publicKey))
  )
  const encryptedKey = publicKey.encrypt(key, 'RSA-OAEP', {
    md: forge.md.sha256.create(),
    mgf1: { md: forge.md.sha256.create() }
  })
  const cipher = forge.cipher.createCipher('AES-GCM', key)
  cipher.start({
    iv,
    additionalData: `dataease:configuration:v1\n${path}\n${challenge.requestId}`,
    tagLength: 128
  })
  cipher.update(forge.util.createBuffer(JSON.stringify(data), 'utf8'))
  if (!cipher.finish()) throw new Error('Configuration encryption failed')
  return {
    version: 1,
    requestId: challenge.requestId,
    encryptedKey: forge.util.encode64(encryptedKey),
    iv: forge.util.encode64(iv),
    ciphertext: forge.util.encode64(cipher.output.getBytes() + cipher.mode.tag.getBytes())
  }
}
