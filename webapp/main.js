console.log("main.js loaded");

let peer = null;
let hostId = null;
const conns = [];
let myPeerId = null;
let claimedInstruments = {};

const sampleBasePath = 'assets/';
const samples808 = [
    '808bd.raw', '808sd_base.raw', '808ch.raw', '808oh.raw',
    '808cp.raw', '808cb.raw', '808hc.raw', '808mc.raw', '808crash.raw'
];

const players = {};
let drumSequence;
const synths = [];
const synthVolumes = [];
const synthDistortions = [];
const synthDelays = [];
const synthSequences = [];
const synthPatterns = [[], [], [], []];
let activeView = 'drums';
let drumsVolume;
const muteStates = {
    drums: false,
    synth0: false,
    synth1: false,
    synth2: false,
    synth3: false,
};
let slideCanvas, slideCtx;
const octaveOffsets = [0, 0, 0, 0];

const notes = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"];
const octaves = 8;
const displayOctaves = 3;

// --- Drum Pattern Templates ---
const drumTemplates = [
    { name: 'House', pattern: { '808bd.raw': [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0], '808sd_base.raw': [0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0], '808ch.raw': [0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0] } },
    { name: 'Techno', pattern: { '808bd.raw': [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1], '808sd_base.raw': [0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0], '808oh.raw': [0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1] } },
    { name: 'Hip Hop', pattern: { '808bd.raw': [1,0,0,1,0,0,1,0,1,0,0,0,0,0,1,0], '808sd_base.raw': [0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0], '808ch.raw': [1,1,1,1,1,1,1,0,1,1,1,0,1,1,1,0] } },
];


// --- Audio Helper ---
function createAudioBuffer(arrayBuffer) {
    const audioContext = Tone.context.rawContext;
    const int16s = new Int16Array(arrayBuffer);
    const float32s = new Float32Array(int16s.length);
    for (let i = 0; i < int16s.length; i++) {
        float32s[i] = int16s[i] / 32768;
    }
    const audioBuffer = audioContext.createBuffer(2, float32s.length / 2, 44100);
    const left = audioBuffer.getChannelData(0);
    const right = audioBuffer.getChannelData(1);
    for (let i = 0; i < float32s.length / 2; i++) {
        left[i] = float32s[i * 2];
        right[i] = float32s[i * 2 + 1];
    }
    return audioBuffer;
}

// --- Drum Machine ---
async function loadSamples() {
    drumsVolume = new Tone.Volume(0).toDestination();
    const promises = samples808.map(async (sampleName) => {
        const path = sampleBasePath + sampleName;
        const response = await fetch(path);
        if (!response.ok) {
            throw new Error(`Failed to fetch sample: ${path}`);
        }
        const arrayBuffer = await response.arrayBuffer();
        const audioBuffer = createAudioBuffer(arrayBuffer);
        players[sampleName] = new Tone.Player(audioBuffer).connect(drumsVolume);
    });
    await Promise.all(promises).catch(err => {
        console.error("Error loading samples:", err);
        throw err; // Re-throw to be caught by the outer try...catch
    });
}

function getDrumPattern() {
    const pattern = {};
    for (const sampleName of samples808) {
        pattern[sampleName] = [];
        for (let i = 0; i < 16; i++) {
            const step = document.querySelector(`#drum-matrix .step[data-track='${sampleName}'][data-step='${i}']`);
            pattern[sampleName][i] = step.classList.contains('active') ? 1 : 0;
        }
    }
    return pattern;
}

function applyDrumPattern(pattern) {
    for (const sampleName of samples808) {
        for (let i = 0; i < 16; i++) {
            const step = document.querySelector(`#drum-matrix .step[data-track='${sampleName}'][data-step='${i}']`);
            if (step) {
                step.classList.toggle('active', pattern[sampleName]?.[i] === 1);
            }
        }
    }
}

function createDrumSequencerGrid() {
    const container = document.getElementById('drum-matrix');
    container.innerHTML = '';
    for (const sampleName of samples808) {
        const track = document.createElement('div');
        track.classList.add('track');
        const label = document.createElement('div');
        label.classList.add('track-label');
        label.textContent = sampleName.replace('.raw', '').replace('808', '');
        track.appendChild(label);
        for (let i = 0; i < 16; i++) {
            const step = document.createElement('div');
            step.classList.add('step');
            step.dataset.step = i;
            step.dataset.track = sampleName;
            step.addEventListener('click', () => {
                if (isInstrumentClaimedByOther('drums')) return;
                step.classList.toggle('active');
                broadcast({ type: 'drum_pattern', pattern: getDrumPattern() });
            });
            track.appendChild(step);
        }
        container.appendChild(track);
    }
}

function randomizeDrumPattern() {
    if (isInstrumentClaimedByOther('drums')) return;
    const template = drumTemplates[Math.floor(Math.random() * drumTemplates.length)].pattern;
    applyDrumPattern(template);
    broadcast({ type: 'drum_pattern', pattern: getDrumPattern() });
}

