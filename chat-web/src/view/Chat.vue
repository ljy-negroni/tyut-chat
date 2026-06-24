<template>
  <el-container class="chat-page">
    <resizable-aside :default-width="260" :min-width="200" :max-width="500" storage-key="chat-aside-width">
      <div class="header">
        <el-input class="search-text" size="small" placeholder="搜索" v-model="searchText">
          <i class="el-icon-search el-input__icon" slot="prefix"> </i>
        </el-input>
      </div>
      <div class="chat-loading" v-if="loading" v-loading="true" element-loading-text="消息接收中..."
        element-loading-spinner="el-icon-loading" element-loading-background="rgba(28,28,30,0.94)" element-loading-size="24">
      </div>
      <virtual-scroller class="scroll-box" :items="showConversations">
        <template v-slot="{ item }">
          <chat-item :conversation="item" @click.native="onActiveItem(item)" @delete="onDelItem(item)"
            @info="onShowInfo(item)" @dnd="onDnd(item)" @top="onTop(item)" :active="item === activeConv"></chat-item>
        </template>
      </virtual-scroller>
    </resizable-aside>
    <el-container>
      <chat-box v-if="activeConv" :conversation="activeConv"></chat-box>
    </el-container>
  </el-container>
</template>

<script>
import ChatItem from "../components/chat/ChatItem.vue";
import ChatBox from "../components/chat/ChatBox.vue";
import ResizableAside from "../components/common/ResizableAside.vue";
import VirtualScroller from "../components/common/VirtualScroller.vue";

export default {
  name: "chat",
  components: {
    ChatItem,
    ChatBox,
    ResizableAside,
    VirtualScroller
  },
  data() {
    return {
      searchText: "",
      messageContent: "",
      group: {},
      groupMembers: []
    }
  },
  methods: {
    onActiveItem(conv) {
      this.chatStore.setActive(conv.key);
    },
    onDelItem(conv) {
      const tip = `删除后记录将清空,确认删除与'${conv.showName}'的聊天 ?`;
      this.$confirm(tip, '删除聊天', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        if (this.isPrivate(conv) || this.isGroup(conv)) {
          const data = { chatId: conv.targetId }
          const chatTypeText = this.isPrivate(conv) ? "private" : "group";
          await this.$http({
            url: `/message/${chatTypeText}/deleteChat`,
            method: 'delete',
            data: data
          });
        }
        await this.chatStore.remove(conv.key);
      });
    },
    onShowInfo(conv) {
      if (this.isPrivate(conv)) {
        this.$router.push("/home/friend?id=" + conv.targetId);
      } else if (this.isGroup(conv)) {
        if (!this.groupStore.isGroup(conv.targetId)) {
          this.$message.error("您已不在群聊中")
          return;
        }
        this.$router.push("/home/group?id=" + conv.targetId);
      }
    },
    onTop(conv) {
      this.chatStore.setTop(conv.key, !conv.isTop)
    },
    onDnd(conv) {
      if (this.isPrivate(conv)) {
        this.setFriendDnd(conv, conv.targetId, !conv.isDnd)
      } else {
        this.setGroupDnd(conv, conv.targetId, !conv.isDnd)
      }
    },
    setFriendDnd(chat, friendId, isDnd) {
      const formData = {
        friendId: friendId,
        isDnd: isDnd
      }
      this.$http({
        url: '/friend/dnd',
        method: 'put',
        data: formData
      }).then(() => {
        this.friendStore.setDnd(friendId, isDnd)
        this.chatStore.setDnd(chat, isDnd)
      })
    },
    setGroupDnd(chat, groupId, isDnd) {
      const formData = {
        groupId: groupId,
        isDnd: isDnd
      }
      this.$http({
        url: '/group/dnd',
        method: 'put',
        data: formData
      }).then(() => {
        this.groupStore.setDnd(groupId, isDnd)
        this.chatStore.setDnd(chat, isDnd)
      })
    },
    isShow(conv) {
      return !this.searchText || conv.showName.includes(this.searchText)
    },
    isPrivate(conv) {
      return this.$enums.CONVERSATION_TYPE.PRIVATE == conv.type
    },
    isGroup(conv) {
      return this.$enums.CONVERSATION_TYPE.GROUP == conv.type
    },
  },
  computed: {
    activeConv() {
      return this.chatStore.activeConversation;
    },
    loading() {
      return this.chatStore.loading;
    },
    showConversations() {
      return this.chatStore.conversations.filter(conv => this.isShow(conv));
    }
  }
}
</script>

<style lang="scss" scoped>
.chat-page { background: transparent;

  .header {
    height: 50px;
    display: flex;
    align-items: center;
    padding: 0 10px;
  }

  ::v-deep .header .el-input__inner {
    background: rgba(255,255,255,0.06);
    border: 1px solid rgba(255,255,255,0.08);
    border-radius: 8px;
    color: rgba(255,255,255,0.8);
    &::placeholder { color: rgba(255,255,255,0.25); }
    &:focus { border-color: rgba(255,255,255,0.18); }
  }

  .chat-loading {
    height: 48px;
    background: transparent;

    .el-icon-loading {
      font-size: 20px;
      color: rgba(255,255,255,0.3);
    }

    .el-loading-text {
      color: rgba(255,255,255,0.3);
      font-size: 12px;
    }

    ::v-deep .el-loading-mask {
      background: transparent !important;
    }
  }

  .chat-items {
    flex: 1;
  }

}
</style>
