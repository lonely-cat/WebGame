<template>
  <div class="lobby-page">
    <header class="lobby-header">
      <div>
        <p class="eyebrow">Lobby</p>
        <h1>Choose a game lane and keep the multiplayer platform moving forward.</h1>
      </div>
      <RouterLink class="home-link" to="/">Back Home</RouterLink>
    </header>

    <section class="grid">
      <article
        v-for="game in games"
        :key="game.gameCode"
        class="game-card"
        :class="game.status ?? 'planned'"
      >
        <div class="card-top">
          <span class="status">{{ game.status ?? "planned" }}</span>
          <span class="genre">{{ game.genre ?? "Arcade" }}</span>
        </div>
        <h2>{{ game.gameName }}</h2>
        <p>{{ game.blurb }}</p>
        <div class="card-meta">
          <span>{{ game.multiplayer ? "Multiplayer" : "Single Player" }}</span>
          <span>{{ game.mode === "vue" ? "Vue Runtime" : "Static Runtime" }}</span>
        </div>
        <RouterLink class="enter-link" :to="game.routePath">
          {{ game.status === "planned" ? "View Placeholder" : "Enter Game" }}
        </RouterLink>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from "vue-router";
import { gameRegistry } from "../sdk/registry";

const games = gameRegistry.getAllGames();
</script>

<style scoped>
.lobby-page {
  min-height: 100vh;
  padding: 32px;
  color: #241307;
  background:
    radial-gradient(circle at top right, rgba(255, 226, 171, 0.9), transparent 28%),
    linear-gradient(135deg, #f7ecd6 0%, #e6c186 48%, #c07b45 100%);
  font-family: Georgia, "Times New Roman", serif;
}

.lobby-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.eyebrow {
  margin: 0 0 10px;
  font-size: 13px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  max-width: 860px;
  font-size: clamp(34px, 5vw, 62px);
  line-height: 0.97;
}

.home-link,
.enter-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 16px;
  border-radius: 16px;
  text-decoration: none;
}

.home-link {
  color: #442109;
  background: rgba(255, 249, 236, 0.9);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 22px;
  margin-top: 28px;
}

.game-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px;
  border: 1px solid rgba(63, 31, 10, 0.16);
  border-radius: 26px;
  background: rgba(255, 248, 235, 0.76);
  box-shadow: 0 18px 38px rgba(87, 47, 16, 0.16);
}

.game-card.playable {
  background: linear-gradient(180deg, rgba(255, 248, 235, 0.94), rgba(254, 233, 193, 0.88));
}

.card-top,
.card-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.status,
.genre,
.card-meta span {
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

h2 {
  margin: 0;
  font-size: 28px;
}

p {
  margin: 0;
  line-height: 1.65;
}

.enter-link {
  margin-top: auto;
  color: #fff5df;
  background: linear-gradient(135deg, #3f1f09, #885122);
}

@media (max-width: 780px) {
  .lobby-header {
    flex-direction: column;
  }
}
</style>
