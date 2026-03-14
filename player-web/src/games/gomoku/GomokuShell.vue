<template>
  <MultiplayerShell
    eyebrow="Board Game Room Flow"
    title="Gomoku now runs through distinct room and match screens."
    subtitle="This route split gives us a reusable multiplayer shell: stage the room first, then move into a clean match view once the server starts the game."
    :phase-title="phaseTitle"
    :phase-description="phaseDescription"
    :phase-class="phaseClass"
    :active-room-code="activeRoomCode"
    :match-code="matchCode"
    :my-stone-label="myStoneLabel"
    :socket-connected="socketConnected"
  >
    <RouterView />
  </MultiplayerShell>
</template>

<script setup lang="ts">
import { RouterView } from "vue-router";
import MultiplayerShell from "../../components/multiplayer/MultiplayerShell.vue";
import { useGomokuSession } from "./useGomokuSession";

const {
  phaseTitle,
  phaseDescription,
  phaseClass,
  activeRoomCode,
  matchCode,
  myStoneLabel,
  socketConnected
} = useGomokuSession();
</script>
