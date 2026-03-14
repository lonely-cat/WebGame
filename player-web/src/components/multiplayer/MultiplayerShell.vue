<template>
  <div class="multiplayer-shell">
    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">{{ eyebrow }}</p>
        <h1>{{ title }}</h1>
        <p class="subtitle">{{ subtitle }}</p>
      </div>
      <div class="phase-card">
        <p class="phase-label">Current Phase</p>
        <strong>{{ phaseTitle }}</strong>
        <p>{{ phaseDescription }}</p>
        <div class="phase-pills">
          <span :class="phaseClass('auth')">1. Login</span>
          <span :class="phaseClass('room')">2. Room</span>
          <span :class="phaseClass('match')">3. Match</span>
        </div>
      </div>
    </section>

    <section class="shell-meta">
      <div class="meta-card">
        <span class="meta-label">Room</span>
        <strong id="active-room-code">{{ activeRoomCode || "-" }}</strong>
      </div>
      <div class="meta-card">
        <span class="meta-label">Match</span>
        <strong id="active-match-code">{{ matchCode || "-" }}</strong>
      </div>
      <div class="meta-card">
        <span class="meta-label">Stone</span>
        <strong id="my-stone">{{ myStoneLabel }}</strong>
      </div>
      <div class="meta-card">
        <span class="meta-label">Socket</span>
        <strong>{{ socketConnected ? "ready" : "offline" }}</strong>
      </div>
    </section>

    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  eyebrow: string;
  title: string;
  subtitle: string;
  phaseTitle: string;
  phaseDescription: string;
  phaseClass: (target: "auth" | "room" | "match") => Record<string, boolean>;
  activeRoomCode: string;
  matchCode: string;
  myStoneLabel: string;
  socketConnected: boolean;
}>();
</script>

<style scoped>
.multiplayer-shell {
  min-height: 100vh;
  padding: 28px;
  color: #2a1908;
  background:
    radial-gradient(circle at top left, rgba(255, 231, 176, 0.9), transparent 32%),
    radial-gradient(circle at top right, rgba(225, 160, 86, 0.42), transparent 28%),
    linear-gradient(135deg, #f8edd0 0%, #deb87a 52%, #c88c53 100%);
  font-family: Georgia, "Times New Roman", serif;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(280px, 0.9fr);
  gap: 24px;
  margin-bottom: 24px;
}

.hero-copy {
  padding: 8px 0;
}

.eyebrow,
.phase-label,
.meta-label {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

h1 {
  margin: 0 0 12px;
  font-size: clamp(42px, 5vw, 72px);
  line-height: 0.94;
}

.subtitle,
.phase-card p {
  line-height: 1.6;
}

.subtitle {
  max-width: 760px;
  font-size: 18px;
}

.phase-card,
.meta-card {
  border: 1px solid rgba(72, 38, 10, 0.18);
  border-radius: 24px;
  background: rgba(255, 247, 229, 0.76);
  box-shadow: 0 18px 40px rgba(88, 50, 16, 0.18);
  backdrop-filter: blur(8px);
}

.phase-card {
  padding: 20px;
}

.phase-card strong {
  display: block;
  font-size: 26px;
  margin-bottom: 8px;
}

.phase-pills {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 18px;
}

.phase-pills :deep(.pill) {
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 251, 242, 0.75);
  color: rgba(74, 37, 9, 0.72);
}

.phase-pills :deep(.pill.active) {
  background: linear-gradient(135deg, #4a240a, #9a5f27);
  color: #fff4e0;
}

.phase-pills :deep(.pill.complete) {
  background: rgba(112, 76, 31, 0.16);
  color: #4a2509;
}

.shell-meta {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.meta-card {
  padding: 16px 18px;
}

.meta-card strong {
  font-size: 24px;
}

@media (max-width: 1080px) {
  .shell-meta {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .shell-meta {
    grid-template-columns: 1fr;
  }
}
</style>
