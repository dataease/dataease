<template>
    <div class="bg" v-if="show">
        <el-button @click="close" class="close">关闭</el-button>
        <div class="canvas-container">
            <div class="canvas"
                :style="{
                    width: changeStyleWithScale(canvasStyleData.width) + 'px',
                    height: changeStyleWithScale(canvasStyleData.height) + 'px',
                }"
            >
                <ComponentWrapper
                    v-for="(item, index) in componentData"
                    :key="index"
                    :config="item"
                />
            </div>
        </div>
    </div>
</template>

<script>
import { getStyle } from '@/utils/style'
import { mapState } from 'vuex'
import ComponentWrapper from './ComponentWrapper'
import { changeStyleWithScale } from '@/utils/translate'

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
    components: { ComponentWrapper },
    computed: mapState([
        'componentData',
        'canvasStyleData',
    ]),
    methods: {
        changeStyleWithScale,

        getStyle,

        close() {
            this.$emit('change', false)
        },
    },
}
</script>

<style lang="scss" scoped>
.bg {
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    position: fixed;
    background: rgb(0, 0, 0, .5);
    z-index: 10;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: auto;
    padding: 20px;

    .canvas-container {
        width: calc(100% - 40px);
        height: calc(100% - 120px);
        overflow: auto;

        .canvas {
            background: #fff;
            position: relative;
            margin: auto;
        }
    }

    .close {
        position: absolute;
        right: 20px;
        top: 100px;
    }
}
</style>
