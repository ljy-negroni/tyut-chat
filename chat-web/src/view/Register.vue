<template><div class="root" @mousemove="mv"><div ref="bg" class="bg"></div><div class="ui">
<img :src="emblemUrl" class="em" alt="TYUT" /><h1 class="ti"><span v-for="(c,i) in '创建账号'" :key="i" :style="'--c:'+i">{{c}}</span></h1>
<div class="cd"><el-form :model="f" :rules="rl" ref="rf">
<el-form-item prop="userName" class="li" style="--n:0"><span class="iw"><i class="el-icon-user"></i><el-input v-model="f.userName" autocomplete="username" placeholder="用户名" ></el-input></span></el-form-item>
<el-form-item prop="password" class="li" style="--n:1"><span class="iw"><i class="el-icon-lock"></i><el-input type="password" v-model="f.password" autocomplete="current-password" placeholder="密码" ></el-input></span></el-form-item>

<el-form-item prop="confirmPassword" class="li" style="--n:2"><span class="iw"><i class="el-icon-lock"></i><el-input type="password" v-model="f.confirmPassword" autocomplete="off" placeholder="确认密码" maxlength="20"></el-input></span></el-form-item><div class="li" style="--n:3"><el-button class="bt" type="primary" :loading="ing" @click="register">注册</el-button></div></el-form>
<router-link class="lk" to="/login">已有账号？<b>前往登录 →</b></router-link></div>
<p class="ft">加入太原理工大学校园社区</p></div></div></template>

