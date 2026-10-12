<template>
  <div class="login">
    <div class="login-box">
      <!-- removed inline img to use it as full-page background -->
      <div class="login-form">
        <el-form ref="loginForm" :model="loginForm" :rules="loginRules">
          <div class="login-form-title">
            <span class="brand-icon">SC</span>
            <div><strong>AI 智慧供应链</strong><small>AI SUPPLY CHAIN PLATFORM</small></div>
          </div>
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              type="text"
              auto-complete="off"
              placeholder="账号"
              prefix-icon="iconfont icon-user"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              prefix-icon="iconfont icon-lock"
              @keyup.enter.native="handleLogin"
            />
          </el-form-item>
          <el-form-item style="width: 100%">
            <el-button
              :loading="loading"
              class="login-btn"
              size="medium"
              type="primary"
              style="width: 100%"
              @click.native.prevent="handleLogin"
            >
              <span v-if="!loading">登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator'
import { Route } from 'vue-router'
import { Form as ElForm, Input } from 'element-ui'
import { UserModule } from '@/store/modules/user'
import { isValidUsername } from '@/utils/validate'

@Component({
  name: 'Login',
})
export default class extends Vue {
  private validateUsername = (rule: any, value: string, callback: Function) => {
    if (!value) {
      callback(new Error('请输入用户名'))
    } else {
      callback()
    }
  }
  private validatePassword = (rule: any, value: string, callback: Function) => {
    if (value.length < 6) {
      callback(new Error('密码必须在6位以上'))
    } else {
      callback()
    }
  }
  private loginForm = {
    username: 'admin',
    password: '123456',
  } as {
    username: String
    password: String
  }

  loginRules = {
    username: [{ validator: this.validateUsername, trigger: 'blur' }],
    password: [{ validator: this.validatePassword, trigger: 'blur' }],
  }
  private loading = false
  private redirect?: string

  @Watch('$route', { immediate: true })
  private onRouteChange(route: Route) {}

  // 登录
  private handleLogin() {
    ;(this.$refs.loginForm as ElForm).validate(async (valid: boolean) => {
      if (valid) {
        this.loading = true
        await UserModule.Login(this.loginForm as any)
          .then((res: any) => {
            if (String(res.code) === '1') {
              this.$router.push('/')
            } else {
              // this.$message.error(res.msg)
              this.loading = false
            }
          })
          .catch(() => {
            // this.$message.error('用户名或密码错误！')
            this.loading = false
          })
      } else {
        return false
      }
    })
  }
}
</script>

<style lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background: radial-gradient(circle at 18% 20%, rgba(27, 208, 201, .24), transparent 30%), radial-gradient(circle at 80% 80%, rgba(22, 119, 255, .3), transparent 35%), linear-gradient(135deg, #06182d, #0b3c68 60%, #0b6fc2);
  font-size: 18px; /* 基础字号增大，子元素继承 */
  position: relative;

  &::before {
    /* 轻微遮罩，提升表单对比度 */
    content: '';
    position: absolute;
    inset: 0;
    background-image: linear-gradient(rgba(255,255,255,.025) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,.025) 1px, transparent 1px);
    background-size: 42px 42px;
    pointer-events: none;
    z-index: 0;
  }
}

.login-box {
  width: 1000px;
  min-height: 520px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  /* transparent so page background (image) shows through */
  background: transparent;
  z-index: 1;
}

.title {
  margin: 0px auto 10px auto;
  text-align: left;
  color: #707070;
}

.login-form {
  background: #ffffff;
  width: 420px; /* 固定宽度以控制表单布局 */
  border-radius: 18px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 36px 40px;
  z-index: 2;

  .el-form {
    width: 100%;
    height: auto;
  }
  .el-form-item {
    margin-bottom: 28px;
  }
  .el-form-item.is-error .el-input__inner {
    border: 0 !important;
    border-bottom: 1px solid #fd7065 !important;
    background: #fff !important;
  }
  .input-icon {
    height: 36px;
    width: 20px;
    margin-left: -2px;
  }
  .el-input__inner {
    border: 0;
    border-bottom: 1px solid #e9e9e8;
    border-radius: 0;
    font-size: 16px; /* 增大输入框字号 */
    font-weight: 500;
    color: #333333;
    height: 42px;
    line-height: 42px;
    padding-left: 30px;
  }
  .el-input__prefix {
    left: 0;
  }
  .el-input--prefix .el-input__inner {
    padding-left: 26px;
  }
  .el-input__inner::placeholder {
    color: #aeb5c4;
    font-size: 16px;
  }
  .el-form-item--medium .el-form-item__content {
    line-height: 42px;
  }
  .el-input--medium .el-input__icon {
    line-height: 42px;
  }
}

.login-btn {
  border-radius: 22px;
  padding: 12px 26px !important;
  margin-top: 10px;
  font-weight: 600;
  font-size: 16px;
  border: 0;
  color: #ffffff;
  background: linear-gradient(90deg,#1677ff,#16a5d9);
  &:hover,
  &:focus {
    background: linear-gradient(90deg,#0f66df,#0792c7);
    color: #ffffff;
  }
}
.login-form-title {
  height: 56px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 30px;
  gap: 12px;
  .brand-icon { width:44px; height:44px; border-radius:13px; background:linear-gradient(135deg,#1cc8c0,#1677ff); display:flex; align-items:center; justify-content:center; color:#fff; font-weight:800; }
  strong { display:block; font-size:22px; color:#17304f; }
  small { display:block; font-size:9px; letter-spacing:1.8px; color:#8ca0b8; margin-top:4px; }
  .title-label {
    font-weight: 600;
    font-size: 24px;
    color: #333333;
    margin-left: 10px;
  }
}

/* 如果模板中还存在图片占位，确保不显示 */
.login-box img {
  display: none;
}
</style>
