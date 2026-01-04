<template>
  <div class="json-editor">
    <textarea ref="textarea" />
  </div>
</template>

<script>
import CodeMirror from 'codemirror'
import 'codemirror/addon/lint/lint.css'
import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/rubyblue.css'
require('script-loader!jsonlint')
import 'codemirror/mode/javascript/javascript'
import 'codemirror/addon/lint/lint'
import 'codemirror/addon/lint/json-lint'

export default {
  name: 'JsonEditor',
  /* eslint-disable vue/require-prop-types */
  props: ['value'],
  data() {
    return {
      jsonEditor: false
    }
  },
  watch: {
    value: {
      handler(value) {
        if (!this.jsonEditor) return
        let formattedValue
        if (typeof value === 'string' && value.trim()) {
          // 如果是字符串，尝试解析为 JSON 对象再格式化
          try {
            const parsed = JSON.parse(value.trim())
            formattedValue = JSON.stringify(parsed, null, 2)
          } catch (e) {
            // 如果解析失败，可能是双重转义，尝试再次解析
            try {
              const firstParse = JSON.parse(value.trim())
              if (typeof firstParse === 'string') {
                const parsed = JSON.parse(firstParse)
                formattedValue = JSON.stringify(parsed, null, 2)
              } else {
                formattedValue = JSON.stringify(firstParse, null, 2)
              }
            } catch (e2) {
              // 如果还是失败，直接使用原字符串（去除首尾空白）
              formattedValue = value.trim()
            }
          }
        } else if (value && typeof value === 'object') {
          // 如果是对象，直接格式化
          formattedValue = JSON.stringify(value, null, 2)
        } else {
          formattedValue = value || ''
        }
        const editorValue = this.jsonEditor.getValue()
        // 比较格式化后的值，确保更新
        if (formattedValue !== editorValue) {
          this.jsonEditor.setValue(formattedValue)
        }
      },
      immediate: false
    }
  },
  mounted() {
    this.jsonEditor = CodeMirror.fromTextArea(this.$refs.textarea, {
      lineNumbers: true,
      mode: 'application/json',
      gutters: ['CodeMirror-lint-markers'],
      theme: 'rubyblue',
      lint: true
    })

    // 初始化时格式化值
    let initialValue
    if (typeof this.value === 'string') {
      try {
        const parsed = JSON.parse(this.value.trim())
        initialValue = JSON.stringify(parsed, null, 2)
      } catch (e) {
        initialValue = this.value.trim()
      }
    } else {
      initialValue = JSON.stringify(this.value, null, 2)
    }
    this.jsonEditor.setValue(initialValue)
    this.jsonEditor.on('change', cm => {
      this.$emit('changed', cm.getValue())
      this.$emit('input', cm.getValue())
    })
  },
  methods: {
    getValue() {
      return this.jsonEditor.getValue()
    }
  }
}
</script>

<style scoped>
.json-editor{
  height: 100%;
  position: relative;
}
.json-editor >>> .CodeMirror {
  height: auto;
  min-height: 300px;
}
.json-editor >>> .CodeMirror-scroll{
  min-height: 300px;
}
.json-editor >>> .cm-s-rubyblue span.cm-string {
  color: #F08047;
}
</style>
