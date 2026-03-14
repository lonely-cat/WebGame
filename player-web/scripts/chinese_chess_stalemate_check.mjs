import { chromium } from "playwright";

const baseUrl = "http://127.0.0.1:5173/games/chinese-chess/room";

async function login(page, username, password) {
  await page.goto(baseUrl, { waitUntil: "networkidle" });
  await page.fill("#login-username", username);
  await page.fill("#login-password", password);
  await page.click("#login-btn");
  await page.waitForTimeout(600);
}

async function waitForRoomReady(page) {
  await page.waitForFunction(() => {
    const raw = window.render_game_to_text?.();
    if (!raw) return false;
    const state = JSON.parse(raw);
    const button = document.querySelector("#start-btn");
    return state.roomPlayers?.length === 2 && button instanceof HTMLButtonElement && !button.disabled;
  }, null, { timeout: 10000 });
}

async function waitForMatchScreen(page) {
  await page.waitForFunction(() => {
    const raw = window.render_game_to_text?.();
    if (!raw) return false;
    const state = JSON.parse(raw);
    return state.phase === "match" || Array.isArray(state.pieces);
  }, null, { timeout: 10000 });
}

async function main() {
  const browser = await chromium.launch({ headless: true });
  const pageA = await browser.newPage({ viewport: { width: 1440, height: 1400 } });
  const pageB = await browser.newPage({ viewport: { width: 1440, height: 1400 } });

  try {
    await login(pageA, "admin", "admin123");
    await pageA.click("text=Connect WS");
    await pageA.waitForTimeout(700);
    await pageA.click("text=Create Room");
    await pageA.waitForTimeout(1000);

    const roomCode = (await pageA.textContent("#active-room-code"))?.trim() ?? "";
    if (!roomCode || roomCode === "-") {
      throw new Error("Room code was not created on page A.");
    }
    await pageA.click("#join-room-btn");
    await pageA.waitForTimeout(500);
    await pageA.click("#ready-btn");
    await pageA.waitForTimeout(900);

    await login(pageB, "codex_test", "test123456");
    await pageB.click("text=Connect WS");
    await pageB.waitForTimeout(700);
    await pageB.fill("#room-code-input", roomCode);
    await pageB.click("#join-room-btn");
    await pageB.waitForTimeout(500);
    await pageB.click("#ready-btn");
    await pageB.waitForTimeout(1200);
    await waitForRoomReady(pageA);
    await pageA.click("#start-btn");
    await waitForMatchScreen(pageA);
    await waitForMatchScreen(pageB);
    await pageA.waitForFunction(() => typeof window.load_chinese_chess_scenario === "function");

    await pageA.evaluate(() => window.load_chinese_chess_scenario("stalemate_red"));

    await pageA.waitForFunction(() => {
      const state = JSON.parse(window.render_game_to_text());
      return state.endReason === "stalemate"
        && state.winner === "red"
        && state.scenarioName === "stalemate_red"
        && Array.isArray(state.pieces)
        && state.pieces.length === 5;
    }, null, { timeout: 10000 });
    await pageB.waitForFunction(() => {
      const state = JSON.parse(window.render_game_to_text());
      return state.endReason === "stalemate"
        && state.winner === "red"
        && state.scenarioName === "stalemate_red";
    }, null, { timeout: 10000 });

    const stateA = JSON.parse(await pageA.evaluate(() => window.render_game_to_text()));
    const stateB = JSON.parse(await pageB.evaluate(() => window.render_game_to_text()));

    console.log(JSON.stringify({ roomCode, stateA, stateB }, null, 2));
  } finally {
    await browser.close();
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
