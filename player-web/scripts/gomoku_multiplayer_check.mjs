import { chromium } from "playwright";

const baseUrl = "http://127.0.0.1:5173/games/gomoku";

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
    await pageB.waitForTimeout(700);

    const canvasA = pageA.locator("canvas");
    const box = await canvasA.boundingBox();
    if (!box) {
      throw new Error("Canvas not found on page A.");
    }

    await pageA.mouse.click(box.x + 514, box.y + 257);
    await pageA.waitForTimeout(900);

    const stateA = JSON.parse(await pageA.evaluate(() => window.render_game_to_text()));
    const stateB = JSON.parse(await pageB.evaluate(() => window.render_game_to_text()));

    console.log(JSON.stringify({
      roomCode,
      pageA: stateA,
      pageB: stateB
    }, null, 2));
  } finally {
    await browser.close();
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
