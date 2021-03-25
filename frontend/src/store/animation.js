export default {
    mutations: {
        addAnimation({ curComponent }, animation) {
            curComponent.animations.push(animation)
        },

        removeAnimation({ curComponent }, index) {
            curComponent.animations.splice(index, 1)
        },
    },
}