function clearDrumPattern() {
    if (isInstrumentClaimedByOther('drums')) return;
    document.querySelectorAll('#drum-matrix .step').forEach(step => step.classList.remove('active'));
    broadcast({ type: 'drum_pattern', pattern: getDrumPattern() });
}

function setupDrumSequencer() {
    drumSequence = new Tone.Sequence((time, col) => {
        document.querySelectorAll(`#drum-matrix .step[data-step='${col}'].active`).forEach(step => {
            players[step.dataset.track]?.start(time);
        });
        document.querySelectorAll('#drum-matrix .step').forEach(step => {
            step.classList.toggle('playing', step.dataset.step == col);
        });
    }, Array.from(Array(16).keys()), '16n').start(0);
}

// --- Synthesizer (Piano Roll) ---
function createSynths() {
    for (let i = 0; i < 4; i++) {
        synthVolumes[i] = new Tone.Volume(0).toDestination();
        synthDistortions[i] = new Tone.Distortion(0.4);
        synthDelays[i] = new Tone.FeedbackDelay("8n", 0.5);
        synths[i] = new Tone.MonoSynth({
            oscillator: { type: "sawtooth" },
            envelope: { attack: 0.01, decay: 0.1, sustain: 0.2, release: 0.1 },
            filterEnvelope: { attack: 0.02, decay: 0.2, sustain: 0.5, release: 0.2, baseFrequency: 200, octaves: 4 }
        });
        synths[i].chain(synthDistortions[i], synthDelays[i], synthVolumes[i]);
    }
}

function randomizeSynthPattern(synthIndex) {
    const scale = Harmony.scales[document.getElementById('scale-select').value];
    const progression = Harmony.progressions[document.getElementById('progression-select').value];
    const key = Harmony.notes.indexOf(document.getElementById('key-select').value);
    const melody = MelodyGenerator.generateMelody(progression, scale, 16);
    applySynthPattern(synthIndex, melody);
}

