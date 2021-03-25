<template>
    <div class="rect-shape">
        <v-text :propValue="element.propValue" :element="element" />
    </div>
</template>

<script>
export default {
    props: {
        element: {
            type: Object,
        },
    },
}
</script>

<style lang="scss" scoped>
.rect-shape {
    width: 100%;
    height: 100%;
    overflow: auto;
}
</style>