<template>
  <div id="app">
    <transition name="fade" mode="out-in">
      <router-view :key="$route.path"></router-view>
    </transition>
  </div>
</template>

<script>
export default {
  name: 'App',
  components: {}
}
</script>

<style lang="scss">
#app {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  position: absolute;
  height: 100%;
  width: 100%;
  color: var(--im-text-color);
  font-family: var(--im-font-family);
}

/* 页面软切换动画 */
.fade-enter-active, .fade-leave-active { transition: opacity .35s ease; }
.fade-enter, .fade-leave-to { opacity: 0; }
</style>
