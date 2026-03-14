export class GameSocketClient {
  private socket: WebSocket | null = null;
  private messageHandlers: Array<(message: MessageEvent) => void> = [];
  private openHandlers: Array<() => void> = [];
  private closeHandlers: Array<() => void> = [];

  connect(token: string, userId: number) {
    const protocol = location.protocol === "https:" ? "wss" : "ws";
    this.socket = new WebSocket(`${protocol}://${location.host}/ws/game?token=${token}&userId=${userId}`);
    this.socket.onmessage = (event) => this.messageHandlers.forEach((handler) => handler(event));
    this.socket.onopen = () => this.openHandlers.forEach((handler) => handler());
    this.socket.onclose = () => this.closeHandlers.forEach((handler) => handler());
  }

  disconnect() {
    this.socket?.close();
    this.socket = null;
  }

  send(message: unknown) {
    this.socket?.send(JSON.stringify(message));
  }

  onMessage(handler: (message: MessageEvent) => void) {
    this.messageHandlers.push(handler);
  }

  onOpen(handler: () => void) {
    this.openHandlers.push(handler);
  }

  onClose(handler: () => void) {
    this.closeHandlers.push(handler);
  }

  heartbeat() {
    this.send({ type: "HEARTBEAT", timestamp: Date.now() });
  }
}
