<script setup lang="ts">
import { onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

onMounted(() => {
  router.replace("/games/gomoku/room");
});
</script>

<template>
  <div class="redirecting">Redirecting to the room...</div>
</template>