<script>import{saveLoginSession}from'../api/auth.js';import emblemImg from'@/assets/image/school-emblem.webp';const T3='https://unpkg.com/three@0.160.0/build/three.min.js'
export default{name:'register',data(){return{emblemUrl:emblemImg,mx:0,my:0,f:{userName:'',password:'',confirmPassword:''},ing:!1,rl:{userName:[{required:!0,message:'请输入用户名',trigger:'blur'}],password:[{required:!0,message:'请输入密码',trigger:'blur'}],confirmPassword:[{required:!0,message:'请确认密码',trigger:'blur'},{validator:(r,v,cb)=>v!==this.f.password?cb(new Error('两次密码输入不一致')):cb(),trigger:'blur'}]}}},methods:{mv(e){const r=e.currentTarget.getBoundingClientRect();this.mx=(e.clientX/r.width-.5)*2;this.my=(e.clientY/r.height-.5)*2},register(){this.$refs.rf.validate(o=>{if(!o)return;this.$http({url:'/register',method:'post',data:this.f}).then(()=>this.$http({url:'/login',method:'post',data:{terminal:1,userName:this.f.userName,password:this.f.password}})).then(d=>{saveLoginSession(d,{autoLogin:!0,userName:this.f.userName});this.$message.success('注册成功');this.$router.push('/home/chat')})})},
init3(th){const el=this.$refs.bg,W=el.clientWidth,H=el.clientHeight,R=new th.WebGLRenderer({antialias:!0,alpha:!0});R.setSize(W,H);R.setPixelRatio(Math.min(devicePixelRatio,2));R.toneMapping=th.ACESFilmicToneMapping;R.toneMappingExposure=1.1;el.appendChild(R.domElement)
const S=new th.Scene();S.background=new th.Color('#ebeef7');S.fog=new th.Fog('#ebeef7',32,130)
const C=new th.PerspectiveCamera(44,W/H,.5,200);C.position.set(0,2,26);C.lookAt(0,0,0)
S.add(new th.AmbientLight('#d8ddf2',2.6));const dl=new th.DirectionalLight('#fff',3);dl.position.set(6,16,8);S.add(dl);S.add(new th.DirectionalLight('#9999dd',1.4).translateX(-5).translateY(2).translateZ(-3))
// grid — 光场地面
const gc=document.createElement('canvas');gc.width=gc.height=512;const gx=gc.getContext('2d');gx.strokeStyle='rgba(56,32,200,.08)';gx.lineWidth=1
for(let i=0;i<=512;i+=34){gx.beginPath();gx.moveTo(i,0);gx.lineTo(i,512);gx.stroke();gx.beginPath();gx.moveTo(0,i);gx.lineTo(512,i);gx.stroke()}
const gt=new th.CanvasTexture(gc);gt.wrapS=gt.wrapT=th.RepeatWrapping;gt.repeat.set(6,6)
const fl=new th.Mesh(new th.PlaneGeometry(110,110),new th.MeshBasicMaterial({map:gt,transparent:!0,opacity:.42,side:th.DoubleSide,depthWrite:!1}));fl.rotation.x=-Math.PI/2;fl.position.y=-6;S.add(fl)
// aurora 光带
const AU=new th.Group();S.add(AU)
const ac=['#5846E8','#7668F2','#4432D0','#6858EC','#3624C0']
for(let i=0;i<5;i++){const vs='varying vec2 vUv;void main(){vUv=uv;gl_Position=projectionMatrix*modelViewMatrix*vec4(position,1.);}';const fs='uniform float t;uniform vec3 c;uniform float ix;varying vec2 vUv;void main(){float d=abs(vUv.y-.5)*2.;float w=sin(vUv.x*12.+t*.42+ix)*.34+cos(vUv.x*7.-t*.26+ix*2.)*.26+sin(vUv.x*20.-t*.32+ix)*.18;float a=smoothstep(.94,0.,d)*(.15+w*.1)*.5;gl_FragColor=vec4(c,a);}';const p=new th.Mesh(new th.PlaneGeometry(52,18),new th.ShaderMaterial({uniforms:{t:{value:0},c:{value:new th.Color(ac[i])},ix:{value:i}},vertexShader:vs,fragmentShader:fs,transparent:!0,depthWrite:!1,side:th.DoubleSide}));p.position.set((i-2)*9,-.6+i*.4,-5-i*6);p.rotation.x=-.25;AU.add(p)}
// 轨道环
const RN=[];[2.2,3.5,5.].forEach((r,i)=>{const t=new th.Mesh(new th.TorusGeometry(r,.011,16,150),new th.MeshBasicMaterial({color:new th.Color().setHSL(.67,.5,.43+i*.13),transparent:!0,opacity:.25-i*.06,side:th.DoubleSide,depthWrite:!1}));t.rotation.x=Math.PI/2+(i-1)*.08;t.userData={s:.05+i*.022};S.add(t);RN.push(t)})
// 粒子场
const PR=[]
for(let ri=0;ri<4;ri++){const pg=new th.BufferGeometry();const cnt=100+ri*28;const ps=new Float32Array(cnt*3),cs=new Float32Array(cnt*3);const r=4.4+ri*3.
for(let i=0;i<cnt;i++){const a=(i/cnt)*Math.PI*2,y=(Math.random()-.5)*6;ps[i*3]=Math.cos(a)*(r+(Math.random()-.5)*2.5);ps[i*3+1]=y;ps[i*3+2]=Math.sin(a)*(r+(Math.random()-.5)*2.5);const cc=new th.Color().setHSL(.67,.55,.43+Math.random()*.3);cs[i*3]=cc.r;cs[i*3+1]=cc.g;cs[i*3+2]=cc.b}
pg.setAttribute('position',new th.BufferAttribute(ps,3));pg.setAttribute('color',new th.BufferAttribute(cs,3));const pm=new th.Points(pg,new th.PointsMaterial({size:.06,vertexColors:!0,transparent:!0,opacity:.22,blending:th.AdditiveBlending,depthWrite:!1}));pm.userData={s:.03+ri*.01};S.add(pm);PR.push(pm)}
// 浮游体
const FT=[]
for(let i=0;i<9;i++){const gs=[new th.IcosahedronGeometry(.1,1),new th.OctahedronGeometry(.08,0),new th.TetrahedronGeometry(.09,0),new th.TorusKnotGeometry(.07,.025,36,6)];const m=new th.Mesh(gs[i%4],new th.MeshStandardMaterial({color:new th.Color().setHSL(.66,.45,.37+i*.06),metalness:.4,roughness:.62,side:th.DoubleSide}));const ang=(i/9)*Math.PI*2,dist=7.+i*.5;m.position.set(Math.cos(ang)*dist,(Math.random()-.5)*7,Math.sin(ang)*dist);m.userData={bx:m.position.x,bz:m.position.z,by:m.position.y,ph:i,dist};S.add(m);FT.push(m)}
// 中轴 + 辉光
S.add(new th.Mesh(new th.CylinderGeometry(.02,.02,42,8),new th.MeshBasicMaterial({color:'#6040e0',transparent:!0,opacity:.045,depthWrite:!1})))
const gl=new th.Mesh(new th.RingGeometry(.9,2.8,64),new th.MeshBasicMaterial({color:'#7050e0',side:th.DoubleSide,transparent:!0,opacity:.045,depthWrite:!1}));gl.rotation.x=-Math.PI/2;gl.position.y=.06;S.add(gl)
// animate
const ck=new th.Clock(),af=()=>{if(!this._alive)return;requestAnimationFrame(af);const t=ck.getElapsedTime(),mx=this.mx*.5,my=this.my*.5;AU.children.forEach(p=>{p.material.uniforms.t.value=t});RN.forEach(r=>{r.rotation.z+=r.userData.s*.2});PR.forEach(p=>{p.rotation.y+=p.userData.s});FT.forEach(f=>{f.position.x=f.userData.bx+Math.sin(t*.3+f.userData.ph)*2.2;f.position.z=f.userData.bz+Math.cos(t*.27+f.userData.ph)*2.2;f.position.y=f.userData.by+Math.sin(t*.5+f.userData.ph*1.2)*1.6;f.rotation.x+=.004;f.rotation.y+=.005});gl.scale.setScalar(1+Math.sin(t*.7)*.22);C.position.x+=(mx*5-C.position.x)*.025;C.position.y+=(2+my*2.2-C.position.y)*.025;C.lookAt(0,0,0);R.render(S,C)};this._alive=!0;af();const rs=()=>{const w=el.clientWidth,h=el.clientHeight;R.setSize(w,h);C.aspect=w/h;C.updateProjectionMatrix()};window.addEventListener('resize',rs);this._clean=()=>{this._alive=!1;window.removeEventListener('resize',rs);R.dispose()}}},mounted(){const s=document.createElement('script');s.src=T3;s.onload=()=>{this.init3(window.THREE)};document.head.appendChild(s)},beforeDestroy(){if(this._clean)this._clean();this._alive=!1}}</script>

