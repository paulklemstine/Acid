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

function createAudioBuffer(arrayBuffer) {
    console.log("createAudioBuffer: starting");
    const audioContext = Tone.context.rawContext;
    const int16s = new Int16Array(arrayBuffer);
    const float32s = new Float32Array(int16s.length);

    for (let i = 0; i < int16s.length; i++) {
        float32s[i] = int16s[i] / 32768;
    }

    const audioBuffer = audioContext.createBuffer(
        2,
        float32s.length / 2,
        44100
    );

    const left = audioBuffer.getChannelData(0);
    const right = audioBuffer.getChannelData(1);
    for (let i = 0; i < float32s.length / 2; i++) {
        left[i] = float32s[i * 2];
        right[i] = float32s[i * 2 + 1];
    }
    console.log("createAudioBuffer: finished");
    return audioBuffer;
}

async function loadSamples() {
    console.log('loadSamples: starting');
    const promises = samples808.map(async (sampleName) => {
        console.log(`loadSamples: fetching ${sampleName}`);
        const path = sampleBasePath + sampleName;
        const response = await fetch(path);
        const arrayBuffer = await response.arrayBuffer();
        console.log(`loadSamples: fetched ${sampleName}`);
        const audioBuffer = createAudioBuffer(arrayBuffer);
        players[sampleName] = new Tone.Player(audioBuffer).toDestination();
        console.log(`loadSamples: loaded ${sampleName}`);
    });
    await Promise.all(promises);
    console.log('loadSamples: finished');
}

function createSynth() {
    console.log("createSynth: starting");
    const distortion = new Tone.Distortion(0.4).toDestination();
    synth = new Tone.MonoSynth({
        oscillator: {
            type: "sawtooth"
        },
        envelope: {
            attack: 0.01,
            decay: 0.1,
            sustain: 0.2,
            release: 0.1,
        },
        filterEnvelope: {
            attack: 0.02,
            decay: 0.2,
            sustain: 0.5,
            release: 0.2,
            baseFrequency: 200,
            octaves: 4,
        }
    }).connect(distortion);
    console.log("createSynth: finished");
}

function randomizeSynthPattern() {
    console.log("randomizeSynthPattern: starting");
    const notes = ["C2", "D2", "E2", "F2", "G2", "A2", "B2", "C3"];
    synthPattern = [];
    for (let i = 0; i < 16; i++) {
        if (Math.random() > 0.5) {
            synthPattern.push({
                note: notes[Math.floor(Math.random() * notes.length)],
                accent: Math.random() > 0.8,
                slide: Math.random() > 0.9,
            });
        } else {
            synthPattern.push(null);
        }
    }
    createSynthSequencerGrid();
    console.log("randomizeSynthPattern: finished");
}

function createSynthSequencerGrid() {
    console.log("createSynthSequencerGrid: starting");
    const container = document.getElementById('synth-sequence-matrix');
    if (!container) {
        console.error("createSynthSequencerGrid: synth-sequence-matrix not found!");
        return;
    }
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
    console.log("createSynthSequencerGrid: finished");
}

function setupSynthSequencer() {
    console.log("setupSynthSequencer: starting");
    synthSequence = new Tone.Sequence((time, step) => {
        const stepData = synthPattern[step];
        if (stepData) {
            synth.portamento = stepData.slide ? 0.05 : 0;
            const velocity = stepData.accent ? 1.0 : 0.7;
            synth.triggerAttackRelease(stepData.note, "16n", time, velocity);
        }

        const allSteps = document.querySelectorAll('#synth-sequence-matrix .step');
        allSteps.forEach((s, index) => {
            if (index === step) {
                s.classList.add('playing');
            } else {
                s.classList.remove('playing');
            }
        });
    }, [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15], '16n').start(0);
    console.log("setupSynthSequencer: finished");
}

function setupKnobs() {
    console.log("setupKnobs: starting");
    document.getElementById('knob-tune').addEventListener('input', e => {
        const detuneValue = (e.target.value - 64) * 100;
        synth.oscillator.detune.value = detuneValue;
    });
    document.getElementById('knob-cutoff').addEventListener('input', e => {
        const cutoffValue = (e.target.value / 127) * 5000 + 200;
        synth.filter.frequency.value = cutoffValue;
    });
    document.getElementById('knob-resonance').addEventListener('input', e => {
        const resonanceValue = (e.target.value / 127) * 20;
        synth.filter.Q.value = resonanceValue;
    });
    document.getElementById('knob-env-mod').addEventListener('input', e => {
        const envModValue = (e.target.value / 127) * 10;
        synth.filterEnvelope.octaves = envModValue;
    });
    document.getElementById('knob-decay').addEventListener('input', e => {
        const decayValue = (e.target.value / 127) * 0.5 + 0.01;
        synth.filterEnvelope.decay = decayValue;
        synth.envelope.decay = decayValue;
    });
    document.getElementById('knob-accent').addEventListener('input', e => {
        const accentValue = (e.target.value / 127);
        synth.filterEnvelope.sustain = 0.5 + accentValue * 0.5;
    });
    console.log("setupKnobs: finished");
}

async function init() {
    console.log("init: starting");
    createSynth();
    randomizeSynthPattern();
    setupSynthSequencer();
    setupKnobs();
    console.log("init: finished");

    console.log("init: setting up event listeners");
    document.getElementById('randomize-button').addEventListener('click', randomizeSynthPattern);

    const playPauseButton = document.getElementById('play-pause-button');
    playPauseButton.addEventListener('click', () => {
        console.log("playPauseButton: clicked, Tone.Transport.state:", Tone.Transport.state);
        if (Tone.Transport.state === 'started') {
            Tone.Transport.pause();
            playPauseButton.textContent = '>';
        } else {
            Tone.Transport.start();
            playPauseButton.textContent = '||';
        }
    });
    console.log("init: event listeners set up");
}

document.addEventListener('DOMContentLoaded', () => {
    console.log("DOMContentLoaded: event listener fired");
    const startButton = document.createElement('button');
    startButton.textContent = 'Start Audio';
    document.body.prepend(startButton);
    startButton.addEventListener('click', async () => {
        console.log("Start Audio button clicked");
        await Tone.start();
        console.log("Tone.start() resolved, Tone.context.state:", Tone.context.state);
        startButton.textContent = 'Loading...';
        await init();
        startButton.style.display = 'none';
    });
});
