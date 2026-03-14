<template>
  <MultiplayerShell
    eyebrow="Party Game Room Flow"
    title="Draw and Guess is now plugged into the shared multiplayer shell."
    subtitle="We can already gather players, launch a match, and enter a dedicated party-game stage before the full drawing and guessing rules land."
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
} = useMultiplayerRoomSession("draw-guess", {
  testUserPrefix: "drawguess"
});
</script>
