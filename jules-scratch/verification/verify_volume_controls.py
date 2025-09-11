from playwright.sync_api import sync_playwright, expect
import time

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()
    page = context.new_page()

    try:
        page.goto("http://localhost:8000")

        # Click the start audio button
        start_button = page.locator("#start-button-container button")
        expect(start_button).to_be_visible()
        start_button.click()

        # Wait for app to initialize
        expect(page.locator("#drum-matrix")).to_be_visible(timeout=15000)

        # Test global volume
        page.locator("#global-vol-knob").fill("50")
        global_vol = page.evaluate("Tone.Destination.volume.value")
        print(f"Global volume: {global_vol}")
        assert -21 < global_vol < -19

        # Test BPM
        page.locator("#bpm-knob").fill("140")
        bpm = page.evaluate("Tone.Transport.bpm.value")
        print(f"BPM: {bpm}")
        assert bpm == 140

        # Test synth 1 volume
        page.locator("#synth0-vol-knob").fill("25")
        synth0_vol = page.evaluate("synthVolumes[0].volume.value")
        print(f"Synth 0 volume: {synth0_vol}")
        assert -31 < synth0_vol < -29

        # Test drums volume
        page.locator("#drums-vol-knob").fill("75")
        drums_vol = page.evaluate("drumsVolume.volume.value")
        print(f"Drums volume: {drums_vol}")
        assert -11 < drums_vol < -9

        page.screenshot(path="jules-scratch/verification/volume_controls.png")

        print("Verification script ran successfully.")

    except Exception as e:
        print(f"An error occurred: {e}")
        page.screenshot(path="jules-scratch/verification/error.png")

    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)
