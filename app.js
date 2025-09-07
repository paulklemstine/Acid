console.log("Acid JS is running!");

const bpmSlider = document.getElementById('bpm');
const bpmValue = document.getElementById('bpm-value');
const volumeSlider = document.getElementById('volume');
const volumeValue = document.getElementById('volume-value');

bpmSlider.addEventListener('input', (event) => {
    const bpm = event.target.value;
    bpmValue.textContent = bpm;
    Tone.Transport.bpm.value = bpm;
});

volumeSlider.addEventListener('input', (event) => {
    const volume = event.target.value;
    volumeValue.textContent = volume;
    Tone.Master.volume.value = volume;
});

const drumGrid = document.getElementById('drum-grid');
const drumPattern = [
    ['BD', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    ['SD', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    ['CH', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    ['OH', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
];

for (let i = 0; i < drumPattern.length; i++) {
    const row = document.createElement('div');
    row.classList.add('drum-row');
    const label = document.createElement('div');
    label.classList.add('drum-label');
    label.textContent = drumPattern[i][0];
    row.appendChild(label);
    for (let j = 1; j < drumPattern[i].length; j++) {
        const cell = document.createElement('input');
        cell.type = 'checkbox';
        cell.classList.add('drum-cell');
        row.appendChild(cell);
    }
    drumGrid.appendChild(row);
}

const sequencerGrid = document.getElementById('sequencer-grid');
const sequencerPattern = new Array(16).fill(0).map(() => new Array(16).fill(0));

for (let i = 0; i < sequencerPattern.length; i++) {
    const row = document.createElement('div');
    row.classList.add('sequencer-row');
    for (let j = 0; j < sequencerPattern[i].length; j++) {
        const cell = document.createElement('div');
        cell.classList.add('sequencer-cell');
        cell.addEventListener('click', () => {
            sequencerPattern[i][j] = sequencerPattern[i][j] ? 0 : 1;
            cell.style.backgroundColor = sequencerPattern[i][j] ? '#f00' : '#222';
        });
        row.appendChild(cell);
    }
    sequencerGrid.appendChild(row);
}

class Synth {
    constructor() {
        this.synth = new Tone.MonoSynth({
            oscillator: {
                type: 'sawtooth'
            },
            envelope: {
                attack: 0.005,
                decay: 0.1,
                sustain: 0.9,
                release: 1
            },
            filterEnvelope: {
                attack: 0.06,
                decay: 0.2,
                sustain: 0.5,
                release: 2,
                baseFrequency: 300,
                octaves: 7,
                exponent: 2
            }
        }).toMaster();
    }

    noteOn(note) {
        this.synth.triggerAttack(note);
    }

    noteOff() {
        this.synth.triggerRelease();
    }
}

const synth = new Synth();

const tuneKnob = document.getElementById('tune');
const cutoffKnob = document.getElementById('cutoff');
const resonanceKnob = document.getElementById('resonance');
const envModKnob = document.getElementById('env-mod');
const decayKnob = document.getElementById('decay');
const accentKnob = document.getElementById('accent');

tuneKnob.addEventListener('input', (event) => {
    const tune = event.target.value;
    synth.synth.detune.value = (tune - 64) * 100;
});

cutoffKnob.addEventListener('input', (event) => {
    const cutoff = event.target.value;
    synth.synth.filter.frequency.value = (cutoff / 127) * 5000;
});

resonanceKnob.addEventListener('input', (event) => {
    const resonance = event.target.value;
    synth.synth.filter.Q.value = (resonance / 127) * 20;
});

envModKnob.addEventListener('input', (event) => {
    const envMod = event.target.value;
    synth.synth.filterEnvelope.octaves = (envMod / 127) * 10;
});

decayKnob.addEventListener('input', (event) => {
    const decay = event.target.value;
    synth.synth.envelope.decay = (decay / 127) * 2;
    synth.synth.filterEnvelope.decay = (decay / 127) * 2;
});

class Drums {
    constructor() {
        this.kits = {
            '808': {
                'C4': 'assets/808/808bd.wav',
                'D4': 'assets/808/808sd_base.wav',
                'E4': 'assets/808/808ch.wav',
                'F4': 'assets/808/808oh.wav',
            },
            '909': {
                'C4': 'assets/909/909bd_atk.wav',
                'D4': 'assets/909/909sd_base.wav',
                'E4': 'assets/909/909ch.wav',
                'F4': 'assets/909/909oh.wav',
            }
        };
        this.sampler = new Tone.Sampler(this.kits['808']).toMaster();
    }

    loadKit(kit) {
        this.sampler.dispose();
        this.sampler = new Tone.Sampler(this.kits[kit]).toMaster();
    }

    play(note) {
        this.sampler.triggerAttack(note);
    }
}

const drums = new Drums();

const drumKitSelector = document.getElementById('drum-kit');
drumKitSelector.addEventListener('change', (event) => {
    const kit = event.target.value;
    drums.loadKit(kit);
});

class Sequencer {
    constructor(synth, drums) {
        this.synth = synth;
        this.drums = drums;
        this.step = 0;

        this.loop = new Tone.Loop((time) => {
            this.updateDrumPattern();
            const drumNotes = ['C4', 'D4', 'E4', 'F4'];
            for (let i = 0; i < drumPattern.length; i++) {
                if (drumPattern[i][this.step + 1]) {
                    this.drums.play(drumNotes[i]);
                }
            }
            this.step = (this.step + 1) % 16;
            this.highlightStep();

            if (sequencerPattern[0][this.step]) {
                this.synth.noteOn('C4');
            } else {
                this.synth.noteOff();
            }
        }, '16n').start(0);
    }

    highlightStep() {
        const drumCells = document.querySelectorAll('.drum-cell');
        const sequencerCells = document.querySelectorAll('.sequencer-cell');
        const allCells = [...drumCells, ...sequencerCells];
        allCells.forEach(cell => cell.classList.remove('active'));
        for (let i = 0; i < drumPattern.length; i++) {
            const cell = drumGrid.children[i].children[this.step + 1];
            if (cell) {
                cell.classList.add('active');
            }
        }
        for (let i = 0; i < sequencerPattern.length; i++) {
            const cell = sequencerGrid.children[i].children[this.step];
            if (cell) {
                cell.classList.add('active');
            }
        }
    }

    updateDrumPattern() {
        const drumGrid = document.getElementById('drum-grid');
        for (let i = 0; i < drumPattern.length; i++) {
            for (let j = 1; j < drumPattern[i].length; j++) {
                const cell = drumGrid.children[i].children[j];
                drumPattern[i][j] = cell.checked ? 1 : 0;
            }
        }
    }

    start() {
        Tone.Transport.start();
    }

    stop() {
        Tone.Transport.stop();
    }
}

const Tone = require('tone');

if (typeof window !== 'undefined') {
    const sequencer = new Sequencer(synth, drums);

    const playStopButton = document.getElementById('play-stop');
    playStopButton.addEventListener('click', () => {
    if (Tone.Transport.state === 'started') {
        sequencer.stop();
        playStopButton.textContent = 'Play';
    } else {
        sequencer.start();
        playStopButton.textContent = 'Stop';
    }
});

const saveButton = document.getElementById('save');
saveButton.addEventListener('click', () => {
    const state = {
        bpm: bpmSlider.value,
        volume: volumeSlider.value,
        tune: tuneKnob.value,
        cutoff: cutoffKnob.value,
        resonance: resonanceKnob.value,
        envMod: envModKnob.value,
        decay: decayKnob.value,
        accent: accentKnob.value,
        drumKit: drumKitSelector.value,
        drumPattern: drumPattern,
        sequencerPattern: sequencerPattern,
    };
    localStorage.setItem('acidjs-state', JSON.stringify(state));
});

const loadButton = document.getElementById('load');
loadButton.addEventListener('click', () => {
    const state = JSON.parse(localStorage.getItem('acidjs-state'));
    if (state) {
        bpmSlider.value = state.bpm;
        bpmValue.textContent = state.bpm;
        Tone.Transport.bpm.value = state.bpm;

        volumeSlider.value = state.volume;
        volumeValue.textContent = state.volume;
        Tone.Master.volume.value = state.volume;

        tuneKnob.value = state.tune;
        synth.synth.detune.value = (state.tune - 64) * 100;

        cutoffKnob.value = state.cutoff;
        synth.synth.filter.frequency.value = (state.cutoff / 127) * 5000;

        resonanceKnob.value = state.resonance;
        synth.synth.filter.Q.value = (state.resonance / 127) * 20;

        envModKnob.value = state.envMod;
        synth.synth.filterEnvelope.octaves = (state.envMod / 127) * 10;

        decayKnob.value = state.decay;
        synth.synth.envelope.decay = (state.decay / 127) * 2;
        synth.synth.filterEnvelope.decay = (state.decay / 127) * 2;

        accentKnob.value = state.accent;

        drumKitSelector.value = state.drumKit;
        drums.loadKit(state.drumKit);

        for (let i = 0; i < state.drumPattern.length; i++) {
            for (let j = 1; j < state.drumPattern[i].length; j++) {
                const cell = drumGrid.children[i].children[j];
                cell.checked = state.drumPattern[i][j];
            }
        }

        for (let i = 0; i < state.sequencerPattern.length; i++) {
            for (let j = 0; j < state.sequencerPattern[i].length; j++) {
                const cell = sequencerGrid.children[i].children[j];
                sequencerPattern[i][j] = state.sequencerPattern[i][j];
                cell.style.backgroundColor = sequencerPattern[i][j] ? '#f00' : '#222';
            }
        }
    }
});
}

module.exports = {
    Synth,
    Drums,
    Sequencer
};
