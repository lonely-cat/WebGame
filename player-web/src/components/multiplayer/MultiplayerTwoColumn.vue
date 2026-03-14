<template>
  <section class="workspace" :class="variantClass">
    <aside class="sidebar">
      <slot name="sidebar" />
    </aside>

    <main class="main-stage">
      <slot name="main" />
    </main>
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(defineProps<{
  variant?: "room" | "match";
}>(), {
  variant: "room"
});

const variantClass = computed(() => `variant-${props.variant}`);
</script>

<style scoped>
.workspace {
  display: grid;
  gap: 24px;
  align-items: start;
}

.workspace.variant-room {
  grid-template-columns: 340px minmax(0, 1fr);
}

.workspace.variant-match {
  grid-template-columns: 320px minmax(0, 1fr);
}

.sidebar,
.main-stage {
  display: grid;
  gap: 18px;
}

@media (max-width: 1080px) {
  .workspace,
  .workspace.variant-room,
  .workspace.variant-match {
    grid-template-columns: 1fr;
  }
}
</style>