function applySynthPattern(synthIndex, pattern) {
    synthPatterns[synthIndex] = pattern.map(noteValue => {
        const isPaused = noteValue === -1 || noteValue === null;

        let absoluteNote;
        if (isPaused) {
            absoluteNote = 48; // Default to C4 for paused notes
        } else if (typeof noteValue === 'string') {
            const noteNameMatch = noteValue.match(/[A-G]#?/);
            const octaveMatch = noteValue.match(/\d+$/);
            if (!noteNameMatch || !octaveMatch) {
                 absoluteNote = 48;
            } else {
                const noteName = noteNameMatch[0];
                const octave = parseInt(octaveMatch[0], 10);
                const noteIndex = Harmony.notes.indexOf(noteName);
                if (noteIndex === -1) {
                    absoluteNote = 48;
                } else {
                    absoluteNote = octave * 12 + noteIndex;
                }
            }
        } else {
            absoluteNote = noteValue;
        }

        const noteName = Harmony.notes[((absoluteNote % 12) + 12) % 12];
        const octave = Math.floor(absoluteNote / 12);
        const accent = Math.random() > 0.8;
        const slide = Math.random() > 0.9;
        return { note: `${noteName}${octave}`, accent: accent, slide: slide, pause: isPaused };
    });
    createSynthSequencerGrid(synthIndex);
    broadcast({ type: 'synth_pattern', synthIndex: synthIndex, pattern: synthPatterns[synthIndex] });
}

function clearSynthPattern(synthIndex) {
    const pattern = Array(16).fill(-1);
    applySynthPattern(synthIndex, pattern);
}

function shiftPattern(synthIndex, amount) {
    for (let i = 0; i < synthPatterns[synthIndex].length; i++) {
        const stepData = synthPatterns[synthIndex][i];
        if (stepData && !stepData.pause) {
            const noteNameWithOctave = stepData.note;
            const noteNameMatch = noteNameWithOctave.match(/[A-G]#?/);
            const octaveMatch = noteNameWithOctave.match(/\d+$/);

            if (!noteNameMatch || !octaveMatch) continue;

            const noteName = noteNameMatch[0];
            const octave = parseInt(octaveMatch[0]);
            const noteIndex = notes.indexOf(noteName);

            if (noteIndex === -1) continue;

            const absoluteNote = octave * 12 + noteIndex;
            const newAbsoluteNote = absoluteNote + amount;
            const newOctave = Math.floor(newAbsoluteNote / 12);
            const newNoteIndex = ((newAbsoluteNote % 12) + 12) % 12;
            const newNoteName = notes[newNoteIndex];

            if (newOctave >= 0 && newOctave < octaves) {
                stepData.note = `${newNoteName}${newOctave}`;
            }
        }
    }
    createSynthSequencerGrid(synthIndex);
    broadcast({ type: 'synth_pattern', synthIndex: synthIndex, pattern: synthPatterns[synthIndex] });
}

function drawSlideLines(synthIndex) {
    if (!slideCanvas) return;
    const grid = document.querySelector('.piano-roll-grid');
    if (!grid) return;

    slideCanvas.width = grid.scrollWidth;
    slideCanvas.height = grid.scrollHeight;
    slideCtx.clearRect(0, 0, slideCanvas.width, slideCanvas.height);

    const pattern = synthPatterns[synthIndex];
    if (!pattern) return;

    const octaveOffset = octaveOffsets[synthIndex];
    const cellHeight = grid.scrollHeight / (displayOctaves * 12);
    const cellWidth = grid.scrollWidth / 16;
    const startOctave = 4 + octaveOffset;
    const highestOctave = startOctave + displayOctaves - 1;

    for (let i = 0; i < pattern.length; i++) {
        const stepData = pattern[i];
        if (!stepData || stepData.pause || !stepData.slide) {
            continue;
        }

        const noteNameMatch1 = stepData.note.match(/[A-G]#?/);
        const octaveMatch1 = stepData.note.match(/\d+$/);
        if (!noteNameMatch1 || !octaveMatch1) continue;

        const noteName1 = noteNameMatch1[0];
        const octave1 = parseInt(octaveMatch1[0], 10);
        const noteIndex1 = notes.indexOf(noteName1);

        if (octave1 < startOctave || octave1 > highestOctave) {
            continue; // Don't draw slides starting from off-screen notes
        }

        const noteRow1 = (highestOctave - octave1) * 12 + (11 - noteIndex1);
        let x1 = i * cellWidth + cellWidth / 2;
        let y1 = noteRow1 * cellHeight + cellHeight / 2;

        // Apply the final vertical offset for the start of the slide
        y1 += 54 * cellHeight;

        // The end of the slide should connect directly to the next note without offset

        const nextStepData = pattern[(i + 1) % 16];
        if (!nextStepData) continue;

        const noteNameMatch2 = nextStepData.note.match(/[A-G]#?/);
        const octaveMatch2 = nextStepData.note.match(/\d+$/);
        if (!noteNameMatch2 || !octaveMatch2) continue;

        const noteName2 = noteNameMatch2[0];
        const octave2 = parseInt(octaveMatch2[0], 10);
        const noteIndex2 = notes.indexOf(noteName2);

        const noteRow2 = (highestOctave - octave2) * 12 + (11 - noteIndex2);
        const x2 = ((i + 1) % 16) * cellWidth + cellWidth / 2;
        const y2 = noteRow2 * cellHeight + cellHeight / 2;

        // Create a "fancy" gradient for the fill
        const gradient = slideCtx.createLinearGradient(x1, y1, x2, y2);
        gradient.addColorStop(0, 'rgba(255, 255, 255, 0.6)');
        gradient.addColorStop(1, 'rgba(255, 255, 255, 0.1)');
        slideCtx.fillStyle = gradient;
        slideCtx.strokeStyle = 'rgba(255, 255, 255, 0.8)';
        slideCtx.lineWidth = 1;

        // Draw a curved shape for the slide
        const width = cellWidth / 2;
        const controlX = (x1 + x2) / 2;
        const controlY = (y1 + y2) / 2 + (x2 - x1) * 0.3; // Control point for a nice arc

        slideCtx.beginPath();
        slideCtx.moveTo(x1 - width / 2, y1);
        slideCtx.quadraticCurveTo(controlX, controlY, x2 - width / 2, y2);
        slideCtx.lineTo(x2 + width / 2, y2);
        slideCtx.quadraticCurveTo(controlX, controlY, x1 + width / 2, y1);
        slideCtx.closePath();
        slideCtx.fill();
        slideCtx.stroke();
    }
}

function createSynthSequencerGrid(synthIndex) {
    const gridContainer = document.querySelector('.piano-roll-grid');
    const keyboardContainer = document.querySelector('.keyboard');
    const gridContainerParent = document.querySelector('.piano-roll-grid-container');

    gridContainer.innerHTML = '<canvas id="slide-canvas"></canvas>';
    slideCanvas = document.getElementById('slide-canvas');
    if (slideCanvas) {
        slideCtx = slideCanvas.getContext('2d');
    }
    keyboardContainer.innerHTML = '';
    const octaveOffset = octaveOffsets[synthIndex];
    const startOctave = 4 + octaveOffset;

    for (let octave = startOctave + displayOctaves - 1; octave >= startOctave; octave--) {
        if (octave < 0 || octave >= octaves) continue;
        for (const note of notes.slice().reverse()) {
            const noteName = `${note}${octave}`;
            const key = document.createElement('div');
            key.classList.add('key');
            if (note.includes('#')) key.classList.add('black');
            key.textContent = noteName;
            keyboardContainer.appendChild(key);
            for (let step = 0; step < 16; step++) {
                const cell = document.createElement('div');
                cell.classList.add('step');
                cell.dataset.note = noteName;
                cell.dataset.step = step;
                const stepData = synthPatterns[synthIndex][step];
                if (stepData && !stepData.pause && stepData.note === noteName) {
                    cell.classList.add('active');
                    if (stepData.accent) cell.classList.add('accent');
                    if (stepData.slide) cell.classList.add('slide');
                }
                cell.addEventListener('click', () => {
                    if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
                    const stepData = synthPatterns[synthIndex][step];

                    if (stepData.pause) {
                        // Step was paused. Create a new note.
                        for (let o = octaves - 1; o >= 0; o--) {
                            for (const n of notes.slice().reverse()) {
                                const otherNoteName = `${n}${o}`;
                                const otherCell = document.querySelector(`.step[data-note='${otherNoteName}'][data-step='${step}']`);
                                if (otherCell) otherCell.classList.remove('active', 'accent', 'slide');
                            }
                        }
                        stepData.note = noteName;
                        stepData.pause = false;
                        stepData.accent = false;
                        stepData.slide = false;
                        cell.classList.add('active');
                    } else {
                        // Step was not paused.
                        if (stepData.note === noteName) {
                            // Clicked on the active note. Cycle it.
                            if (!stepData.accent && !stepData.slide) {
                                stepData.accent = true;
                                cell.classList.add('accent');
                            } else if (stepData.accent && !stepData.slide) {
                                stepData.accent = false;
                                stepData.slide = true;
                                cell.classList.remove('accent');
                                cell.classList.add('slide');
                            } else if (!stepData.accent && stepData.slide) {
                                stepData.accent = true;
                                cell.classList.add('accent');
                            } else {
                                stepData.pause = true;
                                stepData.accent = false;
                                stepData.slide = false;
                                cell.classList.remove('active', 'accent', 'slide');
                            }
                        } else {
                            // Clicked on a different note in the same step. Move the note.
                            const oldCell = document.querySelector(`.step.active[data-step='${step}']`);
                            if (oldCell) oldCell.classList.remove('active', 'accent', 'slide');

                            stepData.note = noteName;
                            stepData.accent = false;
                            stepData.slide = false;
                            cell.classList.add('active');
                        }
                    }
                    broadcast({ type: 'synth_pattern', synthIndex: synthIndex, pattern: synthPatterns[synthIndex] });
                    drawSlideLines(synthIndex);
                });
                gridContainer.appendChild(cell);
            }
        }
    }
}

function setupSynthSequencers() {
    for (let i = 0; i < 4; i++) {
        synthSequences[i] = new Tone.Sequence((time, step) => {
            const stepData = synthPatterns[i][step];
            if (stepData && !stepData.pause) {
                synths[i].portamento = stepData.slide ? 0.05 : 0;
                const velocity = stepData.accent ? 1.0 : 0.7;
                synths[i].triggerAttackRelease(stepData.note, "16n", time, velocity);
            }

            if (activeView === `synth${i}`) {
                document.querySelectorAll('.piano-roll-grid .step').forEach(cell => {
                    cell.classList.toggle('playing', cell.dataset.step == step);
                });
            }
        }, Array.from(Array(16).keys()), '16n').start(0);
    }
}

function setupKnobs() {
    document.getElementById('bpm-knob').addEventListener('input', e => {
        Tone.Transport.bpm.value = parseFloat(e.target.value);
        broadcast({ type: 'knob', id: 'bpm-knob', value: e.target.value });
    });
    document.getElementById('global-vol-knob').addEventListener('input', e => {
        Tone.Destination.volume.value = -40 + (parseFloat(e.target.value) / 100) * 40;
        broadcast({ type: 'knob', id: 'global-vol-knob', value: e.target.value });
    });

    for (let i = 0; i < 4; i++) {
        document.getElementById(`synth${i}-vol-knob`).addEventListener('input', e => {
            if (isInstrumentClaimedByOther(`synth${i}`)) return;
            synthVolumes[i].volume.value = -40 + (parseFloat(e.target.value) / 100) * 40;
            broadcast({ type: 'knob', id: `synth${i}-vol-knob`, value: e.target.value });
        });
    }
    document.getElementById('drums-vol-knob').addEventListener('input', e => {
        if (isInstrumentClaimedByOther('drums')) return;
        drumsVolume.volume.value = -40 + (parseFloat(e.target.value) / 100) * 40;
        broadcast({ type: 'knob', id: 'drums-vol-knob', value: e.target.value });
    });

    document.getElementById('knob-tune').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            synths[synthIndex].oscillator.detune.value = (parseFloat(e.target.value) - 64) * 100;
            broadcast({ type: 'knob', id: 'knob-tune', value: e.target.value });
        }
    });
    document.getElementById('knob-cutoff').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            synths[synthIndex].filter.frequency.value = (parseFloat(e.target.value) / 127) * 5000 + 200;
            broadcast({ type: 'knob', id: 'knob-cutoff', value: e.target.value });
        }
    });
    document.getElementById('knob-resonance').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            synths[synthIndex].filter.Q.value = (parseFloat(e.target.value) / 127) * 20;
            broadcast({ type: 'knob', id: 'knob-resonance', value: e.target.value });
        }
    });
    document.getElementById('knob-env-mod').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            synths[synthIndex].filterEnvelope.octaves = (parseFloat(e.target.value) / 127) * 10;
            broadcast({ type: 'knob', id: 'knob-env-mod', value: e.target.value });
        }
    });
    document.getElementById('knob-decay').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            const decay = (parseFloat(e.target.value) / 127) * 0.5 + 0.01;
            synths[synthIndex].filterEnvelope.decay = decay;
            synths[synthIndex].envelope.decay = decay;
            broadcast({ type: 'knob', id: 'knob-decay', value: e.target.value });
        }
    });
    document.getElementById('knob-accent').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            synths[synthIndex].filterEnvelope.sustain = 0.5 + (parseFloat(e.target.value) / 127) * 0.5;
            broadcast({ type: 'knob', id: 'knob-accent', value: e.target.value });
        }
    });

    document.getElementById('delay-time-knob').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            synthDelays[synthIndex].delayTime.value = (parseFloat(e.target.value) / 100);
            broadcast({ type: 'knob', id: 'delay-time-knob', value: e.target.value });
        }
    });

    document.getElementById('delay-feedback-knob').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            synthDelays[synthIndex].feedback.value = (parseFloat(e.target.value) / 100) * 0.9;
            broadcast({ type: 'knob', id: 'delay-feedback-knob', value: e.target.value });
        }
    });

    document.getElementById('knob-drive').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            synthDistortions[synthIndex].distortion = (parseFloat(e.target.value) / 127);
            broadcast({ type: 'knob', id: 'knob-drive', value: e.target.value });
        }
    });

    document.getElementById('knob-wave').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            const currentWave = synths[synthIndex].oscillator.type;
            synths[synthIndex].oscillator.type = currentWave === 'sawtooth' ? 'square' : 'sawtooth';
            broadcast({ type: 'knob', id: 'knob-wave', value: synths[synthIndex].oscillator.type });
        }
    });
}


