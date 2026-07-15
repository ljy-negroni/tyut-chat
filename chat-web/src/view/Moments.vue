<template>
  <el-container class="moments-page">
    <div class="moments-container">
      <!-- 顶部标题栏 -->
      <div class="moments-header">
        <h1 class="moments-title">朋友圈</h1>
        <el-button type="text" class="publish-btn" @click="$refs.publishFeed.open()">
          <i class="el-icon-camera"></i> 发布动态
        </el-button>
      </div>
      <!-- 动态列表区域（暂为占位） -->
      <div class="feed-list">
        <div class="empty-feed" v-if="!feeds.length">
          <i class="el-icon-picture-outline empty-icon"></i>
          <p>暂无动态，去发布第一条吧</p>
        </div>
        <div v-for="feed in feeds" :key="feed.id" class="feed-card">
          <!-- 动态卡片由 Issue #5 实现 -->
        </div>
      </div>
      <!-- 加载更多 -->
      <div class="load-more" v-if="hasMore">
        <el-button type="text" :loading="loadingMore" @click="loadMore">加载更多</el-button>
      </div>
    </div>
    <publish-feed ref="publishFeed" @published="onPublished"></publish-feed>
  </el-container>
</template>

<script>
import PublishFeed from "../components/feed/PublishFeed.vue";
export default {
  name: "moments",
  components: { PublishFeed },
  data() {
    return {
      feeds: [],
      page: 1,
      hasMore: true,
      loadingMore: false
    }
  },
  methods: {
    onPublished() {
      this.page = 1;
      this.feeds = [];
      this.loadTimeline();
    },
    loadTimeline() {},
    loadMore() {}
  }
}
</script>

<style scoped lang="scss">
.moments-page {
  height: 100%;
  background: var(--im-bg-color);
  overflow: hidden;
}
.moments-container {
  width: 100%;
  max-width: 680px;
  margin: 0 auto;
  height: 100%;
  overflow-y: auto;
  padding: 24px 20px;
}
.moments-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--im-border-color);
}
.moments-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--im-text-color);
  margin: 0;
}
.publish-btn {
  font-size: 14px;
  color: var(--im-color-primary);
  &:hover { opacity: 0.8; }
}
.empty-feed {
  text-align: center;
  padding: 80px 0;
  color: var(--im-text-color-secondary);
  .empty-icon { font-size: 48px; margin-bottom: 16px; }
}
.feed-card {
  margin-bottom: 20px;
}
.load-more {
  text-align: center;
  padding: 20px 0;
}
</style>
