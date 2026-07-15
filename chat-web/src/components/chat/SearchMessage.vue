<template>
  <el-dialog
    title="搜索消息"
    :visible.sync="dialogVisible"
    width="560px"
    :close-on-click-modal="true"
    @open="onOpen"
    @close="onClose"
    custom-class="search-dialog"
  >
    <div class="search-body">
      <el-input
        ref="searchInput"
        v-model="keyword"
        placeholder="输入关键词搜索聊天记录..."
        prefix-icon="el-icon-search"
        clearable
        @input="onSearch"
        class="search-input"
      />
      <div class="search-results" v-if="keyword.trim()">
        <div v-if="searching" class="search-tip">搜索中...</div>
        <div v-else-if="!results.length" class="search-tip">未找到相关消息</div>
        <div
          v-else
          v-for="item in results"
          :key="item.localId"
          class="result-item"
          @click="onClickResult(item)"
        >
          <div class="result-header">
            <span class="result-name">{{ item.convName }}</span>
            <span class="result-time">{{ formatTime(item.sendTime) }}</span>
          </div>
          <div class="result-content" v-html="highlight(item.content)"></div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { getDB } from "../../db/index.js";

export default {
  name: "searchMessage",
  data() {
    return {
      dialogVisible: false,
      keyword: "",
      results: [],
      searching: false,
      timer: null
    }
  },
  methods: {
    open() {
      this.dialogVisible = true;
    },
    onOpen() {
      this.$nextTick(() => {
        this.$refs.searchInput && this.$refs.searchInput.focus();
      });
    },
    onClose() {
      this.keyword = "";
      this.results = [];
    },
    onSearch() {
      if (this.timer) clearTimeout(this.timer);
      const kw = this.keyword.trim();
      if (!kw) {
        this.results = [];
        return;
      }
      this.searching = true;
      this.timer = setTimeout(async () => {
        const messages = await getDB().searchMessages(kw);
        const convMap = {};
        // attach conversation name
        const convs = await getDB().loadAllConversations();
        convs.forEach(c => { convMap[c.key] = c.showName; });
        this.results = messages.slice(0, 50).map(m => ({
          ...m,
          convName: convMap[m.convKey] || "未知会话",
          content: typeof m.content === 'string' ? m.content : JSON.stringify(m.content)
        }));
        this.searching = false;
      }, 300);
    },
    highlight(text) {
      if (!text || !this.keyword) return text;
      const kw = this.keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
      return text.replace(new RegExp(kw, 'gi'), m => `<em>${m}</em>`);
    },
    formatTime(ts) {
      if (!ts) return '';
      const d = new Date(ts);
      const now = new Date();
      const fmt = n => String(n).padStart(2, '0');
      if (d.toDateString() === now.toDateString()) {
        return fmt(d.getHours()) + ':' + fmt(d.getMinutes());
      }
      return (d.getMonth()+1) + '/' + d.getDate() + ' ' + fmt(d.getHours()) + ':' + fmt(d.getMinutes());
    },
    onClickResult(item) {
      this.dialogVisible = false;
      // emit event to parent to navigate to the conversation
      this.$emit("select", item);
    }
  }
}
</script>

<style scoped>
.search-input { margin-bottom: 16px; }
.search-tip {
  text-align: center; padding: 32px 0;
  color: var(--im-text-color-secondary); font-size: 14px;
}
.result-item {
  padding: 12px; border-radius: 8px;
  background: var(--im-card-bg, rgba(255,255,255,0.04));
  margin-bottom: 8px; cursor: pointer;
  transition: background 0.15s;
}
.result-item:hover { background: rgba(255,255,255,0.08); }
.result-header {
  display: flex; justify-content: space-between; margin-bottom: 6px;
}
.result-name {
  font-size: 13px; font-weight: 600;
  color: var(--im-color-primary-light-3, #66b1ff);
}
.result-time { font-size: 11px; color: var(--im-text-color-secondary); }
.result-content {
  font-size: 13px; color: var(--im-text-color);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.result-content ::v-deep em {
  color: #ff9500; font-style: normal; font-weight: 600;
}
</style>
