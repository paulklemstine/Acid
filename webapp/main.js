console.log("main.js loaded");

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

const notes = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"];
const octaves = 8;

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
        const arrayBuffer = await response.arrayBuffer();
        const audioBuffer = createAudioBuffer(arrayBuffer);
        players[sampleName] = new Tone.Player(audioBuffer).connect(drumsVolume);
    });
    await Promise.all(promises);
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
            step.addEventListener('click', () => step.classList.toggle('active'));
            track.appendChild(step);
        }
        container.appendChild(track);
    }
}

function randomizeDrumPattern() {
    const template = drumTemplates[Math.floor(Math.random() * drumTemplates.length)].pattern;
    applyDrumPattern(template);
}

function clearDrumPattern() {
    document.querySelectorAll('#drum-matrix .step').forEach(step => step.classList.remove('active'));
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
        synthDelays[i] = new Tone.FeedbackDelay("8n", 0.5).toDestination();
        synths[i] = new Tone.MonoSynth({
            oscillator: { type: "sawtooth" },
            envelope: { attack: 0.01, decay: 0.1, sustain: 0.2, release: 0.1 },
            filterEnvelope: { attack: 0.02, decay: 0.2, sustain: 0.5, release: 0.2, baseFrequency: 200, octaves: 4 }
        });
        synths[i].chain(synthDistortions[i], synthVolumes[i]);
        synths[i].connect(synthDelays[i]);
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
    const key = Harmony.notes.indexOf(document.getElementById('key-select').value);
    synthPatterns[synthIndex] = pattern.map(noteValue => {
        if (noteValue === -1) return null;
        const noteName = Harmony.notes[(key + noteValue) % 12];
        const octave = Math.floor((key + noteValue) / 12);
        return { note: `${noteName}${octave}`, accent: false, slide: false };
    });
    createSynthSequencerGrid(synthIndex);
}

function clearSynthPattern(synthIndex) {
    synthPatterns[synthIndex] = [];
    createSynthSequencerGrid(synthIndex);
}

function shiftPattern(synthIndex, amount) {
    for (let i = 0; i < synthPatterns[synthIndex].length; i++) {
        const stepData = synthPatterns[synthIndex][i];
        if (stepData) {
            const noteName = stepData.note.slice(0, -1);
            const octave = parseInt(stepData.note.slice(-1));
            const noteIndex = notes.indexOf(noteName);
            const newNoteIndex = noteIndex + amount;
            const newOctave = octave + Math.floor(newNoteIndex / 12);
            const newNoteName = notes[newNoteIndex % 12];
            if (newOctave >= 0 && newOctave < octaves) {
                stepData.note = `${newNoteName}${newOctave}`;
            }
        }
    }
    createSynthSequencerGrid(synthIndex);
}

