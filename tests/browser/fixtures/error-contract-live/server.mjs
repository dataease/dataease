import http from 'node:http'
const server = http.createServer(async (req, res) => {
  if (req.url === '/slow.csv')
    await new Promise((resolve) => setTimeout(resolve, 1800))
  if (res.destroyed) return
  res.setHeader('Content-Type', 'text/csv')
  res.writeHead(req.url === '/failed.csv' ? 503 : 200)
  res.end('name,amount\nsynthetic,10\n')
})
server.listen(4208, '127.0.0.1')
for (const signal of ['SIGINT', 'SIGTERM'])
  process.on(signal, () => server.close(() => process.exit()))
