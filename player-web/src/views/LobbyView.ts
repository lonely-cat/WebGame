import { defineComponent, h } from "vue";

export const LobbyView = defineComponent({
  name: "LobbyView",
  setup() {
    return () => h("div", "Game lobby");
  }
});
