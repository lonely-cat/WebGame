import type { RouteRecordRaw } from "vue-router";
import { defineComponent, h } from "vue";

const DashboardView = defineComponent({
  name: "DashboardView",
  setup() {
    return () => h("div", "Admin dashboard");
  }
});

const UserView = defineComponent({
  name: "UserView",
  setup() {
    return () => h("div", "User management");
  }
});

export const routes: RouteRecordRaw[] = [
  { path: "/", component: DashboardView },
  { path: "/users", component: UserView }
];
