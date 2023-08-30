export default {
  server: {
    proxy: {
      '/api/f': {
        // target: 'http://192.168.31.38:8100',
        target: 'http://localhost:8100',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api\/f/, '')
      },
      // 使用 proxy 实例
      '/api': {
        // target: 'http://qa-de2.fit2cloud.com',
        // target: 'http://192.168.31.74:8100',
        // target: 'https://de2.fit2cloud.com',
        // target: 'http://localhost:8100',
        target: 'http://192.168.0.121:9080',
        // target: 'http://localhost:8100',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, 'de2api')
      }
    },
    port: 8080
  }
}
