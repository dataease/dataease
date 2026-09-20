import { AbstractRendererPlugin } from '@antv/g'

let rendererId = 0

export class SvgGradientPlugin extends AbstractRendererPlugin {
  name = 'dataease-svg-gradient'

  init() {
    const prefix = `dataease-svg-${++rendererId}-`
    const gradients = new Map<string, Element>()
    const references: {
      element: Element
      attribute: string
      original: string
      isolated: string
    }[] = []
    this.addRenderingPlugin({
      apply: context => {
        // 绘制前还原引用，让 AntV 仍能按原 ID 回收旧渐变；绘制结束后再隔离浏览器引用。
        context.renderingService.hooks.beginFrame.tap(this.name, () => {
          references.forEach(({ element, attribute, original, isolated }) => {
            if (element.getAttribute(attribute) === isolated) {
              element.setAttribute(attribute, original)
            }
          })
          references.length = 0
        })
        context.renderingService.hooks.endFrame.tap(this.name, () => {
          const svg = context.contextService.getDomElement() as unknown as SVGSVGElement
          const defs = svg.querySelector('defs')
          if (!defs) return

          // AntV 跨画布复用渐变 ID；保留原定义供内部缓存使用，实际绘制引用实例独有的副本。
          const activeIds = new Set<string>()
          defs.querySelectorAll('linearGradient, radialGradient').forEach(source => {
            const id = source.id
            if (!id || id.startsWith(prefix)) return
            activeIds.add(id)
            const copy = source.cloneNode(true) as Element
            copy.id = prefix + id
            const previous = gradients.get(id)
            if (previous?.isEqualNode(copy)) return
            if (previous) {
              previous.replaceWith(copy)
            } else {
              defs.appendChild(copy)
            }
            gradients.set(id, copy)
          })

          // 数据更新或图元销毁后同步移除副本，避免在持续刷新时累积渐变节点。
          gradients.forEach((gradient, id) => {
            if (!activeIds.has(id)) {
              gradient.remove()
              gradients.delete(id)
            }
          })
          if (!gradients.size) return

          svg.querySelectorAll('[fill], [stroke]').forEach(element => {
            for (const attribute of ['fill', 'stroke']) {
              const value = element.getAttribute(attribute)
              const match = value?.match(/^url\(["']?#([^"')]+)["']?\)$/)
              const gradient = match && gradients.get(match[1])
              if (gradient && value) {
                const isolated = `url(#${gradient.id})`
                element.setAttribute(attribute, isolated)
                references.push({ element, attribute, original: value, isolated })
              }
            }
          })
        })
        context.renderingService.hooks.destroy.tap(this.name, () => {
          gradients.forEach(gradient => gradient.remove())
          gradients.clear()
          references.length = 0
        })
      }
    })
  }

  destroy() {
    this.removeAllRenderingPlugins()
  }
}
