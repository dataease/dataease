import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import path from 'node:path'
import {
  generateKeyPairSync,
  privateDecrypt,
  constants,
  createDecipheriv,
  randomUUID
} from 'node:crypto'
const root = fileURLToPath(new URL('.', import.meta.url))
const frontend = path.resolve(root, '../../../../core/core-frontend')
const require = createRequire(path.join(frontend, 'package.json'))
const { createServer } = await import(require.resolve('vite'))
const pair = generateKeyPairSync('rsa', { modulusLength: 2048 })
const publicKey = pair.publicKey
  .export({ type: 'spki', format: 'der' })
  .toString('base64')
const challenges = new Set()
const server = await createServer({
  configFile: false,
  root,
  resolve: {
    alias: [
      {
        find: '/configuration-module',
        replacement: path.join(
          frontend,
          'src/config/axios/configurationTransport.ts'
        )
      }
    ]
  },
  plugins: [
    {
      name: 'synthetic-configuration-backend',
      configureServer(server) {
        server.middlewares.use('/challenge', (_req, res) => {
          const requestId = randomUUID()
          challenges.add(requestId)
          res.setHeader('Content-Type', 'application/json')
          res.end(JSON.stringify({ version: 1, requestId, publicKey }))
        })
        server.middlewares.use('/verify', async (req, res) => {
          res.setHeader('Content-Type', 'application/json')
          try {
            let text = ''
            for await (const chunk of req) text += chunk
            const envelope = JSON.parse(text)
            if (!challenges.delete(envelope.requestId))
              throw new Error('replayed')
            const key = privateDecrypt(
              {
                key: pair.privateKey,
                padding: constants.RSA_PKCS1_OAEP_PADDING,
                oaepHash: 'sha256'
              },
              Buffer.from(envelope.encryptedKey, 'base64')
            )
            const bytes = Buffer.from(envelope.ciphertext, 'base64')
            const aes = createDecipheriv(
              'aes-256-gcm',
              key,
              Buffer.from(envelope.iv, 'base64')
            )
            aes.setAAD(
              Buffer.from(
                `dataease:configuration:v1\n/datasource/checkRepeat\n${envelope.requestId}`
              )
            )
            aes.setAuthTag(bytes.subarray(-16))
            const data = JSON.parse(
              Buffer.concat([
                aes.update(bytes.subarray(0, -16)),
                aes.final()
              ]).toString('utf8')
            )
            res.end(
              JSON.stringify({
                accepted: true,
                type: data.type,
                name: data.name,
                length: data.configuration.length
              })
            )
          } catch {
            res.statusCode = 400
            res.end(JSON.stringify({ accepted: false }))
          }
        })
      }
    }
  ],
  server: {
    host: '127.0.0.1',
    port: 4198,
    strictPort: true,
    allowedHosts: ['configuration-http.test'],
    fs: { allow: [root, frontend] }
  }
})
await server.listen()
for (const signal of ['SIGINT', 'SIGTERM'])
  process.on(signal, async () => {
    await server.close()
    process.exit()
  })
