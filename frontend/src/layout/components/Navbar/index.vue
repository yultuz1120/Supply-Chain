<template>
  <div class="navbar">
    <div class="left-area">
      <hamburger :is-active="sidebar.opened" class="hamburger" @toggleClick="toggleSideBar" />
      <div><strong>{{ pageTitle }}</strong><small>AI 智慧供应链管理平台</small></div>
    </div>
    <div class="right-area">
      <span class="system-state"><i /> 系统运行中</span>
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="user">{{ name }} <i class="el-icon-arrow-down" /></span>
        <el-dropdown-menu slot="dropdown"><el-dropdown-item command="password">修改密码</el-dropdown-item><el-dropdown-item command="logout" divided>退出登录</el-dropdown-item></el-dropdown-menu>
      </el-dropdown>
    </div>
    <Password :dialog-form-visible="dialogFormVisible" @handleclose="dialogFormVisible=false" />
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { AppModule } from '@/store/modules/app'
import { UserModule } from '@/store/modules/user'
import Hamburger from '@/components/Hamburger/index.vue'
import Password from '../components/password.vue'
import Cookies from 'js-cookie'

@Component({ name: 'Navbar', components: { Hamburger, Password } })
export default class extends Vue {
  private dialogFormVisible = false
  get sidebar() { return AppModule.sidebar }
  get pageTitle() { return (this.$route.meta && this.$route.meta.title) || '工作台' }
  get name() { if ((UserModule.userInfo as any).name) return (UserModule.userInfo as any).name; const text = Cookies.get('user_info'); return text ? JSON.parse(text).name : '管理员' }
  private toggleSideBar() { AppModule.ToggleSideBar(false) }
  private handleCommand(command: string) { if (command === 'password') this.dialogFormVisible = true; if (command === 'logout') this.$store.dispatch('LogOut').then(() => this.$router.replace({ path: '/login' })) }
}
</script>

<style lang="scss" scoped>
.navbar{height:60px;background:#fff;border-bottom:1px solid #e8eef6;display:flex;align-items:center;justify-content:space-between;padding:0 24px 0 12px}.left-area{display:flex;align-items:center;gap:10px}.hamburger{padding:10px;cursor:pointer}.left-area strong{display:block;color:#1d2d44;font-size:16px}.left-area small{display:block;color:#98a4b5;margin-top:3px}.right-area{display:flex;align-items:center;gap:26px}.system-state{font-size:13px;color:#607089}.system-state i{display:inline-block;width:8px;height:8px;border-radius:50%;background:#36c98f;box-shadow:0 0 0 4px rgba(54,201,143,.13)}.user{cursor:pointer;padding:9px 14px;border-radius:9px;background:#f3f7fc;color:#263b59}
</style>
