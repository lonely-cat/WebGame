export interface DrawGuessStroke {
  points: Array<{ x: number; y: number }>;
  color: string;
  width: number;
}

export interface DrawGuessRoundState {
  roundNo: number;
  drawerUserId: number | null;
  secretWordLength: number;
  remainingMs: number;
}
