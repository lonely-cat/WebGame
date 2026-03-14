export class DrawGuessRound {
  nextRound() {}

  isRoundOver() {
    return false;
  }

  assignNextDrawer<T>(players: T[]): T | null {
    return players[0] ?? null;
  }

  calculateRoundScore(_roundState: unknown) {
    return 0;
  }
}
