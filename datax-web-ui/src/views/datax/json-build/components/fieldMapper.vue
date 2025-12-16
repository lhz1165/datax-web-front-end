<template>
  <div class="app-container field-mapper-container">
    <div class="field-mapper-header">
      <span class="field-mapper-title">字段映射设置</span>
      <span class="field-mapper-desc">先点击左侧源字段，再点击右侧目标字段，将自动建立一对一映射关系。</span>
    </div>
    <div class="field-mapper-body">
      <div class="field-mapper-columns">
        <div class="field-mapper-column">
          <div class="field-mapper-column-header">源端字段</div>
          <div class="field-mapper-list">
            <div
              v-for="(c, index) in fromColumnsList"
              :key="c"
              class="field-item"
              :class="{
                'is-selected': selectedSourceIndex === index,
                'is-mapped': !!findMappingBySource(index)
              }"
              @click="handleSourceClick(index)"
            >
              {{ c }}
            </div>
          </div>
        </div>

        <div class="field-mapper-lines-wrapper" :style="{ height: svgHeight + 'px' }">
          <svg
            class="field-mapper-lines"
            :width="svgWidth"
            :height="svgHeight"
          >
            <defs>
              <marker
                id="arrow"
                markerWidth="8"
                markerHeight="8"
                refX="8"
                refY="4"
                orient="auto"
              >
                <path d="M0,0 L8,4 L0,8" fill="none" stroke="#c0c4cc" />
              </marker>
            </defs>
            <line
              v-for="(m, idx) in mappings"
              :key="idx"
              :x1="lineStartX"
              :y1="getSourceY(m.fromIndex)"
              :x2="lineEndX"
              :y2="getTargetY(m.toIndex)"
              stroke="#c0c4cc"
              stroke-width="1"
              marker-end="url(#arrow)"
            />
          </svg>
        </div>

        <div class="field-mapper-column">
          <div class="field-mapper-column-header">目标字段</div>
          <div class="field-mapper-list">
            <div
              v-for="(c, index) in toColumnsList"
              :key="c"
              class="field-item"
              :class="{
                'is-mapped': !!findMappingByTarget(index),
                'is-waiting': selectedSourceIndex !== null && !findMappingByTarget(index)
              }"
              @click="handleTargetClick(index)"
            >
              {{ c }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FieldMapper',
  data() {
    return {
      fromColumnsList: [],
      toColumnsList: [],
      selectedSourceIndex: null,
      mappings: [], // { fromIndex, toIndex }
      itemHeight: 32,
      itemGap: 8,
      topOffset: 16,
      svgWidth: 160
    }
  },
  computed: {
    svgHeight() {
      const count = Math.max(this.fromColumnsList.length, this.toColumnsList.length)
      if (!count) {
        return 0
      }
      const step = this.itemHeight + this.itemGap
      return this.topOffset * 2 + this.itemHeight + (count - 1) * step
    },
    lineStartX() {
      return 10
    },
    lineEndX() {
      return this.svgWidth - 10
    }
  },
  mounted() {
  },
  methods: {
    handleSourceClick(index) {
      // 如果当前没有在选择源字段，并且该源字段已经有映射，则直接删除该映射（作为“删除连线”交互）
      if (this.selectedSourceIndex === null) {
        const existing = this.findMappingBySource(index)
        if (existing) {
          this.mappings = this.mappings.filter(m => m.fromIndex !== index)
          return
        }
      }
      // 再次点击当前选中的源字段，取消选中
      if (this.selectedSourceIndex === index) {
        this.selectedSourceIndex = null
        return
      }
      // 选择一个新的源字段准备建立映射
      const existing = this.findMappingBySource(index)
      if (existing) {
        this.mappings = this.mappings.filter(m => m.fromIndex !== index)
      }
      this.selectedSourceIndex = index
    },
    handleTargetClick(index) {
      // 如果当前没有选中的源字段，并且该目标字段已经有映射，则直接删除该映射（作为“删除连线”交互）
      if (this.selectedSourceIndex === null) {
        const existing = this.findMappingByTarget(index)
        if (existing) {
          this.mappings = this.mappings.filter(m => m.toIndex !== index)
        }
        return
      }
      // 正常的一对一映射：先选源，再点目标
      // 删除当前源或该目标上已有的映射，然后建立新的映射
      this.mappings = this.mappings.filter(m => m.fromIndex !== this.selectedSourceIndex && m.toIndex !== index)
      this.mappings.push({ fromIndex: this.selectedSourceIndex, toIndex: index })
      this.selectedSourceIndex = null
    },
    findMappingBySource(index) {
      return this.mappings.find(m => m.fromIndex === index) || null
    },
    findMappingByTarget(index) {
      return this.mappings.find(m => m.toIndex === index) || null
    },
    getSourceY(index) {
      const step = this.itemHeight + this.itemGap
      return this.topOffset + this.itemHeight / 2 + index * step
    },
    getTargetY(index) {
      const step = this.itemHeight + this.itemGap
      return this.topOffset + this.itemHeight / 2 + index * step
    },
    getLColumns() {
      const sorted = this.mappings.slice().sort((a, b) => a.fromIndex - b.fromIndex)
      return sorted.map(m => this.fromColumnsList[m.fromIndex])
    },
    getRColumns() {
      const sorted = this.mappings.slice().sort((a, b) => a.fromIndex - b.fromIndex)
      return sorted.map(m => this.toColumnsList[m.toIndex])
    }
  }
}
</script>

<style lang="scss" scoped>
.field-mapper-container {
  padding: 16px 20px;
}

.field-mapper-header {
  margin-bottom: 16px;
}

.field-mapper-title {
  font-size: 16px;
  font-weight: 600;
  margin-right: 12px;
}

.field-mapper-desc {
  font-size: 13px;
  color: #666;
}

.field-mapper-body {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
}

.field-mapper-columns {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.field-mapper-column {
  flex: 1;
}

.field-mapper-column-header {
  font-weight: 500;
  margin-bottom: 8px;
}

.field-mapper-lines-wrapper {
  flex: 0 0 160px;
  position: relative;
}

.field-mapper-lines {
  width: 100%;
  height: 100%;
}

.field-mapper-list {
  display: flex;
  flex-direction: column;
}

.field-item {
  height: 32px;
  line-height: 32px;
  padding: 0 10px;
  margin-bottom: 8px;
  background-color: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 2px;
  cursor: pointer;
  transition: all 0.15s ease;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.field-item:hover {
  background-color: #ecf5ff;
  border-color: #c6e2ff;
}

.field-item.is-selected {
  background-color: #ecf5ff;
  border-color: #409eff;
}

.field-item.is-mapped {
  background-color: #f0f9eb;
  border-color: #67c23a;
}

.field-item.is-waiting {
  box-shadow: 0 0 0 1px #409eff inset;
}
</style>