function createSynthSequencerGrid(synthIndex) {
    const gridContainer = document.querySelector('.piano-roll-grid');
    const keyboardContainer = document.querySelector('.keyboard');
    const gridContainerParent = document.querySelector('.piano-roll-grid-container');

    gridContainer.innerHTML = '';
    keyboardContainer.innerHTML = '';
    for (let octave = octaves - 1; octave >= 0; octave--) {
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
                if (stepData && stepData.note === noteName) {
                    cell.classList.add('active');
                }
                cell.addEventListener('click', () => {
                    cell.classList.toggle('active');
                    if (cell.classList.contains('active')) {
                        synthPatterns[synthIndex][step] = { note: noteName, accent: false, slide: false };
                    } else {
                        synthPatterns[synthIndex][step] = null;
                    }
                    for(let o = octaves - 1; o >= 0; o--) {
                        for (const n of notes.slice().reverse()) {
                            const otherNoteName = `${n}${o}`;
                            if (otherNoteName !== noteName) {
                                const otherCell = document.querySelector(`.step[data-note='${otherNoteName}'][data-step='${step}']`);
                                if (otherCell) otherCell.classList.remove('active');
                            }
                        }
                    }
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
            if (stepData) {
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
    });
    document.getElementById('global-vol-knob').addEventListener('input', e => {
        Tone.Destination.volume.value = -40 + (parseFloat(e.target.value) / 100) * 40;
    });

    for (let i = 0; i < 4; i++) {
        document.getElementById(`synth${i}-vol-knob`).addEventListener('input', e => {
            synthVolumes[i].volume.value = -40 + (parseFloat(e.target.value) / 100) * 40;
        });
    }
    document.getElementById('drums-vol-knob').addEventListener('input', e => {
        drumsVolume.volume.value = -40 + (parseFloat(e.target.value) / 100) * 40;
    });

    document.getElementById('knob-tune').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            synths[synthIndex].oscillator.detune.value = (parseFloat(e.target.value) - 64) * 100;
        }
    });
    document.getElementById('knob-cutoff').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            synths[synthIndex].filter.frequency.value = (parseFloat(e.target.value) / 127) * 5000 + 200;
        }
    });
    document.getElementById('knob-resonance').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            synths[synthIndex].filter.Q.value = (parseFloat(e.target.value) / 127) * 20;
        }
    });
    document.getElementById('knob-env-mod').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            synths[synthIndex].filterEnvelope.octaves = (parseFloat(e.target.value) / 127) * 10;
        }
    });
    document.getElementById('knob-decay').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            const decay = (parseFloat(e.target.value) / 127) * 0.5 + 0.01;
            synths[synthIndex].filterEnvelope.decay = decay;
            synths[synthIndex].envelope.decay = decay;
        }
    });
    document.getElementById('knob-accent').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            synths[synthIndex].filterEnvelope.sustain = 0.5 + (parseFloat(e.target.value) / 127) * 0.5;
        }
    });

    document.getElementById('delay-time-knob').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            synthDelays[synthIndex].delayTime.value = (parseFloat(e.target.value) / 100) * 2;
        }
    });

    document.getElementById('delay-feedback-knob').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            synthDelays[synthIndex].feedback.value = (parseFloat(e.target.value) / 100) * 0.9;
        }
    });

    document.getElementById('knob-drive').addEventListener('input', e => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            synthDistortions[synthIndex].distortion = (parseFloat(e.target.value) / 127);
        }
    });

    document.getElementById('knob-wave').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            const currentWave = synths[synthIndex].oscillator.type;
            synths[synthIndex].oscillator.type = currentWave === 'sawtooth' ? 'square' : 'sawtooth';
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
    return {
        drumPattern,
        synthPatterns,
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
        if (activeView === 'drums') randomizeDrumPattern();
        else if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            randomizeSynthPattern(synthIndex);
        }
    });

    document.getElementById('play-pause-button').addEventListener('click', () => {
        if (Tone.Transport.state === 'started') Tone.Transport.pause();
        else Tone.Transport.start();
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
        });
    });

    document.getElementById('gen-mel-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            const scale = Harmony.scales[document.getElementById('scale-select').value];
            const progression = Harmony.progressions[document.getElementById('progression-select').value];
            const melody = MelodyGenerator.generateMelody(progression, scale, 16);
            applySynthPattern(synthIndex, melody);
        }
    });

    document.getElementById('gen-bass-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            const scale = Harmony.scales[document.getElementById('scale-select').value];
            const progression = Harmony.progressions[document.getElementById('progression-select').value];
            const bassline = MelodyGenerator.generateBassline(progression, scale, 16);
            applySynthPattern(synthIndex, bassline);
        }
    });

    document.getElementById('harmonize-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            const melody = synthPatterns[synthIndex].map(step => step ? step.note : -1);
            const scale = Harmony.scales[document.getElementById('scale-select').value];
            const progression = Harmony.progressions[document.getElementById('progression-select').value];
            const harmony = Harmony.generateHarmony(melody, progression, scale);
            for (let i = 0; i < 4; i++) {
                if (i !== synthIndex) {
                    applySynthPattern(i, harmony);
                }
            }
        }
    });

    document.getElementById('mutate-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            const scale = Harmony.scales[document.getElementById('scale-select').value];
            const pattern = synthPatterns[synthIndex].map(step => step ? step.note : -1);
            const mutatedPattern = MelodyGenerator.mutatePattern(pattern, scale, 0.2);
            applySynthPattern(synthIndex, mutatedPattern);
        }
    });

    document.getElementById('mutate-rhythm-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            const pattern = synthPatterns[synthIndex].map(step => step ? step.note : -1);
            const mutatedPattern = MelodyGenerator.mutateRhythm(pattern, 0.2);
            applySynthPattern(synthIndex, mutatedPattern);
        }
    });

    document.getElementById('mutate-accents-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            synthPatterns[synthIndex] = MelodyGenerator.mutateAccents(synthPatterns[synthIndex], 0.2);
            createSynthSequencerGrid(synthIndex);
        }
    });

    document.getElementById('mutate-slides-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            synthPatterns[synthIndex] = MelodyGenerator.mutateSlides(synthPatterns[synthIndex], 0.2);
            createSynthSequencerGrid(synthIndex);
        }
    });

    document.getElementById('clear-synth-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            clearSynthPattern(synthIndex);
        }
    });
    document.getElementById('clear-drums-button').addEventListener('click', clearDrumPattern);

    document.getElementById('up-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            shiftPattern(synthIndex, 1);
        }
    });
    document.getElementById('down-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            shiftPattern(synthIndex, -1);
        }
    });
    document.getElementById('oct-up-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            shiftPattern(synthIndex, 12);
        }
    });
    document.getElementById('oct-down-button').addEventListener('click', () => {
        if (activeView.startsWith('synth')) {
            const synthIndex = parseInt(activeView.replace('synth', ''));
            shiftPattern(synthIndex, -12);
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
