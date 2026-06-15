# 前端 UI 改造计划书

> 创建时间：2026-06-15
> 状态：待审查

## 目标

在不动摇现有功能代码的前提下，改造 chat-web 前端的视觉层，使其具有品牌辨识度和设计感，消除"AI 生成/Element UI 模板"的观感。

## 原则

- 不动后端，不动业务逻辑，只改样式和静态资源
- 技术栈不动：Vue 2 + Element UI + Pinia + SCSS
- 不引入新依赖（除 SVG 插画自制外）

## 任务拆解

### 阶段一：品牌视觉基础（1-3 个 commit）

1. **设计 SVG 矢量 Logo**
   - 创作一个几何化风格的"太理朋友圈"品牌 logo
   - 替换 `public/logo.png`，同时提供 favicon
   - commit: "设计品牌 Logo：太理朋友圈矢量图标"

2. **重构主题色彩体系**
   - 升级 `thems.scss`，增加完整的色彩变量
   - 设计一套有辨识度的配色方案（非模板蓝）
   - 增加暗色模式兼容变量
   - commit: "重构主题色彩体系"

3. **统一图标体系**
   - 补充 iconfont 缺失的图标
   - 统一导航栏图标风格
   - commit: "统一前端图标体系"

### 阶段二：页面视觉升级（2-3 个 commit）

4. **登录/注册页视觉重设计**
   - 用 SVG 几何插画替换纯 CSS 渐变背景
   - 增加品牌 LOGO 展示区域
   - 优化卡片玻璃质感
   - commit: "升级登录注册页视觉设计"

5. **主界面框架视觉升级**
   - 导航栏渐变优化
   - 会话列表微交互
   - 聊天消息气泡样式优化
   - 空白状态插画
   - commit: "升级主界面框架视觉"

6. **细节打磨**
   - `<html lang="zh-CN">` 修复
   - meta 描述补充
   - 滚动条、阴影等细节统一
   - commit: "修复 HTML 元信息及 UI 细节"

## 影响范围

- `chat-web/src/assets/style/thems.scss` —— 主题变量
- `chat-web/src/assets/style/im.scss` —— 全局样式
- `chat-web/src/assets/iconfont/` —— 图标字体
- `chat-web/src/view/Login.vue` —— 登录页
- `chat-web/src/view/Register.vue` —— 注册页
- `chat-web/src/view/Home.vue` —— 主框架
- `chat-web/src/components/` —— 各组件样式
- `chat-web/public/logo.png` → `logo.svg` —— Logo
- `chat-web/public/index.html` —— HTML 模板

## 风险

- **低风险**：所有改动限于 CSS/SCSS 和静态资源，不触及 JS 逻辑
- 每次改动后 `npm run serve` 验证页面正常显示
- 每次改动后 `npm run build` 确认编译通过

## 审查点

请确认：
1. 任务拆解是否合理？
2. 色彩方案偏好？（偏暖/偏冷/中性？）
3. Logo 风格偏好？（简约几何/线条/扁平？）
4. 是否可以开始执行？
