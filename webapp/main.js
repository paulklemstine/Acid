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

function createDrumSequencerGrid() {
    console.log("createDrumSequencerGrid: starting");
    const container = document.getElementById('drum-matrix');
    if (!container) {
        console.error("createDrumSequencerGrid: drum-matrix not found!");
        return;
    }
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
                step.classList.toggle('active');
            });
            track.appendChild(step);
        }
        container.appendChild(track);
    }
    console.log("createDrumSequencerGrid: finished");
}

function setupDrumSequencer() {
    console.log("setupDrumSequencer: starting");
    drumSequence = new Tone.Sequence((time, col) => {
        const activeSteps = document.querySelectorAll(`#drum-matrix .step[data-step='${col}'].active`);
        activeSteps.forEach(step => {
            const sampleName = step.dataset.track;
            if (players[sampleName]) {
                players[sampleName].start(time);
            }
        });
        const allSteps = document.querySelectorAll('#drum-matrix .step');
        allSteps.forEach(step => {
            if (step.dataset.step == col) {
                step.classList.add('playing');
            } else {
                step.classList.remove('playing');
            }
        });
    }, [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15], '16n').start(0);
    console.log("setupDrumSequencer: finished");
}

async function init() {
    console.log("init: starting");
    await loadSamples();
    createDrumSequencerGrid();
    setupDrumSequencer();
    console.log("init: finished");

    console.log("init: setting up event listeners");
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
    const container = document.getElementById('start-button-container');
    const startButton = document.createElement('button');
    startButton.textContent = 'Start Audio';
    container.appendChild(startButton);
    startButton.addEventListener('click', async () => {
        console.log("Start Audio button clicked");
        await Tone.start();
        console.log("Tone.start() resolved, Tone.context.state:", Tone.context.state);
        startButton.textContent = 'Loading...';
        await init();
        container.style.display = 'none';
    });
});
