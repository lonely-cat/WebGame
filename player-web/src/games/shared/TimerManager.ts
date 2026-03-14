export class TimerManager {
  private readonly timers = new Map<string, number>();

  start(name: string, duration: number) {
    this.timers.set(name, Date.now() + duration);
  }

  stop(name: string) {
    this.timers.delete(name);
  }

  getRemaining(name: string) {
    const endAt = this.timers.get(name);
    return endAt ? Math.max(0, endAt - Date.now()) : 0;
  }

  clearAll() {
    this.timers.clear();
  }
}
