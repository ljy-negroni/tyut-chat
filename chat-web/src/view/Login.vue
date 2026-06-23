<template>
<div class="root" @mousemove="mv">
  <div ref="bg" class="bg"></div>
  <!-- 粒子画布遮罩：底部微透光 -->
  <div class="vignette"></div>

  <!-- 左侧品牌区 -->
  <div class="brand">
    <div class="brand-logo">TYUT CHAT</div>
    <div class="brand-tagline">太原理工大学 · 校园社区</div>
    <div class="brand-desc">连接校园每一刻</div>
  </div>

  <!-- 右侧登录面板 -->
  <div class="panel-area">
    <div class="panel">
      <div class="panel-title">登录</div>
      <el-form :model="f" ref="rf" @keyup.enter.native="login">
        <el-form-item prop="terminal" v-show="false">
          <el-input v-model="f.terminal"></el-input>
        </el-form-item>
        <el-form-item prop="userName" class="fld">
          <el-input v-model="f.userName" autocomplete="username" placeholder="用户名" @input="err.userName=''">
            <i slot="prefix" class="el-icon-user"></i>
          </el-input>
          <div class="fe" v-if="err.userName">{{err.userName}}</div>
        </el-form-item>
        <el-form-item prop="password" class="fld">
          <el-input type="password" v-model="f.password" autocomplete="current-password" placeholder="密码" @input="err.password=''">
            <i slot="prefix" class="el-icon-lock"></i>
          </el-input>
          <div class="fe" v-if="err.password">{{err.password}}</div>
        </el-form-item>
        <el-form-item prop="captchaCode" class="fld">
          <div class="captcha-row">
            <el-input v-model="f.captchaCode" autocomplete="off" placeholder="验证码" maxlength="4" @input="err.captchaCode=''">
              <i slot="prefix" class="el-icon-key"></i>
            </el-input>
            <img :src="captchaImage" @click="getCaptcha" class="captcha-img" title="点击刷新" />
          </div>
          <div class="fe" v-if="err.captchaCode">{{err.captchaCode}}</div>
        </el-form-item>
        <div class="chk-row">
          <el-checkbox v-model="auto">自动登录</el-checkbox>
        </div>
        <el-button class="submit-btn" :loading="ing" @click="login">登 录</el-button>
      </el-form>
      <router-link class="switch-link" to="/register">没有账号？<b>前往注册 →</b></router-link>
    </div>
  </div>
</div>
</template>

<script>
import * as auth from '../api/auth.js';
const T3 = 'https://unpkg.com/three@0.160.0/build/three.min.js';

