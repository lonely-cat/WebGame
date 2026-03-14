import { defineComponent, h } from "vue";

export const HomeView = defineComponent({
  name: "HomeView",
  setup() {
    return () => h("div", "WebGame home");
  }
});
