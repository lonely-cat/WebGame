export class GameSocketClient {
  private socket: WebSocket | null = null;
  private messageHandlers: Array<(message: MessageEvent) => void> = [];

  connect(token: string, userId: number) {
    const protocol = location.protocol === "https:" ? "wss" : "ws";
    this.socket = new WebSocket(`${protocol}://${location.host}/ws/game?token=${token}&userId=${userId}`);
    this.socket.onmessage = (event) => this.messageHandlers.forEach((handler) => handler(event));
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
    if (this.socket) {
      this.socket.onopen = handler;
    }
  }

  onClose(handler: () => void) {
    if (this.socket) {
      this.socket.onclose = handler;
    }
  }

  heartbeat() {
    this.send({ type: "HEARTBEAT", timestamp: Date.now() });
  }
}
