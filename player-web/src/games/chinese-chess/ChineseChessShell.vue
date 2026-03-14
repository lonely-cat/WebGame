<template>
  <MultiplayerShell
    eyebrow="Board Game Room Flow"
    title="Chinese Chess is now plugged into the multiplayer room framework."
    subtitle="The room flow, live board, and first-pass move validation are all in place, giving Xiangqi the same multiplayer foundation as the rest of the platform."
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
import { useChineseChessSession } from "./useChineseChessSession";

const {
  phaseTitle,
  phaseDescription,
  phaseClass,
  activeRoomCode,
  matchCode,
  roleLabel,
  socketConnected
} = useChineseChessSession();
</script>
