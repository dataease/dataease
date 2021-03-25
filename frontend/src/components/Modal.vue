<template>
    <div class="modal-bg" v-if="show" @click="hide">
        <div class="fadeInLeft animated modal" @click="stopPropagation">
            <slot></slot>
        </div>
    </div>
</template>

<script>
export default {
    model: {
        prop: 'show',
        event: 'change',
    },
    props: {
        show: {
            type: Boolean,
            default: false,
        },
    },
    methods: {
        hide() {
            this.$emit('change')
        },

        stopPropagation(e) {
            e.stopPropagation()
        },
    },
}
</script>

<style lang="scss" scoped>
.modal-bg {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0,0,0,.5);
    z-index: 1001;

    .modal {
        width: 400px;
        background: #fff;
        height: 100%;
    }
}
</style>