<style scoped lang="scss">

/* ===== 布局节奏 ===== */
.root{width:100%;height:100%;position:relative;overflow:hidden}
.bg{position:absolute;inset:0;z-index:0}
.ui{position:relative;z-index:2;display:flex;flex-direction:column;align-items:center;justify-content:center;height:100%;gap:24px;animation:in .75s cubic-bezier(.22,.61,.36,1) both}
@keyframes in{0%{opacity:0}100%{opacity:1}}

/* ===== 校徽 ===== */
.em{width:120px;height:120px;border-radius:50%;object-fit:contain;filter:drop-shadow(0 4px 28px rgba(48,0,240,.12));animation:ei .7s cubic-bezier(.22,.61,.36,1) both}
@keyframes ei{0%{opacity:0;translate:0 16px}100%{opacity:1;translate:0 0}}

/* ===== 标题 ===== */
.ti{font-size:44px;font-weight:800;letter-spacing:10px;span{display:inline-block;background:linear-gradient(135deg,#2E1CB8,#5848E0,#2E1CB8);background-size:200% auto;-webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text;animation:tc .42s calc(.32s + var(--c)*.045s) cubic-bezier(.22,.61,.36,1) both,tw 3s ease-in-out infinite}}
@keyframes tc{0%{opacity:0;translate:0 10px}100%{opacity:1;translate:0 0}}
@keyframes tw{0%,100%{background-position:0% center}50%{background-position:100% center}}

/* ===== 登录卡 ===== */
.cd{width:450px;background:rgba(96,60,230,.03);backdrop-filter:blur(50px);-webkit-backdrop-filter:blur(50px);border-radius:26px;padding:38px 42px 24px;border:1px solid rgba(96,48,240,.06);box-shadow:0 0 48px rgba(96,48,240,.025),inset 0 1px 0 rgba(255,255,255,.15);animation:ci .55s .2s cubic-bezier(.22,.61,.36,1) both;transition:transform .4s cubic-bezier(.4,0,.2,1),box-shadow .4s ease,background .4s ease;&:hover{transform:translateY(-4px);box-shadow:0 16px 52px rgba(96,48,240,.06),inset 0 1px 0 rgba(255,255,255,.2);background:rgba(96,60,230,.05)}}
@keyframes ci{0%{opacity:0;translate:0 14px}100%{opacity:1;translate:0 0}}

.li{animation:ri .38s calc(.28s + var(--n)*.065s) cubic-bezier(.22,.61,.36,1) both}
@keyframes ri{0%{opacity:0;translate:0 10px}100%{opacity:1;translate:0 0}}

/* ===== 输入框 ===== */
::v-deep .el-input__inner{height:54px;border-radius:16px;border:1px solid rgba(96,48,240,.08);background:rgba(96,60,230,.03);backdrop-filter:blur(20px);-webkit-backdrop-filter:blur(20px);padding-left:48px;font-size:15px;color:rgba(30,20,50,.85);transition:all .35s cubic-bezier(.4,0,.2,1);&::placeholder{color:rgba(96,48,240,.2)}&:focus{background:rgba(96,60,230,.06);border-color:rgba(96,48,240,.2);box-shadow:0 0 32px rgba(96,48,240,.05),0 4px 14px rgba(96,48,240,.02),inset 0 1px 0 rgba(255,255,255,.1)}}
/* ===== 图标容器 ===== */
.iw{position:relative;display:block}.iw .el-icon-user,.iw .el-icon-lock{position:absolute;left:12px;top:50%;transform:translateY(-50%);z-index:2;color:rgba(96,48,240,.25);font-size:15px;pointer-events:none;width:16px;text-align:center}
.iw .el-input__inner{padding-left:48px !important}
::v-deep .el-form-item{margin-bottom:16px}

/* ===== 按钮 ===== */
.bt{width:100%;height:54px;margin-top:4px;border-radius:16px;font-size:16px;font-weight:600;letter-spacing:6px;border:none;position:relative;overflow:hidden;background:transparent;transition:all .4s cubic-bezier(.4,0,.2,1);&::before{content:'';position:absolute;inset:0;background:linear-gradient(135deg,#5B48E8,#3824D0);z-index:0;opacity:.92;transition:all .4s}&::after{content:'';position:absolute;inset:0;z-index:1;background:linear-gradient(90deg,transparent,rgba(255,255,255,.32),transparent);transform:translateX(-100%)}&:hover{translate:0 -3px;box-shadow:0 14px 34px rgba(56,36,208,.22),0 0 56px rgba(56,36,208,.06);&::before{opacity:1}&::after{transform:translateX(100%);transition:transform .7s ease}}&:active{translate:0 0}::v-deep span{position:relative;z-index:2}}

/* ===== 链接 ===== */
.lk{display:block;text-align:center;text-decoration:none;font-size:14px;color:rgba(96,48,240,.28);transition:all .25s;b{font-weight:500}&:hover{color:rgba(58,36,208,.5);b{letter-spacing:1px}}}

/* ===== 底部 ===== */
.ft{font-size:12px;letter-spacing:5px;color:rgba(96,48,240,.11);animation:fti 1s .65s cubic-bezier(.22,.61,.36,1) both}
@keyframes fti{0%{opacity:0}100%{opacity:1}}
@media(max-width:500px){.cd{width:92vw}.ti{font-size:28px}}

</style>
