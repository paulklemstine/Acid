package synth;

import java.util.Random;

public class Harmonizer {

    private static Random random = new Random();

    public static int[] harmonize(int[] melody, int[] chordProgression, int[] scale, int patternLength) {
        int[] harmony = new int[patternLength];
        for (int i = 0; i < patternLength; i++) {
            if (melody[i] == -1) {
                harmony[i] = -1;
                continue;
            }
            int degree = chordProgression[(i / 4) % chordProgression.length];
            int[] chord = new Harmony().getNotesInChord(0, scale, degree - 1, 3, 0);

            // Prefer harmonizing with a third or a fifth
            int third = chord[1];
            int fifth = chord[2];

            if (random.nextBoolean()) {
                harmony[i] = third;
            } else {
                harmony[i] = fifth;
            }

            // Adjust octave to be close to the melody
            while (Math.abs(harmony[i] - melody[i]) > 6) {
                if (harmony[i] < melody[i]) {
                    harmony[i] += 12;
                } else {
                    harmony[i] -= 12;
                }
            }
        }
        return harmony;
    }

    public static int[] createCounterMelody(int[] melody, int[] chordProgression, int[] scale, int patternLength) {
        int[] counterMelody = new int[patternLength];
        for (int i = 0; i < patternLength; i++) {
             if (melody[i] == -1) {
                counterMelody[i] = -1;
                continue;
            }
            int degree = chordProgression[(i / 4) % chordProgression.length];
            int[] chord = new Harmony().getNotesInChord(0, scale, degree - 1, 3, 0);

            // Create contrary motion if possible
            int melodyDirection = (i > 0 && melody[i] > melody[i-1]) ? 1 : -1;

            int bestNote = -1;
            int largestInterval = -1;

            for (int chordNote : chord) {
                if (chordNote != melody[i]) {
                    int interval = Math.abs(chordNote - melody[i]);
                    if (melodyDirection > 0 && chordNote < melody[i] && interval > largestInterval) {
                        bestNote = chordNote;
                        largestInterval = interval;
                    } else if (melodyDirection < 0 && chordNote > melody[i] && interval > largestInterval) {
                        bestNote = chordNote;
                        largestInterval = interval;
                    }
                }
            }

            if (bestNote == -1) { // fallback
                 for (int chordNote : chord) {
                    if (chordNote != melody[i]) {
                        bestNote = chordNote;
                        break;
                    }
                }
            }
            counterMelody[i] = bestNote;
        }
        return counterMelody;
    }
}