export default {
  name: 'login',
  data() {
    return {
      mx: 0, my: 0,
      captchaImage: '',
      f: { terminal: 1, userName: '', password: '', captchaKey: '', captchaCode: '' },
      auto: true,
      ing: false,
      err: { userName: '', password: '', captchaCode: '' }
    };
  },
  methods: {
    mv(e) {
      const r = e.currentTarget.getBoundingClientRect();
      this.mx = (e.clientX / r.width - 0.5) * 2;
      this.my = (e.clientY / r.height - 0.5) * 2;
    },
    getCaptcha() {
      this.captchaImage = '';
      this.$http({ url: '/captcha', method: 'get' }).then(d => {
        this.f.captchaKey = d.captchaKey;
        this.captchaImage = d.captchaImage;
      }).catch(e => { console.error('获取验证码失败', e); });
    },
    login() {
      let ok = true;
      if (!this.f.userName) { this.err.userName = '请输入用户名'; ok = false; } else { this.err.userName = ''; }
      if (!this.f.password) { this.err.password = '请输入密码'; ok = false; } else { this.err.password = ''; }
      if (!this.f.captchaCode) { this.err.captchaCode = '请输入验证码'; ok = false; } else { this.err.captchaCode = ''; }
      if (!ok) return;
      this.ing = true;
      this.$http({ url: '/login', method: 'post', data: this.f }).then(d => {
        auth.saveLoginSession(d, { autoLogin: this.auto, userName: this.f.userName });
        this.$message.success('登录成功');
        this.$router.push('/home/chat');
      }).finally(() => { this.ing = false; });
    },
    tryAuto() {
      if (!auth.isAutoLoginEnabled()) return;
      this.ing = true;
      auth.refreshLogin(this.$http).then(() => this.$router.push('/home/chat'))
        .catch(() => { auth.clearLoginSession(true); this.auto = false; })
        .finally(() => { this.ing = false; });
    },
    // ========== Three.js 粒子背景 ==========
    init3(th) {
      const el = this.$refs.bg;
      const W = el.clientWidth, H = el.clientHeight;
      const R = new th.WebGLRenderer({ antialias: true, alpha: true });
      R.setSize(W, H);
      R.setPixelRatio(Math.min(devicePixelRatio, 2));
      R.toneMapping = th.ACESFilmicToneMapping;
      R.toneMappingExposure = 1.0;
      el.appendChild(R.domElement);

      const S = new th.Scene();
      S.background = new th.Color('#0a0a0c');
      S.fog = new th.Fog('#0a0a0c', 28, 110);

      const C = new th.PerspectiveCamera(44, W / H, 0.5, 200);
      C.position.set(0, 2, 26);
      C.lookAt(0, 0, 0);

      // 极为克制的环境光
      S.add(new th.AmbientLight('#333344', 1.6));
      const dl = new th.DirectionalLight('#ffffff', 1.4);
      dl.position.set(6, 16, 8);
      S.add(dl);

      // === 浮动几何体（保留原有多面体飘浮） ===
      const FT = [];
      const geoSet = [
        new th.IcosahedronGeometry(0.1, 1),
        new th.OctahedronGeometry(0.08, 0),
        new th.TetrahedronGeometry(0.09, 0),
        new th.TorusKnotGeometry(0.07, 0.025, 36, 6)
      ];
      for (let i = 0; i < 12; i++) {
        const m = new th.Mesh(geoSet[i % 4],
          new th.MeshStandardMaterial({
            color: new th.Color().setHSL(0.6, 0.08, 0.22 + i * 0.03),
            metalness: 0.3,
            roughness: 0.7,
            side: th.DoubleSide
          }));
        const ang = (i / 12) * Math.PI * 2;
        const dist = 7 + i * 0.5;
        m.position.set(Math.cos(ang) * dist, (Math.random() - 0.5) * 7, Math.sin(ang) * dist);
        m.userData = { bx: m.position.x, bz: m.position.z, by: m.position.y, ph: i, dist };
        S.add(m);
        FT.push(m);
      }

      // === 粒子环 ===
      const PR = [];
      for (let ri = 0; ri < 4; ri++) {
        const cnt = 100 + ri * 28;
        const ps = new Float32Array(cnt * 3), cs = new Float32Array(cnt * 3);
        const r = 4.4 + ri * 3;
        for (let i = 0; i < cnt; i++) {
          const a = (i / cnt) * Math.PI * 2;
          const y = (Math.random() - 0.5) * 6;
          ps[i * 3] = Math.cos(a) * (r + (Math.random() - 0.5) * 2.5);
          ps[i * 3 + 1] = y;
          ps[i * 3 + 2] = Math.sin(a) * (r + (Math.random() - 0.5) * 2.5);
          const cc = new th.Color().setHSL(0.6, 0.04, 0.35 + Math.random() * 0.25);
          cs[i * 3] = cc.r; cs[i * 3 + 1] = cc.g; cs[i * 3 + 2] = cc.b;
        }
        const pg = new th.BufferGeometry();
        pg.setAttribute('position', new th.BufferAttribute(ps, 3));
        pg.setAttribute('color', new th.BufferAttribute(cs, 3));
        const pm = new th.Points(pg, new th.PointsMaterial({
          size: 0.06, vertexColors: true,
          transparent: true, opacity: 0.18,
          blending: th.AdditiveBlending, depthWrite: false
        }));
        pm.userData = { s: 0.03 + ri * 0.01 };
        S.add(pm);
        PR.push(pm);
      }

      // === 极光丝带（改为极淡白/灰） ===
      const AU = new th.Group();
      S.add(AU);
      const ac = ['#3a3a44', '#4a4a55', '#353544', '#454555', '#3d3d48'];
      for (let i = 0; i < 5; i++) {
        const vs = 'varying vec2 vUv;void main(){vUv=uv;gl_Position=projectionMatrix*modelViewMatrix*vec4(position,1.);}';
        const fs = 'uniform float t;uniform vec3 c;uniform float ix;varying vec2 vUv;void main(){float d=abs(vUv.y-.5)*2.;float w=sin(vUv.x*12.+t*.42+ix)*.34+cos(vUv.x*7.-t*.26+ix*2.)*.26+sin(vUv.x*20.-t*.32+ix)*.18;float a=smoothstep(.94,0.,d)*(.12+w*.08)*.35;gl_FragColor=vec4(c,a);}';
        const p = new th.Mesh(new th.PlaneGeometry(52, 18),
          new th.ShaderMaterial({
            uniforms: { t: { value: 0 }, c: { value: new th.Color(ac[i]) }, ix: { value: i } },
            vertexShader: vs, fragmentShader: fs,
            transparent: true, depthWrite: false, side: th.DoubleSide
          }));
        p.position.set((i - 2) * 9, -0.6 + i * 0.4, -5 - i * 6);
        p.rotation.x = -0.25;
        AU.add(p);
      }

      // === 极淡光环 ===
      const RN = [];
      [2.2, 3.5, 5.0].forEach((r, i) => {
        const t = new th.Mesh(new th.TorusGeometry(r, 0.011, 16, 150),
          new th.MeshBasicMaterial({
            color: new th.Color().setHSL(0.6, 0.05, 0.25 + i * 0.08),
            transparent: true, opacity: 0.15 - i * 0.04,
            side: th.DoubleSide, depthWrite: false
          }));
        t.rotation.x = Math.PI / 2 + (i - 1) * 0.08;
        t.userData = { s: 0.05 + i * 0.022 };
        S.add(t);
        RN.push(t);
      });

      // === 动画循环 ===
      const ck = new th.Clock();
      const af = () => {
        if (!this._alive) return;
        requestAnimationFrame(af);
        const t = ck.getElapsedTime();
        AU.children.forEach(p => { p.material.uniforms.t.value = t; });
        RN.forEach(r => { r.rotation.z += r.userData.s * 0.2; });
        PR.forEach(p => { p.rotation.y += p.userData.s; });
        FT.forEach(f => {
          f.position.x = f.userData.bx + Math.sin(t * 0.3 + f.userData.ph) * 1.6;
          f.position.z = f.userData.bz + Math.cos(t * 0.27 + f.userData.ph) * 1.6;
          f.position.y = f.userData.by + Math.sin(t * 0.5 + f.userData.ph * 1.2) * 1.2;
          f.rotation.x += 0.003;
          f.rotation.y += 0.004;
        });
        C.position.x += (this.mx * 1.5 - C.position.x) * 0.02;
        C.position.y += (-this.my * 0.8 + 2 - C.position.y) * 0.02;
        C.lookAt(0, 0, 0);
        R.render(S, C);
      };
      this._alive = true;
      af();

      const rs = () => {
        const w = el.clientWidth, h = el.clientHeight;
        R.setSize(w, h);
        C.aspect = w / h;
        C.updateProjectionMatrix();
      };
      window.addEventListener('resize', rs);
      this._clean = () => {
        this._alive = false;
        window.removeEventListener('resize', rs);
        R.dispose();
      };
    }
  },
  mounted() {
    this.getCaptcha();
    auth.removeLegacyPassword();
    if (localStorage.getItem('isAutoLogin') != null) this.auto = auth.isAutoLoginEnabled();
    const s = document.createElement('script');
    s.src = T3;
    s.onload = () => { this.init3(window.THREE); };
    document.head.appendChild(s);
    this.tryAuto();
  },
  beforeDestroy() {
    if (this._clean) this._clean();
    this._alive = false;
  }
};
</script>

