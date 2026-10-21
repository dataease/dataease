import { createServer } from 'node:http'
import { readFile } from 'node:fs/promises'

const port = Number(process.env.PLAYWRIGHT_DEMO_PORT || '4188')
if (!Number.isInteger(port) || port < 1 || port > 65535) {
  throw new Error('PLAYWRIGHT_DEMO_PORT must be between 1 and 65535')
}
const html = await readFile(new URL('./index.html', import.meta.url))
const server = createServer((req, res) => {
  if (req.url === '/healthz') {
    res.writeHead(200, { 'Content-Type': 'text/plain' })
    res.end('ok')
  } else if (req.url === '/' || req.url === '/index.html') {
    res.writeHead(200, {
      'Content-Type': 'text/html; charset=utf-8',
      'Cache-Control': 'no-store'
    })
    res.end(html)
  } else {
    res.writeHead(404)
    res.end()
  }
})
server.listen(port, '127.0.0.1', () => {
  console.log(`Browser tooling fixture: http://127.0.0.1:${port}`)
})
for (const signal of ['SIGINT', 'SIGTERM']) {
  process.on(signal, () => server.close())
}
