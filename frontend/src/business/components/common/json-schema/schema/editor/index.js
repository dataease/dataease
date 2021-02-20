import JsonSchemaEditor from './main.vue'

JsonSchemaEditor.install = function (Vue) {
  Vue.component(JsonSchemaEditor.name, JsonSchemaEditor)
}

export default JsonSchemaEditor