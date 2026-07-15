<template>
  <div class="feed-item">
    <!-- 头部：头像+昵称+时间 -->
    <div class="feed-header">
      <head-image :name="feed.nickName" :url="feed.headImage" :size="40" @click.native="$emit('userClick', feed.userId)" />
      <div class="header-info">
        <span class="nick-name">{{ feed.nickName }}</span>
        <span class="time">{{ $date.formatTime(feed.createdTime) }}</span>
      </div>
      <el-dropdown v-if="isOwner" trigger="click" class="more-btn" @command="handleCommand">
        <span class="el-icon-more"></span>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="delete">删除</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>

    <!-- 文字内容 -->
    <div class="feed-content" v-if="feed.content">
      <p :class="{ collapsed: !expanded && needExpand }" ref="contentText">{{ feed.content }}</p>
      <span v-if="needExpand" class="expand-btn" @click="expanded = !expanded">
        {{ expanded ? '收起' : '展开' }}
      </span>
    </div>

    <!-- 图片九宫格 -->
    <div v-if="feed.images && feed.images.length" class="feed-images" :class="'grid-' + Math.min(feed.images.length, 9)">
      <div v-for="(img, idx) in feed.images.slice(0, 9)" :key="idx" class="image-cell" @click="previewImage(img)">
        <img :src="img" />
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="feed-actions">
      <div class="action-row">
        <span class="action-btn" @click="toggleLike">
          <i :class="feed.liked ? 'el-icon-star-on liked' : 'el-icon-star-off'"></i>
          <span v-if="feed.likeCount" class="count">{{ feed.likeCount }}</span>
        </span>
        <span class="action-btn" @click="showComment = !showComment">
          <i class="el-icon-chat-line-round"></i>
          <span v-if="feed.commentCount" class="count">{{ feed.commentCount }}</span>
        </span>
      </div>
      <!-- 点赞用户列表 -->
      <div v-if="feed.likeUserNames && feed.likeUserNames.length" class="like-users">
        <i class="el-icon-star-on liked small"></i>
        <span>{{ feed.likeUserNames.slice(0, 3).join('、') }}</span>
        <span v-if="feed.likeCount > 3"> 等{{ feed.likeCount }}人</span>
      </div>
    </div>

    <!-- 评论区 -->
    <div v-if="showComment" class="feed-comments">
      <div v-for="c in feed.comments" :key="c.id" class="comment-item">
        <span class="comment-user">{{ c.nickName }}：</span>
        <span class="comment-content">{{ c.content }}</span>
      </div>
      <div class="comment-input-row">
        <el-input
          v-model="commentText"
          size="small"
          placeholder="评论..."
          @keyup.enter.native="submitComment"
          class="comment-input"
        />
        <el-button size="mini" type="text" :loading="commenting" @click="submitComment">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import HeadImage from "../common/HeadImage.vue";

export default {
  name: "feedItem",
  components: { HeadImage },
  props: {
    feed: { type: Object, required: true }
  },
  data() {
    return {
      expanded: false,
      showComment: false,
      commentText: "",
      commenting: false
    }
  },
  computed: {
    isOwner() {
      return this.feed.userId === this.$store?.userStore?.userInfo?.id
        || this.feed.userId === this.userStore?.userInfo?.id;
    },
    needExpand() {
      return this.feed.content && this.feed.content.length > 140;
    }
  },
  methods: {
    toggleLike() {
      this.$http({ url: "/feed/like", method: "post", params: { feedId: this.feed.id } })
        .then(liked => {
          this.feed.liked = liked;
          this.feed.likeCount += liked ? 1 : -1;
        });
    },
    submitComment() {
      if (!this.commentText.trim()) return;
      this.commenting = true;
      this.$http({
        url: "/feed/comment",
        method: "post",
        data: { feedId: this.feed.id, content: this.commentText.trim() }
      }).then(comment => {
        if (!this.feed.comments) this.$set(this.feed, 'comments', []);
        this.feed.comments.push(comment);
        this.feed.commentCount = (this.feed.commentCount || 0) + 1;
        this.commentText = "";
      }).finally(() => { this.commenting = false; });
    },
    handleCommand(cmd) {
      if (cmd === "delete") {
        this.$confirm("确定删除这条动态吗？", "提示", { type: "warning" })
          .then(() => {
            this.$http({ url: "/feed/" + this.feed.id, method: "delete" })
              .then(() => { this.$emit("deleted", this.feed.id); });
          });
      }
    },
    previewImage(url) {
      this.$eventBus.$emit("openFullImage", url);
    }
  }
}
</script>

<style scoped lang="scss">
.feed-item {
  background: var(--im-card-bg, rgba(255,255,255,0.04));
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}
.feed-header {
  display: flex; align-items: center; margin-bottom: 12px;
  .header-info { margin-left: 10px; flex: 1; }
  .nick-name { display: block; font-size: 14px; font-weight: 600; color: var(--im-color-primary-light-3, #66b1ff); }
  .time { display: block; font-size: 12px; color: var(--im-text-color-secondary, #999); margin-top: 2px; }
  .more-btn { font-size: 18px; color: var(--im-text-color-secondary); cursor: pointer; }
}
.feed-content {
  margin-bottom: 10px;
  p { margin: 0; font-size: 14px; line-height: 1.6; color: var(--im-text-color); white-space: pre-wrap; word-break: break-all; }
  .collapsed { display: -webkit-box; -webkit-line-clamp: 6; -webkit-box-orient: vertical; overflow: hidden; }
  .expand-btn { font-size: 13px; color: var(--im-color-primary); cursor: pointer; }
}
.feed-images {
  display: grid; gap: 4px; margin-bottom: 10px;
  &.grid-1 { grid-template-columns: 1fr; max-width: 280px; }
  &.grid-2 { grid-template-columns: 1fr 1fr; max-width: 320px; }
  &.grid-3, &.grid-4, &.grid-5, &.grid-6, &.grid-7, &.grid-8, &.grid-9 { grid-template-columns: 1fr 1fr 1fr; max-width: 360px; }
  .image-cell {
    aspect-ratio: 1; overflow: hidden; border-radius: 6px; cursor: pointer;
    img { width: 100%; height: 100%; object-fit: cover; }
  }
}
.feed-actions {
  .action-row { display: flex; gap: 20px; }
  .action-btn {
    display: inline-flex; align-items: center; gap: 4px;
    font-size: 13px; color: var(--im-text-color-secondary); cursor: pointer;
    i { font-size: 16px; }
    .liked { color: #ff6b6b; }
    .count { font-size: 12px; }
  }
  .like-users {
    margin-top: 6px; font-size: 12px; color: var(--im-text-color-secondary);
    .small { font-size: 12px; color: #ff6b6b; margin-right: 4px; }
  }
}
.feed-comments {
  margin-top: 10px; padding-top: 10px; border-top: 1px solid var(--im-border-color, rgba(255,255,255,0.06));
  .comment-item { font-size: 13px; line-height: 1.8; color: var(--im-text-color); }
  .comment-user { color: var(--im-color-primary-light-3, #66b1ff); }
  .comment-input-row { display: flex; align-items: center; gap: 8px; margin-top: 8px; }
  .comment-input { flex: 1; }
}
</style>
