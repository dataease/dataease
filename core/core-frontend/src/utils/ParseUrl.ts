function parseUrl(url?: string) {
  const [pathname, params = ''] = (typeof url === 'string' ? url : '').split('?')
  const [_, path] = pathname.split('#/')
  return {
    path,
    query: params
      .split('&')
      .filter(Boolean)
      .map(ele => ele.split('='))
      .reduce((pre, next) => {
        const [key, value] = next
        pre[key] = value
        return pre
      }, {})
  }
}

export { parseUrl }
