// Only a transport adapter. Playwright owns the isolated HTTP responses.
export default {
  async post({ url, data }) {
    const response = await fetch(url, { method: 'POST', body: JSON.stringify(data), headers: { 'Content-Type': 'application/json' } })
    const result = await response.json()
    if (!response.ok) throw new Error(result.msg)
    return result
  },
  async get({ url }) { return (await fetch(url)).json() }
}
