import { GameRegistry } from "./GameRegistry";

export const gameRegistry = new GameRegistry();

gameRegistry.register({
  gameCode: "gomoku",
  gameName: "Gomoku",
  blurb: "Real-time five-in-a-row with room flow, ready checks, and synchronized moves.",
  genre: "Board",
  status: "playable",
  mode: "vue",
  loader: () => import("../games/gomoku/GomokuView.vue"),
  multiplayer: true,
  routePath: "/games/gomoku"
});

gameRegistry.register({
  gameCode: "chinese-chess",
  gameName: "Chinese Chess",
  blurb: "Next in line after Gomoku, with piece rules and checkmate logic moving server-side.",
  genre: "Board",
  status: "prototype",
  mode: "vue",
  loader: () => Promise.resolve(null),
  multiplayer: true,
  routePath: "/games/chinese-chess"
});

gameRegistry.register({
  gameCode: "blackjack",
  gameName: "Blackjack",
  blurb: "Card table framework for score calculation, turns, and reward settlement.",
  genre: "Card",
  status: "prototype",
  mode: "vue",
  loader: () => Promise.resolve(null),
  multiplayer: true,
  routePath: "/games/blackjack"
});

gameRegistry.register({
  gameCode: "doodle",
  gameName: "Doodle",
  blurb: "Casual drawing playground that will feed into creative and social mini-games.",
  genre: "Creative",
  status: "planned",
  mode: "static",
  loader: () => Promise.resolve(null),
  multiplayer: true,
  routePath: "/games/doodle"
});

gameRegistry.register({
  gameCode: "draw-guess",
  gameName: "Draw and Guess",
  blurb: "Social drawing party game with hidden words, timed rounds, and fast guess scoring.",
  genre: "Party",
  status: "prototype",
  mode: "vue",
  loader: () => Promise.resolve(null),
  multiplayer: true,
  routePath: "/games/draw-guess"
});