<style scoped lang="scss">
// ====== 基础布局 ======
.root {
  width: 100%; height: 100%;
  position: relative;
  overflow: hidden;
  background: #0a0a0c;
  display: flex;
  align-items: center;
  justify-content: center;
}
.bg {
  position: absolute; inset: 0; z-index: 0;
}
.vignette {
  position: absolute; inset: 0; z-index: 1;
  background: radial-gradient(ellipse at 30% 50%, transparent 40%, rgba(0,0,0,0.5) 100%);
  pointer-events: none;
}

// ====== 左侧品牌区 ======
.brand {
  position: relative; z-index: 2;
  flex: 0 0 50%;
  display: flex; flex-direction: column;
  align-items: flex-start;
  padding-left: 8vw;
  animation: fadeIn 1s cubic-bezier(.22,.61,.36,1) both;
}
.brand-logo {
  font-family: "Georgia", "Noto Serif SC", "Times New Roman", serif;
  font-size: clamp(36px, 5vw, 64px);
  font-weight: 700;
  color: rgba(255,255,255,0.92);
  letter-spacing: 0.04em;
  line-height: 1.1;
  margin-bottom: 16px;
  user-select: none;
}
.brand-tagline {
  font-family: "SF Pro Display", "PingFang SC", "Microsoft YaHei", sans-serif;
  font-size: clamp(14px, 1.5vw, 18px);
  color: rgba(255,255,255,0.45);
  letter-spacing: 0.08em;
  margin-bottom: 8px;
  user-select: none;
}
.brand-desc {
  font-family: "SF Pro Display", "PingFang SC", "Microsoft YaHei", sans-serif;
  font-size: clamp(12px, 1.2vw, 14px);
  color: rgba(255,255,255,0.28);
  letter-spacing: 0.12em;
  user-select: none;
}

