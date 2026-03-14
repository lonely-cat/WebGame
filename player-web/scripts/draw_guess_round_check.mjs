import { chromium } from "playwright";

const baseUrl = "http://127.0.0.1:5173/games/draw-guess/room";

async function login(page, username, password) {
  await page.goto(baseUrl, { waitUntil: "networkidle" });
  await page.fill("#login-username", username);
  await page.fill("#login-password", password);
  await page.click("#login-btn");
  await page.waitForTimeout(600);
}

async function main() {
  const browser = await chromium.launch({ headless: true });
  const pageA = await browser.newPage({ viewport: { width: 1440, height: 1400 } });
  const pageB = await browser.newPage({ viewport: { width: 1440, height: 1400 } });

  try {
    await login(pageA, "admin", "admin123");
    await pageA.click("#quick-start-btn");
    await pageA.waitForTimeout(1400);

    const roomCode = (await pageA.textContent("#active-room-code"))?.trim() ?? "";
    if (!roomCode || roomCode === "-") {
      throw new Error("Room code was not created on page A.");
    }

    await login(pageB, "codex_test", "test123456");
    await pageB.click("text=Connect WS");
    await pageB.waitForTimeout(700);
    await pageB.fill("#room-code-input", roomCode);
    await pageB.click("#join-room-btn");
    await pageB.waitForTimeout(500);
    await pageB.click("#ready-btn");
    await pageB.waitForTimeout(900);
    await pageA.click("#start-btn");
    await pageA.waitForTimeout(1200);

    const drawerState = JSON.parse(await pageA.evaluate(() => window.render_game_to_text()));
    const promptText = drawerState.prompt?.replace(/^Prompt:\s*/i, "") ?? "";
    if (!promptText || promptText.includes("_")) {
      throw new Error(`Drawer prompt was not available: ${promptText}`);
    }

    const canvas = pageA.locator("canvas");
    const box = await canvas.boundingBox();
    if (!box) {
      throw new Error("Canvas not found.");
    }

    await pageA.mouse.move(box.x + 120, box.y + 120);
    await pageA.mouse.down();
    await pageA.mouse.move(box.x + 220, box.y + 160);
    await pageA.mouse.move(box.x + 320, box.y + 210);
    await pageA.mouse.up();
    await pageA.waitForTimeout(900);

    await pageB.fill("input[placeholder='Type a guess and press submit']", promptText);
    await pageB.press("input[placeholder='Type a guess and press submit']", "Enter");
    await pageA.waitForFunction(() => {
      const state = JSON.parse(window.render_game_to_text());
      return state.roundPhase === "round_finished";
    }, null, { timeout: 10000 });
    await pageB.waitForFunction(() => {
      const state = JSON.parse(window.render_game_to_text());
      return state.roundPhase === "round_finished";
    }, null, { timeout: 10000 });

    const finishedA = JSON.parse(await pageA.evaluate(() => window.render_game_to_text()));
    const finishedB = JSON.parse(await pageB.evaluate(() => window.render_game_to_text()));

    await pageA.click("text=Next Round");
    await pageA.waitForFunction(() => {
      const state = JSON.parse(window.render_game_to_text());
      return state.roundNo === 2 && state.roundPhase === "drawing";
    }, null, { timeout: 10000 });
    await pageB.waitForFunction(() => {
      const state = JSON.parse(window.render_game_to_text());
      return state.roundNo === 2 && state.roundPhase === "drawing";
    }, null, { timeout: 10000 });

    const nextRoundA = JSON.parse(await pageA.evaluate(() => window.render_game_to_text()));
    const nextRoundB = JSON.parse(await pageB.evaluate(() => window.render_game_to_text()));

    console.log(JSON.stringify({
      roomCode,
      promptText,
      finishedA,
      finishedB,
      nextRoundA,
      nextRoundB
    }, null, 2));
  } finally {
    await browser.close();
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
