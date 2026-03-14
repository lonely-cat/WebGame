export abstract class CanvasGame {
  protected canvas: HTMLCanvasElement | null = null;
  protected animationFrameId = 0;

  mount(canvas: HTMLCanvasElement) {
    this.canvas = canvas;
  }

  unmount() {
    this.stopLoop();
    this.canvas = null;
  }

  startLoop() {
    const tick = () => {
      this.update(16.67);
      this.render();
      this.animationFrameId = requestAnimationFrame(tick);
    };
    this.animationFrameId = requestAnimationFrame(tick);
  }

  stopLoop() {
    cancelAnimationFrame(this.animationFrameId);
  }

  abstract update(deltaTime: number): void;

  abstract render(): void;

  resize() {}
}
