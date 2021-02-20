<template>
  <div>
    <div class="jsoneditor-vue" style="height: 400px"></div>
  </div>
</template>

<script>
  import 'jsoneditor/dist/jsoneditor.css';
  import JsonEditor from 'jsoneditor'

  export default {
    props: {
      value: [String, Number, Object, Array],
      expandedOnStart: {
        type: Boolean,
        default: false
      },
      mode: {
        type: String,
        default: "code"
      },
      modes: {
        type: Array,
        default: function () {
          return ["code"];
        }
      }
    },
    watch: {
      value: {
        immediate: true,
        async handler(val) {
          if (!this.internalChange) {
            await this.setEditor(val);
            this.expandAll();
          }
        },
        deep: true
      }
    },
    data() {
      return {
        editor: null,
        error: false,
        json: JSON.parse(this.value ? this.value : "{}"),
        internalChange: false,
        expandedModes: ["tree", "view", "form"],
      };
    },
    mounted() {
      let self = this;
      let options = {
        mode: this.mode,
        modes: this.modes, // allowed modes
        onChange() {
          try {
            let json = self.editor.get();
            self.json = json;
            self.$emit("json-change", json);
            self.internalChange = true;
            self.$emit("input", json);
            self.$nextTick(function () {
              self.internalChange = false;
            });
          } catch (e) {
            self.$emit("has-error", e);
          }
        },
        onModeChange() {
          self.expandAll();
        },
        onError(error) {
          self.$emit("onError", error);
        }
      };
      this.editor = new JsonEditor(
        this.$el.querySelector(".jsoneditor-vue"),
        options,
        this.json
      );
    },
    methods: {
      expandAll() {
        if (
          this.expandedOnStart &&
          this.expandedModes.includes(this.editor.getMode())
        ) {
          this.editor.expandAll();
        }
      },
      async setEditor(value) {
        if (this.editor) this.editor.set(value);
      }
    }
  };
</script>

<style scoped>
  .ace_line_group {
    text-align: left;
  }

  .json-editor-container {
    display: flex;
    width: 100%;
  }

  .json-editor-container .tree-mode {
    width: 50%;
  }

  .json-editor-container .code-mode {
    flex-grow: 1;
  }

  .jsoneditor-btns {
    text-align: center;
    margin-top: 10px;
  }

  .jsoneditor-vue .jsoneditor-outer {
    height: 300px;
  }

  .jsoneditor-vue div.jsoneditor-tree {
    min-height: 350px;
  }

  .json-save-btn {
    background-color: #20A0FF;
    border: none;
    color: #fff;
    padding: 5px 10px;
    border-radius: 5px;
  }

  .json-save-btn:focus {
    outline: none;
  }

  .json-save-btn[disabled] {
    background-color: #1D8CE0;
  }

  code {
    background-color: #f5f5f5;
  }

  /deep/ .jsoneditor-poweredBy {
    visibility: hidden;
  }

  /deep/ .jsoneditor-contextmenu .jsoneditor-menu li button.jsoneditor-selected {
    background-color: #1E9FFB;
  }

  /deep/ jsoneditor-tree {
    overflow: auto;
  }

  /deep/ .jsoneditor {
    border-color: #1E9FFB;
    border-radius: 3px;
  }
</style>
