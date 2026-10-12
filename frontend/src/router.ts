import Vue from "vue";
import Router from "vue-router";
import Layout from "@/layout/index.vue";

Vue.use(Router);

const router = new Router({
  scrollBehavior: (to, from, savedPosition) => {
    if (savedPosition) {
      return savedPosition;
    }
    return { x: 0, y: 0 };
  },
  base: process.env.BASE_URL,
  routes: [
    {
      path: "/login",
      component: () =>
        import(/* webpackChunkName: "login" */ "@/views/login/index.vue"),
      meta: { title: "AI 智慧供应链", hidden: true, notNeedAuth: true }
    },
    {
      path: "/404",
      component: () => import(/* webpackChunkName: "404" */ "@/views/404.vue"),
      meta: { title: "AI 智慧供应链", hidden: true, notNeedAuth: true }
    },
    {
      path: "/",
      component: Layout,
      redirect: "/dashboard",
      children: [
        {
          path: "dashboard",
          component: () =>
            import(/* webpackChunkName: "dashboard" */ "@/views/dashboard/index.vue"),
          name: "Dashboard",
          meta: {
            title: "工作台",
            icon: "dashboard",
            affix: true
          }
        },
        {
          path: "employee",
          component: () =>
            import(/* webpackChunkName: "group" */ "@/views/employee/index.vue"),
          meta: {
            title: "集团管理",
            icon: "icon-employee"
          }
        },
        {
          path: "stores",
          component: () =>
            import(/* webpackChunkName: "supplyChain" */ "@/views/store/index.vue"),
          meta: {
            title: "门店管理",
            icon: "icon-category"
          }
        },
        {
          path: "delivery",
          component: () =>
            import(/* webpackChunkName: "supplyChain" */ "@/views/delivery/index.vue"),
          meta: {
            title: "配送中心",
            icon: "icon-order"
          }
        },
        {
          path: "ai-assistant",
          component: () =>
            import(/* webpackChunkName: "ai" */ "@/views/ai/index.vue"),
          meta: {
            title: "智能客服",
            icon: "icon-statistics"
          }
        },
        {
          path: "knowledge",
          component: () =>
            import(/* webpackChunkName: "ai" */ "@/views/knowledge/index.vue"),
          meta: {
            title: "知识库管理",
            icon: "icon-combo"
          }
        },
        
        {
          path: "/employee/add",
          component: () =>
            import(/* webpackChunkName: "dashboard" */ "@/views/employee/addEmployee.vue"),
          meta: {
            title: "添加人员",
            hidden: true
          }
        }
      ]
    },
    {
      path: "*",
      redirect: "/404",
      meta: { hidden: true }
    }
  ]
});

export default router;