// --- View Logic ---
function updateView() {
    const isSynthView = activeView.startsWith('synth');
    document.getElementById('drum-matrix').style.display = activeView === 'drums' ? 'flex' : 'none';
    document.getElementById('synth-sequence-matrix').style.display = isSynthView ? 'flex' : 'none';
    document.querySelector('.knobs-container').style.display = isSynthView ? 'block' : 'none';
    if (isSynthView) {
        const synthIndex = parseInt(activeView.replace('synth', ''));
        createSynthSequencerGrid(synthIndex);
    }
}

function populateSelects() {
    const keySelect = document.getElementById('key-select');
    Harmony.notes.forEach((note, i) => {
        const option = document.createElement('option');
        option.value = note;
        option.textContent = note;
        keySelect.appendChild(option);
    });

    const scaleSelect = document.getElementById('scale-select');
    Object.keys(Harmony.scales).forEach(scaleName => {
        const option = document.createElement('option');
        option.value = scaleName;
        option.textContent = scaleName;
        scaleSelect.appendChild(option);
    });

    const progressionSelect = document.getElementById('progression-select');
    Object.keys(Harmony.progressions).forEach(progName => {
        const option = document.createElement('option');
        option.value = progName;
        option.textContent = progName;
        progressionSelect.appendChild(option);
    });
}

