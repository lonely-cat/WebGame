import type { GameContext } from "./types";

export abstract class BaseGame {
  protected container: HTMLElement | null = null;
  protected context: GameContext | null = null;

  init(container: HTMLElement, context: GameContext) {
    this.container = container;
    this.context = context;
  }

  abstract start(): void;

  pause() {}

  resume() {}

  restart() {}

  destroy() {
    this.container = null;
    this.context = null;
  }

  getSnapshot(): Record<string, unknown> {
    return {};
  }

  handleServerMessage(_message: unknown) {}
}
