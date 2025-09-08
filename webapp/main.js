console.log("main.js loaded");

const sampleBasePath = 'assets/';
const samples808 = [
    '808bd.raw',
    '808sd_base.raw',
    '808ch.raw',
    '808oh.raw',
    '808cp.raw',
    '808cb.raw',
    '808hc.raw',
    '808mc.raw',
    '808crash.raw'
];

const players = {};
let drumSequence;
let synth;
let synthSequence;
let synthPattern = [];
let activeView = 'drums';

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
    const promises = samples808.map(async (sampleName) => {
        const path = sampleBasePath + sampleName;
        const response = await fetch(path);
        const arrayBuffer = await response.arrayBuffer();
        const audioBuffer = createAudioBuffer(arrayBuffer);
        players[sampleName] = new Tone.Player(audioBuffer).toDestination();
    });
    await Promise.all(promises);
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
            step.addEventListener('click', () => step.classList.toggle('active'));
            track.appendChild(step);
        }
        container.appendChild(track);
    }
}

function randomizeDrumPattern() {
    document.querySelectorAll('#drum-matrix .step').forEach(step => {
        step.classList.toggle('active', Math.random() > 0.8);
    });
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

// --- Synthesizer ---
function createSynth() {
    const distortion = new Tone.Distortion(0.4).toDestination();
    synth = new Tone.MonoSynth({
        oscillator: { type: "sawtooth" },
        envelope: { attack: 0.01, decay: 0.1, sustain: 0.2, release: 0.1 },
        filterEnvelope: { attack: 0.02, decay: 0.2, sustain: 0.5, release: 0.2, baseFrequency: 200, octaves: 4 }
    }).connect(distortion);
}

function randomizeSynthPattern() {
    const notes = ["C2", "D2", "E2", "F2", "G2", "A2", "B2", "C3"];
    synthPattern = Array.from({ length: 16 }, () =>
        Math.random() > 0.5
            ? { note: notes[Math.floor(Math.random() * notes.length)], accent: Math.random() > 0.8, slide: Math.random() > 0.9 }
            : null
    );
    createSynthSequencerGrid();
}

function createSynthSequencerGrid() {
    const container = document.getElementById('synth-sequence-matrix');
    container.innerHTML = '';
    for (let i = 0; i < 16; i++) {
        const stepButton = document.createElement('button');
        stepButton.classList.add('step');
        const stepData = synthPattern[i];
        if (stepData) {
            stepButton.textContent = stepData.note;
            stepButton.classList.add('active');
            if (stepData.accent) stepButton.classList.add('accent');
            if (stepData.slide) stepButton.classList.add('slide');
        }
        container.appendChild(stepButton);
    }
}

function setupSynthSequencer() {
    synthSequence = new Tone.Sequence((time, step) => {
        const stepData = synthPattern[step];
        if (stepData) {
            synth.portamento = stepData.slide ? 0.05 : 0;
            const velocity = stepData.accent ? 1.0 : 0.7;
            synth.triggerAttackRelease(stepData.note, "16n", time, velocity);
        }
        document.querySelectorAll('#synth-sequence-matrix .step').forEach((s, index) => {
            s.classList.toggle('playing', index === step);
        });
    }, Array.from(Array(16).keys()), '16n').start(0);
}

function setupKnobs() {
    document.getElementById('knob-tune').addEventListener('input', e => synth.oscillator.detune.value = (e.target.value - 64) * 100);
    document.getElementById('knob-cutoff').addEventListener('input', e => synth.filter.frequency.value = (e.target.value / 127) * 5000 + 200);
    document.getElementById('knob-resonance').addEventListener('input', e => synth.filter.Q.value = (e.target.value / 127) * 20);
    document.getElementById('knob-env-mod').addEventListener('input', e => synth.filterEnvelope.octaves = (e.target.value / 127) * 10);
    document.getElementById('knob-decay').addEventListener('input', e => {
        const decayValue = (e.target.value / 127) * 0.5 + 0.01;
        synth.filterEnvelope.decay = decayValue;
        synth.envelope.decay = decayValue;
    });
    document.getElementById('knob-accent').addEventListener('input', e => synth.filterEnvelope.sustain = 0.5 + (e.target.value / 127) * 0.5);
}

// --- View Logic ---
function updateView() {
    document.getElementById('drum-matrix').style.display = activeView === 'drums' ? 'flex' : 'none';
    document.getElementById('synth-sequence-matrix').style.display = activeView === 'synth' ? 'flex' : 'none';
    document.querySelector('.knobs-container').style.display = activeView === 'synth' ? 'block' : 'none';
}

// --- Main Init ---
async function init() {
    await loadSamples();
    createDrumSequencerGrid();
    randomizeDrumPattern();
    setupDrumSequencer();
    createSynth();
    randomizeSynthPattern();
    createSynthSequencerGrid();
    setupSynthSequencer();
    setupKnobs();
    updateView(); // Set initial view

    document.getElementById('randomize-button').addEventListener('click', () => {
        if (activeView === 'drums') {
            randomizeDrumPattern();
        } else {
            randomizeSynthPattern();
        }
    });

    document.getElementById('play-pause-button').addEventListener('click', () => {
        if (Tone.Transport.state === 'started') {
            Tone.Transport.pause();
        } else {
            Tone.Transport.start();
        }
    });

    document.querySelectorAll('.track-selector').forEach(button => {
        button.addEventListener('click', () => {
            if (button.id.startsWith('synth')) {
                activeView = 'synth';
            } else {
                activeView = 'drums';
            }
            updateView();
        });
    });
}

document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('start-button-container');
    const startButton = document.createElement('button');
    startButton.textContent = 'Start Audio';
    container.appendChild(startButton);
    startButton.addEventListener('click', async () => {
        await Tone.start();
        startButton.textContent = 'Loading...';
        await init();
        container.style.display = 'none';
    });
});