// --- Song Management ---
function getAppState() {
    const drumPattern = getDrumPattern();
    const claimedInstruments = {};
    document.querySelectorAll('.instrument-claim').forEach(cb => {
        if (cb.checked) {
            claimedInstruments[cb.dataset.instrument] = true;
        }
    });
    return {
        drumPattern,
        synthPatterns,
        claimedInstruments,
    };
}

function loadAppState(state) {
    applyDrumPattern(state.drumPattern);
    for (let i = 0; i < 4; i++) {
        synthPatterns[i] = state.synthPatterns[i] || [];
        if (activeView === `synth${i}`) {
            createSynthSequencerGrid(i);
        }
    }
    if (state.claimedInstruments) {
        claimedInstruments = state.claimedInstruments;
        for (const instrument in state.claimedInstruments) {
            const checkbox = document.querySelector(`.instrument-claim[data-instrument='${instrument}']`);
            if (checkbox) {
                checkbox.disabled = true;
                checkbox.checked = true;
            }
            const button = document.getElementById(instrument);
            if (button) {
                button.disabled = true;
                button.classList.add('claimed');
            }
        }
    }
    checkAllInstrumentsAssigned();
}

function updateSongList() {
    const songListSelect = document.getElementById('song-list-select');
    songListSelect.innerHTML = '';
    const songs = Object.keys(localStorage);
    songs.forEach(songName => {
        if (songName.startsWith('acidjs_')) {
            const option = document.createElement('option');
            option.value = songName;
            option.textContent = songName.replace('acidjs_', '');
            songListSelect.appendChild(option);
        }
    });
}

function generateEuclideanRhythm(steps, pulses) {
    steps = Math.round(steps);
    pulses = Math.round(pulses);
    if(pulses > steps || pulses == 0 || steps == 0) {
        return new Array();
    }
    let pattern = [];
    let counts = [];
    let remainders = [];
    let divisor = steps - pulses;
    remainders.push(pulses);
    let level = 0;
    while(true) {
        counts.push(Math.floor(divisor / remainders[level]));
        remainders.push(divisor % remainders[level]);
        divisor = remainders[level];
        level += 1;
        if (remainders[level] <= 1) {
            break;
        }
    }
    counts.push(divisor);

    const build = function(level) {
        if (level > -1) {
            for (var i=0; i < counts[level]; i++) {
                build(level-1);
            }
            if (remainders[level] != 0) {
                build(level-2);
            }
        } else if (level == -1) {
            pattern.push(0);
        } else if (level == -2) {
            pattern.push(1);
        }
    };

    build(level);
    return pattern.reverse();
}

