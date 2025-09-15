const Harmony = {
  notes: ["C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"],

  SCALE_NATURAL_MINOR: [0, 2, 3, 5, 7, 8, 10],
  SCALE_MAJOR: [0, 2, 4, 5, 7, 9, 11],
  SCALE_DORIAN: [0, 2, 3, 5, 7, 9, 10],
  SCALE_PHRYGIAN: [0, 1, 3, 5, 7, 8, 10],
  SCALE_LYDIAN: [0, 2, 4, 6, 7, 9, 11],
  SCALE_MIXOLYDIAN: [0, 2, 4, 5, 7, 9, 10],
  SCALE_LOCRIAN: [0, 1, 3, 5, 6, 8, 10],
  SCALE_BLUES: [0, 3, 5, 6, 7, 10],
  SCALE_PENTATONIC_MINOR: [0, 3, 5, 7, 10],
  SCALE_PENTATONIC_MAJOR: [0, 2, 4, 7, 9],

  scales: {
    "Major": [0, 2, 4, 5, 7, 9, 11],
    "Natural Minor": [0, 2, 3, 5, 7, 8, 10],
    "Dorian": [0, 2, 3, 5, 7, 9, 10],
    "Phrygian": [0, 1, 3, 5, 7, 8, 10],
    "Lydian": [0, 2, 4, 6, 7, 9, 11],
    "Mixolydian": [0, 2, 4, 5, 7, 9, 10],
    "Locrian": [0, 1, 3, 5, 6, 8, 10],
    "Blues": [0, 3, 5, 6, 7, 10],
    "Pentatonic Minor": [0, 3, 5, 7, 10],
    "Pentatonic Major": [0, 2, 4, 7, 9],
  },

  progressions: {
    "Pop": [1, 5, 6, 4],
    "Jazz": [2, 5, 1],
    "Blues": [1, 4, 5],
    "Pachelbel": [1, 5, 6, 3, 4, 1, 4, 5],
    "Pop Punk": [1, 5, 6, 4],
    "Andalusian": [1, 7, 6, 5],
    "50s": [1, 6, 4, 5],
  },

  getFromScale(i, scale) {
    return scale[i % scale.length] + 12 * Math.floor(i / scale.length);
  },

  getNotesInChord(key, scale, degree, type = 3, inversion = 0) {
    let chord = [];
    for (let i = 0; i < type; i++) {
      chord[i] = key + this.getFromScale(degree + i * 2, scale);
    }
    // Handle inversions if needed
    return chord;
  },

  generateHarmony(melody, chordProgression, scale) {
    const harmony = [];
    for (let i = 0; i < melody.length; i++) {
        if (melody[i] !== -1) {
            const degree = chordProgression[Math.floor(i / 4) % chordProgression.length];
            const chord = this.getNotesInChord(36, scale, degree - 1, 3, 0);

            let harmonyNote = -1;
            for (let j = 0; j < chord.length; j++) {
                if (chord[j] % 12 !== melody[i] % 12) {
                    harmonyNote = chord[j];
                    break;
                }
            }

            if (harmonyNote !== -1) {
                harmony[i] = harmonyNote;
            } else {
                harmony[i] = chord[0] + 12;
            }
        } else {
            harmony[i] = -1;
        }
    }
    return harmony;
  }
};
