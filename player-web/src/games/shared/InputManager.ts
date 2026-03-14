export class InputManager {
  private pressedKeys = new Set<string>();
  private mousePosition = { x: 0, y: 0 };

  bindKeyboard() {
    window.addEventListener("keydown", this.onKeyDown);
    window.addEventListener("keyup", this.onKeyUp);
  }

  bindMouse() {
    window.addEventListener("mousemove", this.onMouseMove);
  }

  unbindAll() {
    window.removeEventListener("keydown", this.onKeyDown);
    window.removeEventListener("keyup", this.onKeyUp);
    window.removeEventListener("mousemove", this.onMouseMove);
  }

  isKeyPressed(key: string) {
    return this.pressedKeys.has(key);
  }

  getMousePosition() {
    return this.mousePosition;
  }

  private onKeyDown = (event: KeyboardEvent) => this.pressedKeys.add(event.key);
  private onKeyUp = (event: KeyboardEvent) => this.pressedKeys.delete(event.key);
  private onMouseMove = (event: MouseEvent) => {
    this.mousePosition = { x: event.clientX, y: event.clientY };
  };
}
