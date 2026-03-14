<template>
  <MultiplayerShell
    eyebrow="Board Game Room Flow"
    title="Chinese Chess is now plugged into the multiplayer room framework."
    subtitle="The room and match flow are live first so we can stabilize the platform shell before landing full Xiangqi rules and board interaction."
    :phase-title="phaseTitle"
    :phase-description="phaseDescription"
    :phase-class="phaseClass"
    :active-room-code="activeRoomCode"
    :match-code="matchCode"
    :my-stone-label="roleLabel"
    :socket-connected="socketConnected"
  >
    <RouterView />
  </MultiplayerShell>
</template>

<script setup lang="ts">
import { RouterView } from "vue-router";
import MultiplayerShell from "../../components/multiplayer/MultiplayerShell.vue";
import { useMultiplayerRoomSession } from "../../composables/useMultiplayerRoomSession";

const {
  phaseTitle,
  phaseDescription,
  phaseClass,
  activeRoomCode,
  matchCode,
  roleLabel,
  socketConnected
} = useMultiplayerRoomSession("chinese-chess", {
  testUserPrefix: "xiangqi"
});
</script>