function isInstrumentClaimedByOther(instrument) {
    return claimedInstruments[instrument] && claimedInstruments[instrument] !== myPeerId;
}

function checkAllInstrumentsAssigned() {
    const playPauseButton = document.getElementById('play-pause-button');
    const allAssigned = Object.keys(claimedInstruments).length === 5;
    playPauseButton.disabled = !allAssigned;
}

function updatePlayPauseButton() {
    const playPauseButton = document.getElementById('play-pause-button');
    if (Tone.Transport.state === 'started') {
        playPauseButton.textContent = '||';
    } else {
        playPauseButton.textContent = '▶';
    }
}

function initPeer() {
    try {
        peer = new Peer();

        peer.on('open', id => {
            myPeerId = id;
            console.log('My peer ID is: ' + myPeerId);
        });

        peer.on('error', err => {
            console.error('PeerJS error:', err);
            alert(`PeerJS error: ${err.type}`);
            if (err.type === 'peer-unavailable') {
                const joinButton = document.getElementById('join-room-button');
                if (joinButton) {
                    joinButton.textContent = 'Join Room';
                    joinButton.disabled = false;
                }
                const roomIdInput = document.getElementById('room-id-input');
                if (roomIdInput) {
                    roomIdInput.disabled = false;
                }
            }
        });

        peer.on('disconnected', () => {
            console.log('Peer disconnected from server. Attempting to reconnect...');
            alert('Connection to the peer server has been lost. Attempting to reconnect...');
            peer.reconnect();
        });

        peer.on('connection', conn => {
            console.log(`Incoming connection from ${conn.peer}`);
            setupConnection(conn);
        });

        document.getElementById('create-room-button').addEventListener('click', () => {
            if (!myPeerId) {
                alert('Still connecting to the peer server. Please wait a moment.');
                return;
            }
            hostId = myPeerId;
            document.getElementById('room-id-display').textContent = `Room ID: ${myPeerId}`;
            document.getElementById('join-room-button').disabled = true;
            document.getElementById('room-id-input').disabled = true;
        });

        document.getElementById('join-room-button').addEventListener('click', () => {
            if (!myPeerId) {
                alert('Still connecting to the peer server. Please wait a moment.');
                return;
            }
            const roomIdInput = document.getElementById('room-id-input');
            const roomId = roomIdInput.value;
            if (!roomId) {
                alert('Please enter a Room ID.');
                return;
            }

            const joinButton = document.getElementById('join-room-button');
            joinButton.textContent = 'Connecting...';
            joinButton.disabled = true;
            roomIdInput.disabled = true;

            const conn = peer.connect(roomId);
            hostId = roomId;
            setupConnection(conn);
        });
    } catch (e) {
        console.error('Failed to initialize PeerJS:', e);
        alert('Failed to initialize multiplayer functionality. PeerJS might be blocked or unavailable.');
    }
}

function setupConnection(conn) {
    conns.push(conn);

    conn.on('data', data => {
        console.log('Received data:', data);
        if (data.type === 'state') {
            loadAppState(data.state);
        } else if (data.type === 'drum_pattern') {
            applyDrumPattern(data.pattern);
        } else if (data.type === 'synth_pattern') {
            applySynthPattern(data.synthIndex, data.pattern);
        } else if (data.type === 'knob') {
            const knob = document.getElementById(data.id);
            if (knob) {
                knob.value = data.value;
                knob.dispatchEvent(new Event('input'));
            }
        } else if (data.type === 'play_pause') {
            if (Tone.Transport.state !== data.state) {
                if (data.state === 'started') {
                    Tone.Transport.start();
                } else {
                    Tone.Transport.pause();
                }
                    updatePlayPauseButton();
            }
        } else if (data.type === 'instrument_claim') {
            claimedInstruments[data.instrument] = conn.peer;
            const button = document.getElementById(data.instrument);
            if (button) {
                button.disabled = true;
                button.classList.add('claimed');
            }
            const checkbox = document.querySelector(`.instrument-claim[data-instrument='${data.instrument}']`);
            if (checkbox) {
                checkbox.disabled = true;
                checkbox.checked = true;
            }
            checkAllInstrumentsAssigned();
        } else if (data.type === 'instrument_unclaim') {
            delete claimedInstruments[data.instrument];
            const checkbox = document.querySelector(`.instrument-claim[data-instrument='${data.instrument}']`);
            if (checkbox) {
                checkbox.disabled = false;
                checkbox.checked = false;
            }
            const button = document.getElementById(data.instrument);
            if (button) {
                button.disabled = false;
                button.classList.remove('claimed');
            }
            checkAllInstrumentsAssigned();
        } else if (data.type === 'track_select') {
            activeView = data.track;
            updateView();
            document.querySelectorAll('.track-selector').forEach(b => b.classList.remove('selected'));
            document.getElementById(activeView)?.classList.add('selected');
        }
    });

    conn.on('open', () => {
        console.log(`Connection to ${conn.peer} opened.`);
        const joinButton = document.getElementById('join-room-button');
        if (joinButton) {
            joinButton.textContent = 'Connected';
        }

        if (myPeerId === hostId) {
            const state = getAppState();
            conn.send({ type: 'state', state });
        }
    });

    conn.on('error', err => {
        console.error('Connection error:', err);
        alert(`Failed to connect: ${err.type}`);
        const joinButton = document.getElementById('join-room-button');
        if (joinButton) {
            joinButton.textContent = 'Join Room';
            joinButton.disabled = false;
        }
        const roomIdInput = document.getElementById('room-id-input');
        if (roomIdInput) {
            roomIdInput.disabled = false;
        }
    });
}

