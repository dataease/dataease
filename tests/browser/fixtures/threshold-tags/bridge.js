// Only read-only candidates and unrelated rich text/icon boundaries are replaced.
export const useI18n = () => ({ t: key => key })
export const Icon = { template: '<span><slot /></span>' }
export default { render() { return null } }
export const sanitizeHtml = value => value
export const groups = Array.from({ length: 4 }, (_, i) => ({
  chat_id: `group-${i}`, name: i === 3 ? 'Long group name ABCDEFGHIJKLMNOPQRSTUVWXYZ 长名称测试群' : `Group ${i + 1}`
}))
export const queryCategoryStatusApi = async () => ({ data: ['email', 'lark', 'larksuite'].map(name => ({ name, enable: true })) })
export const larkGroupOptionApi = async () => ({ data: { valid: true, groupList: groups } })
export const larksuiteGroupOptionApi = larkGroupOptionApi
export const webhookOptionsApi = async () => ({ data: [{ id: 'hook', name: 'Webhook recipient' }] })
export const queryAllSubjectsApi = async type => ({ data: type ? [] : [{ id: 'user', name: 'Test recipient', account: 'test' }] })
export const thresholdPreviewApi = () => { throw new Error('Preview requests are outside this fixture') }
