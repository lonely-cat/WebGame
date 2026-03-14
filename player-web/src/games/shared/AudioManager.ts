export class AudioManager {
  private muted = false;

  load(_name: string, _url: string) {}

  play(name: string) {
    if (!this.muted) {
      console.info(`play audio: ${name}`);
    }
  }

  stop(name: string) {
    console.info(`stop audio: ${name}`);
  }

  toggleMute() {
    this.muted = !this.muted;
  }
}
