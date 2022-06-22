// 通过控制css变量控制过滤组件弹框样式 de-select-grid除外
import {  attrsMap, styleAttrs } from '@/components/widget/DeWidget/serviceNameFn.js'

export default {
    data() {
        return {
            attrsMap,
            styleAttrs,
            // 过滤组件名css变量映射
            refComNameMap: {
                'de-date': ['--BgDateColor', '--DateColor', '--BrDateColor'],
                'de-select': ['--BgSelectColor', '--SelectColor', '--BrSelectColor'],
                'de-select-tree': ['--BgSelectTreeColor', '--SelectTreeColor', '--BrSelectTreeColor'],
                "de-input-search": ['--BgSearchColor', '--SearchColor', '--BrSearchColor'],
                "de-number-range": ['--BgRangeColor', '--RangeColor', '--BrRangeColor']
            }
        }
    },
    mounted() {
        this.handleCoustomStyle()
    },
    methods: {
        typeTransform() {
            let refNode = this.refComNameMap[this.element.component];
            if (!refNode) return [];
            return refNode
        },
        handleCoustomStyle() {
            // 判断组件是否是在仪表板内部 否则css样式取默认值
            const isPanelDe = this.$parent.handlerInputStyle;
            const { brColor, wordColor, innerBgColor } = this.element.style;
            const newValue = { brColor, wordColor, innerBgColor };
            const cssVar = this.typeTransform();
            this.styleAttrs.forEach((ele, index) => {
                document.documentElement.style.setProperty(cssVar[index],  !isPanelDe ? '' : newValue[ele])
            })
        },
    }
}