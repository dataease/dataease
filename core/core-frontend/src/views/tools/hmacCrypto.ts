import { HmacInitializationError, type HmacEnvelope } from '@/config/axios/hmacTransport'

export type HmacSigner = (message: string) => Promise<string>

interface Recipient {
  publicKey: string
  requestId: string
  open: (envelope: HmacEnvelope) => Promise<HmacSigner>
}

const encode = (bytes: ArrayBuffer): string => btoa(String.fromCharCode(...new Uint8Array(bytes)))
const decode = (text: string): Uint8Array => Uint8Array.from(atob(text), char => char.charCodeAt(0))
const encoder = new TextEncoder()

const randomBytes = (length: number): Uint8Array => {
  if (!globalThis.crypto?.getRandomValues) {
    throw new HmacInitializationError('random_source_required')
  }
  return crypto.getRandomValues(new Uint8Array(length))
}

export const createHmacRecipient = async (): Promise<Recipient> => {
  const requestId = Array.from(randomBytes(16), byte => byte.toString(16).padStart(2, '0')).join('')
  const aad = `2\n${requestId}\ntrue`
  if (globalThis.isSecureContext && globalThis.crypto?.subtle) {
    const subtle = crypto.subtle
    const recipient = await subtle.generateKey(
      {
        name: 'RSA-OAEP',
        modulusLength: 2048,
        publicExponent: new Uint8Array([1, 0, 1]),
        hash: 'SHA-256'
      },
      false,
      ['unwrapKey']
    )
    return {
      publicKey: encode(await subtle.exportKey('spki', recipient.publicKey)),
      requestId,
      async open(envelope) {
        const { encryptedKey, iv, ciphertext } = checked(envelope)
        const wrappingKey = await subtle.unwrapKey(
          'raw',
          encryptedKey,
          recipient.privateKey,
          'RSA-OAEP',
          { name: 'AES-GCM' },
          false,
          ['unwrapKey']
        )
        const key = await subtle.unwrapKey(
          'raw',
          ciphertext,
          wrappingKey,
          { name: 'AES-GCM', iv, additionalData: encoder.encode(aad), tagLength: 128 },
          { name: 'HMAC', hash: 'SHA-256' },
          false,
          ['sign']
        )
        return async message => encode(await subtle.sign('HMAC', key, encoder.encode(message)))
      }
    }
  }

  // Ordinary HTTP has getRandomValues, but no SubtleCrypto. Keep the same wire algorithms.
  // Load the JS implementation only here; never fall back to Math.random for key generation.
  const forge = (await import('node-forge')).default
  const binaryRandom = (count: number) => String.fromCharCode(...randomBytes(count))
  type GenerationState = { keys: import('node-forge').pki.rsa.KeyPair }
  // Forge documents this incremental API, but @types/node-forge omits it.
  const rsa = forge.pki.rsa as typeof forge.pki.rsa & {
    createKeyPairGenerationState: (
      bits: number,
      exponent: number,
      options: {
        prng: { getBytesSync: (count: number) => string }
      }
    ) => GenerationState
    stepKeyPairGenerationState: (state: GenerationState, milliseconds: number) => boolean
  }
  const state = rsa.createKeyPairGenerationState(2048, 65537, {
    prng: { getBytesSync: binaryRandom }
  })
  const pair = await new Promise<import('node-forge').pki.rsa.KeyPair>((resolve, reject) => {
    const step = () => {
      try {
        if (rsa.stepKeyPairGenerationState(state, 20)) resolve(state.keys)
        else setTimeout(step, 0)
      } catch (error) {
        reject(error)
      }
    }
    step()
  })
  const publicKey = forge.util.encode64(
    forge.asn1.toDer(forge.pki.publicKeyToAsn1(pair.publicKey)).getBytes()
  )
  return {
    publicKey,
    requestId,
    async open(envelope) {
      const { encryptedKey, iv, ciphertext } = checked(envelope)
      let wrappingKey = pair.privateKey.decrypt(String.fromCharCode(...encryptedKey), 'RSA-OAEP', {
        md: forge.md.sha256.create(),
        mgf1: { md: forge.md.sha256.create() }
      })
      if (wrappingKey.length !== 32) throw new HmacInitializationError('initialization_failed')
      const decipher = forge.cipher.createDecipher('AES-GCM', wrappingKey)
      wrappingKey = ''
      decipher.start({
        iv: String.fromCharCode(...iv),
        additionalData: aad,
        tagLength: 128,
        tag: forge.util.createBuffer(String.fromCharCode(...ciphertext.slice(-16)))
      })
      decipher.update(forge.util.createBuffer(String.fromCharCode(...ciphertext.slice(0, -16))))
      if (!decipher.finish()) throw new HmacInitializationError('initialization_failed')
      // HTTP cannot provide non-extractable native keys; retain the signer only in a closure.
      const hmac = forge.hmac.create()
      hmac.start('sha256', decipher.output.getBytes())
      return async message => {
        hmac.start(null, null)
        hmac.update(forge.util.encodeUtf8(message))
        return forge.util.encode64(hmac.digest().getBytes())
      }
    }
  }
}

const checked = (envelope: HmacEnvelope) => {
  if (!envelope.encryptedKey || !envelope.iv || !envelope.ciphertext) {
    throw new HmacInitializationError('initialization_failed')
  }
  const encryptedKey = decode(envelope.encryptedKey)
  const iv = decode(envelope.iv)
  const ciphertext = decode(envelope.ciphertext)
  if (encryptedKey.length !== 256 || iv.length !== 12 || ciphertext.length <= 16) {
    throw new HmacInitializationError('initialization_failed')
  }
  return { encryptedKey, iv, ciphertext }
}
