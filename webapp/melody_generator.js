const MelodyGenerator = {
  generateMelody(chordProgression, scale, patternLength) {
    const melody = [];
    for (let i = 0; i < patternLength; i++) {
      const degree = chordProgression[(Math.floor(i / 4)) % chordProgression.length];
      const chord = Harmony.getNotesInChord(48, scale, degree - 1, 3, 0);

      const noteIndex = Math.floor(Math.random() * chord.length);
      melody[i] = chord[noteIndex];

      if (Math.random() < 0.2) {
        melody[i] = -1; // Rest
      }
    }
    return melody;
  },

  generateBassline(chordProgression, scale, patternLength) {
    const bassline = [];
    for (let i = 0; i < patternLength; i++) {
      if (i % 4 === 0) {
        const degree = chordProgression[Math.floor(i / 4) % chordProgression.length];
        const note = Harmony.getFromScale(degree - 1, scale) + 24;
        bassline[i] = Math.max(note, 0);
      } else {
        if (Math.random() < 0.3) {
          const degree = chordProgression[Math.floor(i / 4) % chordProgression.length];
          const chord = Harmony.getNotesInChord(0, scale, degree - 1, 3, 0);
          const note = chord[Math.floor(Math.random() * chord.length)] + 24;
          bassline[i] = Math.max(note, 0);
        } else {
          bassline[i] = -1;
        }
      }
    }
    return bassline;
  },

  mutatePattern(pattern, scale, mutationRate) {
    const mutatedPattern = [...pattern];
    for (let i = 0; i < mutatedPattern.length; i++) {
        if (Math.random() < mutationRate) {
            const randomNoteFromScale = scale[Math.floor(Math.random() * scale.length)];
            const octave = Math.floor(Math.random() * 2) + 3;
            mutatedPattern[i] = randomNoteFromScale + (12 * octave);
        }
    }
    return mutatedPattern;
  },

  mutateRhythm(pattern, mutationRate) {
    const mutatedPattern = [...pattern];
    for (let i = 0; i < mutatedPattern.length; i++) {
        if (Math.random() < mutationRate) {
            if (mutatedPattern[i] === -1) {
                // Add a note where there was a rest
                mutatedPattern[i] = pattern[i-1] || pattern[i+1] || 0; // simple logic to add a note
            } else {
                mutatedPattern[i] = -1; // Add a rest
            }
        }
    }
    return mutatedPattern;
  },

  mutateAccents(pattern, mutationRate) {
    const mutatedPattern = [...pattern];
    for (let i = 0; i < mutatedPattern.length; i++) {
        if (mutatedPattern[i] && Math.random() < mutationRate) {
            mutatedPattern[i].accent = !mutatedPattern[i].accent;
        }
    }
    return mutatedPattern;
  },

  mutateSlides(pattern, mutationRate) {
    const mutatedPattern = [...pattern];
    for (let i = 0; i < mutatedPattern.length; i++) {
        if (mutatedPattern[i] && Math.random() < mutationRate) {
            mutatedPattern[i].slide = !mutatedPattern[i].slide;
        }
    }
    return mutatedPattern;
  },

  arpeggiate(pattern, octaves, direction) {
    let notes = pattern
      .map(step => (step ? step.note : null))
      .filter(note => note !== null);

    let uniqueNotes = [...new Set(notes)].map(noteName => {
        const noteNameMatch = noteName.match(/[A-G]#?/);
        const octaveMatch = noteName.match(/\d+$/);
        if (!noteNameMatch || !octaveMatch) return null;
        const note = noteNameMatch[0];
        const octave = parseInt(octaveMatch[0]);
        const noteIndex = Harmony.notes.indexOf(note);
        return octave * 12 + noteIndex;
    }).filter(note => note !== null);

    if (uniqueNotes.length === 0) {
      return Array(16).fill(-1);
    }

    uniqueNotes.sort((a, b) => a - b);

    const arpNotes = [];
    if (direction.toLowerCase() === 'up') {
      for (let o = 0; o < octaves; o++) {
        for (const note of uniqueNotes) {
          arpNotes.push(note + (12 * o));
        }
      }
    } else if (direction.toLowerCase() === 'down') {
      for (let o = 0; o < octaves; o++) {
        for (let i = uniqueNotes.length - 1; i >= 0; i--) {
          arpNotes.push(uniqueNotes[i] + (12 * o));
        }
      }
    } else { // "up-down"
      for (let o = 0; o < octaves; o++) {
        for (const note of uniqueNotes) {
          arpNotes.push(note + (12 * o));
        }
        for (let i = uniqueNotes.length - 2; i > 0; i--) {
          arpNotes.push(uniqueNotes[i] + (12 * o));
        }
      }
    }

    if (arpNotes.length === 0) {
        return Array(16).fill(-1);
    }

    const arpeggiatedPattern = [];
    for (let i = 0; i < 16; i++) {
      arpeggiatedPattern[i] = arpNotes[i % arpNotes.length];
    }

    return arpeggiatedPattern;
  }
};
