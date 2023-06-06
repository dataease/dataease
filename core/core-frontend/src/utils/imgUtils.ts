const basePath = import.meta.env.VITE_API_BASEPATH

export function imgUrlTrans(url) {
  return url
    ? (basePath.endsWith('/') ? basePath.substring(0, basePath.length - 1) : basePath) + url
    : null
}
