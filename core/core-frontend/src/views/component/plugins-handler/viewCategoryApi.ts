import request from '@/config/axios'

let plugins, p
const queue = []
let loading = false

export async function loadViewCategory() {
    return new Promise((resolve, reject) => {
        if (plugins) {
            resolve(plugins)
        }
        if (loading) {
            queue.push({ resolve, reject })
        }
        if (!loading && !plugins) {
            loading = true
            request.get({ url: '/xpackComponent/viewPlugins' }).then(res => {
                plugins = res.data
                loading = false
                resolve(plugins)
                while (p = queue.shift()) {
                    p.resolve(plugins)
                }
            })
        }
    })
}
