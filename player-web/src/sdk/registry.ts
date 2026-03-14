import { GameRegistry } from "./GameRegistry";

export const gameRegistry = new GameRegistry();

gameRegistry.register({
  gameCode: "gomoku",
  gameName: "Gomoku",
  mode: "vue",
  loader: () => Promise.resolve(null),
  multiplayer: true,
  routePath: "/games/gomoku"
});

gameRegistry.register({
  gameCode: "chinese-chess",
  gameName: "Chinese Chess",
  mode: "vue",
  loader: () => Promise.resolve(null),
  multiplayer: true,
  routePath: "/games/chinese-chess"
});

gameRegistry.register({
  gameCode: "blackjack",
  gameName: "Blackjack",
  mode: "vue",
  loader: () => Promise.resolve(null),
  multiplayer: true,
  routePath: "/games/blackjack"
});

gameRegistry.register({
  gameCode: "doodle",
  gameName: "Doodle",
  mode: "static",
  loader: () => Promise.resolve(null),
  multiplayer: true,
  routePath: "/games/doodle"
});

gameRegistry.register({
  gameCode: "draw-guess",
  gameName: "Draw and Guess",
  mode: "vue",
  loader: () => Promise.resolve(null),
  multiplayer: true,
  routePath: "/games/draw-guess"
});
