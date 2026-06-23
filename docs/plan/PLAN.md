# 前端 UI 改造计划书

> 创建时间：2026-06-15
> 最后更新：2026-06-23
> 状态：进行中

## 目标

在不动摇现有功能代码的前提下，改造 chat-web 前端的视觉层，采用 **macOS Vibrancy 暗色风格**，统一品牌视觉，消除"AI 生成/Element UI 模板"的观感。

## 原则

- 不动后端，不动业务逻辑，只改样式和静态资源
- 技术栈不动：Vue 2 + Element UI + Pinia + SCSS
- 风格锁定：macOS Vibrancy 暗色毛玻璃，层级分明的深灰面板系统
- 禁止：渐变、大阴影、紫色系、装饰动画

## 已完成

### ✅ 品牌重塑
- 全量替换"太理朋友圈" → **TYUT CHAT**
- 黑底白泡矢量 Logo（`public/logo.svg`）
- 浏览器标签页标题、主题色统一
- Swagger 文档标题同步

### ✅ 登录/注册页 macOS Vibrancy 暗色重设计
- 左品牌右表单分栏布局
- Three.js 暗色粒子背景（黑色底 + 灰白粒子 + 极淡飘浮几何体）
- 右侧暗色玻璃面板：`rgba(28,28,30,0.72)` + `backdrop-blur(60px)`
- 纯色暗灰按钮，无渐变
- 1px 白色半透明边框
- 去掉校徽纹理、3D 文字、紫色主题
- 新增图形验证码（Hutool 生成 + Redis 存储）

### ✅ 聊天界面风格统一
- 对方消息：iMessage 浅灰白泡 `#e9e9eb`，黑字，扁平无阴影
- 自己消息：macOS 蓝 `#0a84ff`，白字
- 在线状态：`#30d158` macOS 经典绿点
- 未读徽标：红色小圆点，≤99 显示数字，>99 显示 99+
- 搜索框：暗色半透明底，1px 白色边框

### ✅ 默认头像
- macOS 暗调色板（8 种深灰微色调）
- 白字，根据用户名 hash 分配色值

## 待完成

### 阶段一：基础完善
1. **重构主题色彩体系**
   - 升级 `themes.scss`，建立完整的 macOS Vibrancy 色彩变量
   - 统一 `--im-*` CSS 变量
   
2. **统一图标体系**
   - 清理 iconfont 冗余图标
   - 统一导航栏图标风格

### 阶段二：细节打磨
3. **主界面框架微调**
   - 导航栏 hover/active 状态
   - 会话列表选中态高亮
   - 滚动条暗色风格
   - 群聊详情页样式统一

4. **空白状态与提示**
   - 空会话引导文案
   - 空好友列表提示
   - 3D 校徽模型（待建模完成后接入）

5. **响应式与可访问性**
   - 移动端适配验证
   - 色彩对比度 WCAG AA 达标确认

## 影响范围

- `chat-web/src/assets/style/themes.scss` —— 主题变量
- `chat-web/src/assets/style/im.scss` —— 全局样式
- `chat-web/src/assets/iconfont/` —— 图标字体
- `chat-web/src/view/Login.vue` —— 登录页 ✅
- `chat-web/src/view/Register.vue` —— 注册页 ✅
- `chat-web/src/view/Home.vue` —— 主框架
- `chat-web/src/view/Chat.vue` —— 聊天页 ✅
- `chat-web/src/view/Friend.vue` —— 好友页 ✅
- `chat-web/src/components/` —— 各组件样式（部分完成）
- `chat-web/public/logo.svg` —— Logo ✅
- `chat-web/public/index.html` —— HTML 模板 ✅

## 风险

- **低风险**：所有改动限于 CSS/SCSS 和静态资源，不触及 JS 逻辑
- 每次改动后 `npm run serve` 验证页面正常显示