function broadcast(data) {
    conns.forEach(conn => {
        conn.send(data);
    });
}

// --- Main Init ---
async function init() {
    populateSelects();
    await loadSamples();
    createDrumSequencerGrid();
    randomizeDrumPattern();
    setupDrumSequencer();
    createSynths();
    for (let i=0; i<4; i++) {
        randomizeSynthPattern(i);
    }
    setupSynthSequencers();
    setupKnobs();
    updateView();
    updateSongList();

    document.getElementById('randomize-button').addEventListener('click', () => {
        if (activeView === 'drums') {
            if (isInstrumentClaimedByOther('drums')) return;
            randomizeDrumPattern();
        } else if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            randomizeSynthPattern(synthIndex);
        }
    });

    updatePlayPauseButton();

    document.getElementById('play-pause-button').addEventListener('click', () => {
        if (Tone.Transport.state === 'started') {
            Tone.Transport.pause();
            broadcast({ type: 'play_pause', state: 'paused' });
        } else {
            Tone.Transport.start();
            broadcast({ type: 'play_pause', state: 'started' });
        }
        updatePlayPauseButton();
    });

    document.querySelectorAll('.track-selector').forEach(button => {
        let pressTimer;
        button.addEventListener('mousedown', () => {
            pressTimer = setTimeout(() => {
                // Long press
                const trackId = button.id;
                muteStates[trackId] = !muteStates[trackId];
                if (trackId === 'drums') {
                    drumsVolume.mute = muteStates[trackId];
                } else if (trackId.startsWith('synth')) {
                    const synthIndex = parseInt(trackId.replace('synth', ''));
                    synthVolumes[synthIndex].mute = muteStates[trackId];
                }
                button.classList.toggle('muted', muteStates[trackId]);
            }, 500);
        });
        button.addEventListener('mouseup', () => {
            clearTimeout(pressTimer);
        });
        button.addEventListener('click', () => {
             activeView = button.id;
             updateView();
             document.querySelectorAll('.track-selector').forEach(b => b.classList.remove('selected'));
             button.classList.add('selected');
             broadcast({ type: 'track_select', track: activeView });
        });
    });

    document.getElementById('gen-mel-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            const scale = Harmony.scales[document.getElementById('scale-select').value];
            const progression = Harmony.progressions[document.getElementById('progression-select').value];
            const melody = MelodyGenerator.generateMelody(progression, scale, 16);
            applySynthPattern(synthIndex, melody);
        }
    });

    document.getElementById('gen-bass-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            const scale = Harmony.scales[document.getElementById('scale-select').value];
            const progression = Harmony.progressions[document.getElementById('progression-select').value];
            const bassline = MelodyGenerator.generateBassline(progression, scale, 16);
            applySynthPattern(synthIndex, bassline);
        }
    });

    document.getElementById('harmonize-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            const melody = synthPatterns[synthIndex].map(step => step ? step.note : -1);
            const scale = Harmony.scales[document.getElementById('scale-select').value];
            const progression = Harmony.progressions[document.getElementById('progression-select').value];
            const harmony = Harmony.generateHarmony(melody, progression, scale);
            for (let i = 0; i < 4; i++) {
                if (i !== synthIndex) {
                    if (isInstrumentClaimedByOther(`synth${i}`)) continue;
                    applySynthPattern(i, harmony);
                }
            }
        }
    });

    document.getElementById('mutate-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            const scale = Harmony.scales[document.getElementById('scale-select').value];
            const pattern = synthPatterns[synthIndex].map(step => step ? step.note : -1);
            const mutatedPattern = MelodyGenerator.mutatePattern(pattern, scale, 0.2);
            applySynthPattern(synthIndex, mutatedPattern);
        }
    });

    document.getElementById('mutate-rhythm-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            const pattern = synthPatterns[synthIndex].map(step => step ? step.note : -1);
            const mutatedPattern = MelodyGenerator.mutateRhythm(pattern, 0.2);
            applySynthPattern(synthIndex, mutatedPattern);
        }
    });

    document.getElementById('mutate-accents-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            synthPatterns[synthIndex] = MelodyGenerator.mutateAccents(synthPatterns[synthIndex], 0.2);
            createSynthSequencerGrid(synthIndex);
            broadcast({ type: 'synth_pattern', synthIndex: synthIndex, pattern: synthPatterns[synthIndex] });
        }
    });

    document.getElementById('arpeggiate-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            const pattern = synthPatterns[synthIndex];
            const direction = document.getElementById('arpeggiate-direction-select').value;
            const arpeggiatedPattern = MelodyGenerator.arpeggiate(pattern, 1, direction);
            applySynthPattern(synthIndex, arpeggiatedPattern);
        }
    });

    document.getElementById('mutate-slides-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            synthPatterns[synthIndex] = MelodyGenerator.mutateSlides(synthPatterns[synthIndex], 0.2);
            createSynthSequencerGrid(synthIndex);
            broadcast({ type: 'synth_pattern', synthIndex: synthIndex, pattern: synthPatterns[synthIndex] });
        }
    });

    document.getElementById('clear-synth-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            clearSynthPattern(synthIndex);
        }
    });
    document.getElementById('clear-drums-button').addEventListener('click', () => {
        if (isInstrumentClaimedByOther('drums')) return;
        clearDrumPattern();
    });

    document.getElementById('gen-drums-button').addEventListener('click', () => {
        if (activeView === 'drums') {
            if (isInstrumentClaimedByOther('drums')) return;
            randomizeDrumPattern();
        }
    });

    document.getElementById('gen-euclidean-button').addEventListener('click', () => {
        if (activeView === 'drums') {
            if (isInstrumentClaimedByOther('drums')) return;
            const pattern = {
                '808bd.raw': generateEuclideanRhythm(16, 4),
                '808sd_base.raw': generateEuclideanRhythm(16, 4),
                '808ch.raw': generateEuclideanRhythm(16, 8),
                '808oh.raw': generateEuclideanRhythm(16, 2),
            };
            applyDrumPattern(pattern);
            broadcast({ type: 'drum_pattern', pattern: pattern });
        }
    });

    document.getElementById('up-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            shiftPattern(synthIndex, 1);
        }
    });
    document.getElementById('down-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            shiftPattern(synthIndex, -1);
        }
    });
    document.getElementById('oct-up-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            shiftPattern(synthIndex, 12);
        }
    });
    document.getElementById('oct-down-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            shiftPattern(synthIndex, -12);
        }
    });

    document.getElementById('seq-oct-up-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            octaveOffsets[synthIndex]++;
            createSynthSequencerGrid(synthIndex);
        }
    });

    document.getElementById('seq-oct-down-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            if (isInstrumentClaimedByOther(`synth${synthIndex}`)) return;
            octaveOffsets[synthIndex]--;
            createSynthSequencerGrid(synthIndex);
        }
    });

    document.getElementById('save-song-button').addEventListener('click', () => {
        const songName = prompt("Enter song name:");
        if (songName) {
            const state = getAppState();
            localStorage.setItem(`acidjs_${songName}`, JSON.stringify(state));
            updateSongList();
        }
    });

    document.getElementById('load-song-button').addEventListener('click', () => {
        const songListSelect = document.getElementById('song-list-select');
        const songName = songListSelect.value;
        if (songName) {
            const stateString = localStorage.getItem(songName);
            if (stateString) {
                const state = JSON.parse(stateString);
                loadAppState(state);
            }
        }
    });

    document.getElementById('delete-song-button').addEventListener('click', () => {
        const songListSelect = document.getElementById('song-list-select');
        const songName = songListSelect.value;
        if (songName) {
            localStorage.removeItem(songName);
            updateSongList();
        }
    });

    document.getElementById('random-progression-button').addEventListener('click', () => {
        const progressionSelect = document.getElementById('progression-select');
        const randomIndex = Math.floor(Math.random() * progressionSelect.options.length);
        progressionSelect.selectedIndex = randomIndex;
    });

    document.querySelectorAll('.instrument-claim').forEach(checkbox => {
        checkbox.addEventListener('change', e => {
            const instrument = e.target.dataset.instrument;
            if (e.target.checked) {
                broadcast({ type: 'instrument_claim', instrument: instrument });
                claimedInstruments[instrument] = myPeerId;
            } else {
                broadcast({ type: 'instrument_unclaim', instrument: instrument });
                delete claimedInstruments[instrument];
            }
            checkAllInstrumentsAssigned();
        });
    });
    checkAllInstrumentsAssigned();
}

document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('start-button-container');
    const startButton = document.createElement('button');
    startButton.textContent = 'Start Audio';
    container.appendChild(startButton);
    startButton.addEventListener('click', async () => {
        try {
            await Tone.start();
            startButton.textContent = 'Loading...';
            initPeer();
            await init();
            container.style.display = 'none';
        } catch (e) {
            console.error('Error during initialization:', e);
            alert(`An error occurred during startup: ${e.message}`);
        }
    });
});
