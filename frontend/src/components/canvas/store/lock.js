export default {
    mutations: {
        lock({ curComponent }) {
            curComponent.isLock = true
        },

        unlock({ curComponent }) {
            curComponent.isLock = false
        },
    },
}