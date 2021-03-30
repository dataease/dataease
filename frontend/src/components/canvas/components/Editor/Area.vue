<template>
    <div :style="{
        left: start.x + 'px',
        top: start.y + 'px',
        width: width + 'px',
        height: height + 'px',
    }" class="area"></div>
</template>

<script>
export default {
    props: {
        start: {
            type: Object,
        },
        width: {
            type: Number,
        },
        height: {
            type: Number,
        },
    },
}
</script>

<style lang="scss" scoped>
.area {
    border: 1px solid #70c0ff;
    position: absolute;
}
</style>