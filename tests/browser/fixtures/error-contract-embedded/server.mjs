import http from 'node:http'
const bi = process.env.ERROR_CONTRACT_LIVE_URL || 'http://127.0.0.1:7070'
const server = http.createServer(async (req, res) => {
  if (req.url === '/api/sysParameter/requestTimeOut') {
    const response = await fetch(bi + req.url)
    res.setHeader('Content-Type', 'application/json')
    res.end(await response.text())
    return
  }
  res.setHeader('Content-Type', 'text/html; charset=utf-8')
  res.end(
    '<!doctype html><html><head><title>Temporary embedding regression host</title></head><body style="margin:0"><div id="designer" style="width:100vw;height:100vh"></div></body></html>'
  )
})
server.listen(4209, '127.0.0.1')
for (const signal of ['SIGTERM', 'SIGINT'])
  process.on(signal, () => server.close(() => process.exit()))
