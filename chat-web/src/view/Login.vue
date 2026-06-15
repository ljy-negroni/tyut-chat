<template>
  <div class="login-view">
    <!-- 左侧品牌面板 -->
    <div class="brand-panel">
      <div class="brand-illustration">
        <svg viewBox="0 0 600 800" xmlns="http://www.w3.org/2000/svg">
          <!-- 社交网络节点与连线 -->
          <g stroke="#A3AAEB" stroke-width="1.2" fill="none" opacity="0.5">
            <line x1="180" y1="200" x2="320" y2="160"/>
            <line x1="180" y1="200" x2="280" y2="380"/>
            <line x1="180" y1="200" x2="80" y2="340"/>
            <line x1="320" y1="160" x2="420" y2="280"/>
            <line x1="320" y1="160" x2="400" y2="100"/>
            <line x1="280" y1="380" x2="420" y2="280"/>
            <line x1="280" y1="380" x2="180" y2="520"/>
            <line x1="80" y1="340" x2="180" y2="520"/>
            <line x1="80" y1="340" x2="120" y2="160"/>
            <line x1="420" y1="280" x2="500" y2="380"/>
            <line x1="180" y1="520" x2="280" y2="620"/>
            <line x1="280" y1="620" x2="400" y2="560"/>
            <line x1="500" y1="380" x2="400" y2="560"/>
          </g>
          <!-- 主要节点 -->
          <g fill="#3748C7" opacity="0.15">
            <circle cx="180" cy="200" r="48"/>
            <circle cx="320" cy="160" r="52"/>
            <circle cx="420" cy="280" r="44"/>
            <circle cx="280" cy="380" r="56"/>
            <circle cx="80" cy="340" r="40"/>
            <circle cx="180" cy="520" r="50"/>
            <circle cx="500" cy="380" r="42"/>
          </g>
          <!-- 节点边线 -->
          <g fill="none" stroke="#3748C7" stroke-width="2" opacity="0.3">
            <circle cx="180" cy="200" r="48"/>
            <circle cx="320" cy="160" r="52"/>
            <circle cx="420" cy="280" r="44"/>
            <circle cx="280" cy="380" r="56"/>
            <circle cx="80" cy="340" r="40"/>
            <circle cx="180" cy="520" r="50"/>
            <circle cx="500" cy="380" r="42"/>
          </g>
          <!-- 小节点 -->
          <g fill="#5B68D3" opacity="0.5">
            <circle cx="120" cy="160" r="5"/>
            <circle cx="400" cy="100" r="4"/>
            <circle cx="280" cy="620" r="5"/>
            <circle cx="400" cy="560" r="4"/>
            <circle cx="250" cy="280" r="3"/>
            <circle cx="160" cy="440" r="3"/>
            <circle cx="340" cy="260" r="4"/>
            <circle cx="460" cy="340" r="3"/>
          </g>
          <!-- 装饰菱形 -->
          <g fill="none" stroke="#7F89DF" stroke-width="1.5" opacity="0.25">
            <rect x="370" y="420" width="28" height="28" rx="3" transform="rotate(45 384 434)"/>
            <rect x="80" y="500" width="20" height="20" rx="2" transform="rotate(45 90 510)"/>
            <rect x="440" y="160" width="22" height="22" rx="2" transform="rotate(45 451 171)"/>
          </g>
        </svg>
      </div>
      <div class="brand-text">
        <h1 class="brand-name">太理朋友圈</h1>
        <p class="brand-desc">TYUT Campus · 即时通讯</p>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="form-panel">
      <div class="form-card">
        <div class="form-logo">
          <svg viewBox="0 0 512 512" width="56" height="56" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="miniGrad" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="#5B68D3"/>
                <stop offset="100%" stop-color="#3748C7"/>
              </linearGradient>
            </defs>
            <rect x="0" y="0" width="512" height="512" rx="112" fill="url(#miniGrad)"/>
            <path d="M155 195C155 165 180 140 210 140L340 140C370 140 390 160 390 190L390 290C390 320 370 340 340 340L270 340L215 390L215 340L210 340C180 340 155 320 155 290Z" fill="rgba(255,255,255,0.18)"/>
            <path d="M125 175C125 148 148 125 175 125L355 125C382 125 400 143 400 170L400 285C400 312 382 330 355 330L255 330L195 390L195 330L175 330C148 330 125 312 125 285Z" fill="white"/>
            <circle cx="195" cy="245" r="18" fill="#3748C7"/>
            <circle cx="255" cy="245" r="18" fill="#5B68D3" opacity="0.65"/>
            <circle cx="315" cy="245" r="18" fill="#7F89DF" opacity="0.35"/>
          </svg>
        </div>
        <div class="form-header">
          <h2>欢迎登录</h2>
          <p>校园即时通讯解决方案</p>
        </div>
        <el-form class="form" :model="loginForm" status-icon :rules="rules" ref="loginForm"
          @keyup.enter.native="submitForm('loginForm')">
          <el-form-item prop="terminal" v-show="false">
            <el-input type="terminal" v-model="loginForm.terminal" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item prop="userName">
            <el-input type="userName" v-model="loginForm.userName" autocomplete="username" placeholder="用户名"
              prefix-icon="el-icon-user"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input type="password" v-model="loginForm.password" autocomplete="current-password"
              placeholder="密码" prefix-icon="el-icon-lock"></el-input>
          </el-form-item>
          <el-form-item class="nav-tool-bar">
            <el-checkbox v-model="isAutoLogin">自动登录</el-checkbox>
          </el-form-item>
          <el-button class="submit-btn" type="primary" :loading="submitting"
            @click="submitForm('loginForm')">登录</el-button>
        </el-form>
        <div class="footer-links">
          <router-link class="link" to="/register">没有账号，前往注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as auth from '../api/auth.js'

