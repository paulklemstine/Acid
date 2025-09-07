jest.mock('tone');
const { Synth, Drums, Sequencer } = require('./app');

describe('Synth', () => {
    it('can be instantiated', () => {
        const synth = new Synth();
        expect(synth).toBeDefined();
    });
});
