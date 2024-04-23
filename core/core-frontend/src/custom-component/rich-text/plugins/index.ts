import tinymce from 'tinymce/tinymce'

const plugins = import.meta.glob(['./*.ts', '!./index.ts'], { eager: true })
for (const pluginName in plugins) {
  const plugin = plugins[pluginName]['default']
  const exist = tinymce.PluginManager.get(plugin.name)
  if (exist) {
    continue
  }
  tinymce.PluginManager.add(plugin.name, plugin.plugin)
}
