const Tone = {
    MonoSynth: class {
        toMaster() {
            return this;
        }
        triggerAttack() {}
        triggerRelease() {}
    },
    Sampler: class {
        toMaster() {
            return this;
        }
        triggerAttack() {}
    },
    Loop: class {
        start() {
            return this;
        }
    },
    Transport: {
        bpm: {
            value: 120
        },
        start: () => {},
        stop: () => {},
        state: 'stopped'
    },
    Master: {
        volume: {
            value: 0
        }
    }
};

module.exports = Tone;