// ====== 右侧面板区 ======
.panel-area {
  position: relative; z-index: 2;
  flex: 0 0 50%;
  display: flex; align-items: center; justify-content: center;
  padding-right: 4vw;
}
.panel {
  width: 400px; max-width: 90vw;
  background: rgba(28,28,30,0.72);
  backdrop-filter: blur(60px);
  -webkit-backdrop-filter: blur(60px);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 16px;
  padding: 40px 36px 28px;
  animation: panelIn 0.6s 0.15s cubic-bezier(.22,.61,.36,1) both;
  transition: background 0.3s ease;
  &:hover {
    background: rgba(28,28,30,0.78);
  }
}
.panel-title {
  font-family: "Georgia", "Noto Serif SC", serif;
  font-size: 22px;
  font-weight: 600;
  color: rgba(255,255,255,0.9);
  margin-bottom: 28px;
  letter-spacing: 0.04em;
}

// ====== 表单元素 ======
.fld {
  margin-bottom: 18px;
  animation: rowIn 0.4s calc(0.3s + var(--n, 0) * 0.06s) cubic-bezier(.22,.61,.36,1) both;
}

// 输入框深度覆盖
::v-deep .fld .el-input__inner {
  height: 48px;
  background: rgba(28,28,30,0.6);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 10px;
  color: rgba(255,255,255,0.88);
  font-size: 14px;
  padding-left: 40px;
  transition: border-color 0.2s ease, background 0.2s ease;
  &::placeholder {
    color: rgba(255,255,255,0.25);
    font-weight: 400;
  }
  &:focus {
    background: rgba(44,44,46,0.6);
    border-color: rgba(255,255,255,0.22);
    outline: none;
    box-shadow: none;
  }
}
::v-deep .fld .el-input__prefix {
  left: 12px;
  color: rgba(255,255,255,0.35);
  font-size: 14px;
  display: flex; align-items: center;
}

// 验证码行
.captcha-row {
  display: flex; gap: 10px; align-items: center;
  .el-input { flex: 1; }
}
.captcha-img {
  height: 48px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid rgba(255,255,255,0.1);
  transition: border-color 0.2s ease;
  &:hover { border-color: rgba(255,255,255,0.25); }
}

// 复选框
.chk-row {
  margin-bottom: 20px;
}
::v-deep .chk-row .el-checkbox__inner {
  border-radius: 4px;
  border-color: rgba(255,255,255,0.18);
  background: rgba(255,255,255,0.04);
  transition: all 0.2s ease;
}
::v-deep .chk-row .el-checkbox__input.is-checked .el-checkbox__inner {
  background: rgba(255,255,255,0.7);
  border-color: rgba(255,255,255,0.5);
}
::v-deep .chk-row .el-checkbox__label {
  color: rgba(255,255,255,0.5);
  font-size: 13px;
  font-weight: 500;
}

// 提交按钮（macOS Vibrancy 纯色按钮）
.submit-btn {
  width: 100%; height: 48px;
  margin-top: 4px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.3em;
  border: none;
  background: #3a3a3c;
  color: rgba(255,255,255,0.9);
  transition: background 0.2s ease;
  &:hover {
    background: #4a4a4c;
  }
  &:active {
    background: #2c2c2e;
  }
}

// 切换链接
.switch-link {
  display: block; text-align: center; text-decoration: none;
  font-size: 13px; font-weight: 400;
  color: rgba(255,255,255,0.35);
  margin-top: 20px;
  transition: color 0.2s;
  b { font-weight: 600; }
  &:hover {
    color: rgba(255,255,255,0.6);
    b { letter-spacing: 1px; }
    transition: all 0.2s;
  }
}

// 错误提示
.fe {
  color: #e07068;
  font-size: 12px;
  line-height: 1.4;
  padding: 6px 0 0 4px;
  text-align: left;
  font-weight: 500;
  animation: errIn 0.25s ease both;
}

// ====== 动画 ======
@keyframes fadeIn {
  0% { opacity: 0; }
  100% { opacity: 1; }
}
@keyframes panelIn {
  0% { opacity: 0; transform: translateX(20px); }
  100% { opacity: 1; transform: translateX(0); }
}
@keyframes rowIn {
  0% { opacity: 0; transform: translateY(8px); }
  100% { opacity: 1; transform: translateY(0); }
}
@keyframes errIn {
  0% { opacity: 0; transform: translateY(-4px); }
  100% { opacity: 1; transform: translateY(0); }
}

// ====== 响应式 ======
@media (max-width: 768px) {
  .root {
    flex-direction: column;
  }
  .brand {
    flex: 0 0 auto;
    align-items: center;
    padding-left: 0;
    padding-top: 8vh;
    margin-bottom: 32px;
  }
  .brand-logo {
    font-size: 32px;
    text-align: center;
  }
  .brand-tagline { text-align: center; }
  .brand-desc { text-align: center; }
  .panel-area {
    flex: 1 1 auto;
    padding-right: 0;
    align-items: flex-start;
  }
  .panel {
    width: 92vw;
    padding: 28px 24px 20px;
  }
}
</style>