export default {
  name: "login",
  components: {},
  data() {
    var checkUsername = (rule, value, callback) => {
      if (!value) return callback(new Error('请输入用户名'));
      callback();
    };
    var checkPassword = (rule, value, callback) => {
      if (value === '') callback(new Error('请输入密码'));
      callback();
    };
    return {
      loginForm: {
        terminal: this.$enums.TERMINAL_TYPE.WEB,
        userName: '',
        password: ''
      },
      isAutoLogin: true,
      submitting: false,
      rules: {
        userName: [{ validator: checkUsername, trigger: 'blur' }],
        password: [{ validator: checkPassword, trigger: 'blur' }]
      }
    };
  },
  methods: {
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (!valid) return;
        this.submitting = true;
        this.$http({
          url: "/login", method: 'post', data: this.loginForm
        }).then((data) => {
          auth.saveLoginSession(data, {
            autoLogin: this.isAutoLogin, userName: this.loginForm.userName
          });
          this.$message.success("登录成功");
          this.$router.push("/home/chat");
        }).finally(() => { this.submitting = false; });
      });
    },
    tryTokenAutoLogin() {
      if (!auth.isAutoLoginEnabled()) return;
      this.submitting = true;
      auth.refreshLogin(this.$http).then(() => {
        this.$router.push("/home/chat");
      }).catch(() => {
        auth.clearLoginSession(true);
        this.isAutoLogin = false;
      }).finally(() => { this.submitting = false; });
    }
  },
  mounted() {
    auth.removeLegacyPassword();
    if (localStorage.getItem("isAutoLogin") != null) {
      this.isAutoLogin = auth.isAutoLoginEnabled();
    }
    this.loginForm.userName = auth.getSavedUsername();
    this.tryTokenAutoLogin();
  }
}
</script>

<style scoped lang="scss">
.login-view {
  display: flex;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ECEDFA 0%, #F8F9FD 50%, #FFFFFF 100%);
  overflow: hidden;

  // === 左侧品牌面板 ===
  .brand-panel {
    flex: 0 0 42%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: hidden;

    .brand-illustration {
      position: absolute;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;

      svg {
        width: 90%;
        max-width: 540px;
        height: auto;
      }
    }

    .brand-text {
      position: relative;
      z-index: 1;
      text-align: center;
      margin-top: auto;
      padding-bottom: 60px;

      .brand-name {
        margin: 0;
        font-size: 32px;
        font-weight: 700;
        color: #3748C7;
        letter-spacing: 2px;
      }

      .brand-desc {
        margin: 8px 0 0;
        font-size: 14px;
        color: #7F89DF;
        letter-spacing: 1px;
      }
    }
  }

  // === 右侧表单面板 ===
  .form-panel {
    flex: 0 0 58%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #FFFFFF;
    position: relative;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 10%;
      bottom: 10%;
      width: 1px;
      background: linear-gradient(180deg, transparent, #E5E5EA 20%, #E5E5EA 80%, transparent);
    }
  }

  .form-card {
    width: 380px;
    padding: 0;
    display: flex;
    flex-direction: column;
    align-items: center;

    .form-logo {
      margin-bottom: 20px;
      filter: drop-shadow(0 4px 12px rgba(55, 72, 199, 0.2));
    }

    .form-header {
      text-align: center;
      margin-bottom: 32px;

      h2 {
        margin: 0 0 6px;
        font-size: 24px;
        font-weight: 700;
        color: #1C1C1E;
      }

      p {
        margin: 0;
        font-size: 14px;
        color: #8E8E93;
      }
    }
  }

  .form {
    width: 100%;

    ::v-deep .el-form-item {
      margin-bottom: 18px;
    }

    ::v-deep .el-input__inner {
      height: 48px;
      border-radius: 12px;
      border: 1px solid #E5E5EA;
      background: #F8F9FD;
      padding-left: 44px;
      font-size: 14px;
      transition: all 0.25s ease;

      &:focus {
        background: #FFFFFF;
        border-color: #5B68D3;
        box-shadow: 0 0 0 3px rgba(55, 72, 199, 0.08);
      }
    }

    ::v-deep .el-input__prefix {
      left: 16px;
      .el-input__icon { color: #A3AAEB; font-size: 18px; }
    }
  }

  .nav-tool-bar {
    ::v-deep .el-form-item__content {
      display: flex;
      justify-content: space-between;
    }
  }

  .submit-btn {
    width: 100%;
    height: 48px;
    margin-top: 4px;
    border-radius: 12px;
    font-size: 15px;
    font-weight: 600;
    letter-spacing: 1px;
  }

  .footer-links {
    margin-top: 20px;
    text-align: center;

    .link {
      text-decoration: none;
      color: #8E8E93;
      font-size: 13px;
      transition: color 0.2s;
      &:hover { color: #3748C7; }
    }
  }
}

// === 响应式 ===
@media (max-width: 860px) {
  .login-view .brand-panel { display: none; }
  .login-view .form-panel {
    flex: 1;
    &::before { display: none; }
  }
}
</style>
