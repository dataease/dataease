export default {
    mutations: {
        addEvent({ curComponent }, { event, param }) {
            curComponent.events[event] = param
        },

        removeEvent({ curComponent }, event) {
            delete curComponent.events[event]
        },
    },
}