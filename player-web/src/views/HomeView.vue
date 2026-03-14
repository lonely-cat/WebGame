<template>
  <div class="home-page">
    <section class="hero">
      <div class="copy">
        <p class="eyebrow">WebGame Arcade</p>
        <h1>One place for board games, party rooms, cards, and drawing battles.</h1>
        <p class="summary">
          We are building this as a reusable multiplayer platform first, then expanding game by game.
          Gomoku is already playable end-to-end, and the rest of the arcade is lining up behind it.
        </p>
        <div class="actions">
          <RouterLink class="primary" to="/lobby">Enter Lobby</RouterLink>
          <RouterLink class="secondary" to="/games/gomoku">Play Gomoku</RouterLink>
        </div>
      </div>

      <div class="hero-card">
        <div class="metric">
          <span>Playable Now</span>
          <strong>1</strong>
        </div>
        <div class="metric">
          <span>Games Planned</span>
          <strong>{{ games.length }}</strong>
        </div>
        <div class="metric">
          <span>Realtime Stack</span>
          <strong>Spring + WS</strong>
        </div>
        <div class="hero-note">
          Current milestone: server-authoritative Gomoku room flow with dual-client sync.
        </div>
      </div>
    </section>

    <section class="featured">
      <header>
        <p class="eyebrow">Featured Track</p>
        <h2>What we are polishing right now</h2>
      </header>

      <article class="feature-card">
        <div>
          <span class="tag">Playable</span>
          <h3>Gomoku Multiplayer Prototype</h3>
          <p>
            Room creation, join flow, ready states, match start, move sync, turn validation,
            and illegal move rejection are already wired into the backend.
          </p>
        </div>
        <RouterLink class="primary" to="/games/gomoku">Open Prototype</RouterLink>
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
.home-page {
  min-height: 100vh;
  padding: 32px;
  color: #1f1208;
  background:
    radial-gradient(circle at top left, rgba(255, 230, 178, 0.95), transparent 32%),
    radial-gradient(circle at bottom right, rgba(190, 93, 43, 0.22), transparent 26%),
    linear-gradient(135deg, #fbf0d5 0%, #e7c27a 54%, #bb7b48 100%);
  font-family: Georgia, "Times New Roman", serif;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(300px, 0.8fr);
  gap: 28px;
  align-items: stretch;
}

.eyebrow {
  margin: 0 0 10px;
  font-size: 13px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  font-size: clamp(42px, 6vw, 76px);
  line-height: 0.94;
  max-width: 820px;
}

.summary {
  max-width: 720px;
  margin: 20px 0 0;
  font-size: 18px;
  line-height: 1.65;
}

.actions {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  margin-top: 28px;
}

.primary,
.secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 13px 18px;
  border-radius: 16px;
  text-decoration: none;
  font-size: 15px;
}

.primary {
  color: #fff5df;
  background: linear-gradient(135deg, #3e1f0a, #824b1e);
}

.secondary {
  color: #442109;
  background: rgba(255, 249, 236, 0.88);
}

.hero-card,
.feature-card {
  border: 1px solid rgba(68, 33, 9, 0.16);
  border-radius: 28px;
  background: rgba(255, 247, 231, 0.72);
  box-shadow: 0 20px 42px rgba(80, 44, 16, 0.18);
  backdrop-filter: blur(9px);
}

.hero-card {
  display: grid;
  gap: 14px;
  padding: 22px;
}

.metric {
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 252, 243, 0.86);
}

.metric span {
  display: block;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.metric strong {
  display: block;
  margin-top: 4px;
  font-size: 30px;
}

.hero-note {
  margin-top: 6px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(78, 35, 7, 0.88);
  color: #fff3e0;
  line-height: 1.6;
}

.featured {
  margin-top: 34px;
}

.featured h2 {
  margin: 0;
  font-size: 30px;
}

.feature-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-top: 16px;
  padding: 22px;
}

.feature-card h3 {
  margin: 10px 0 8px;
  font-size: 28px;
}

.feature-card p {
  max-width: 720px;
  margin: 0;
  line-height: 1.65;
}

.tag {
  display: inline-flex;
  padding: 7px 10px;
  border-radius: 999px;
  background: rgba(63, 31, 10, 0.1);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

@media (max-width: 960px) {
  .hero,
  .feature-card {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
