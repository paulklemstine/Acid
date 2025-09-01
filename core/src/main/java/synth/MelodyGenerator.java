package synth;

import java.util.Random;

public class MelodyGenerator {

    private static Random random = new Random();

    public static int[] generateMelody(int[] chordProgression, int[] scale, int patternLength) {
        int[] melody = new int[patternLength];
        for (int i = 0; i < patternLength; i++) {
            int degree = chordProgression[(i / 4) % chordProgression.length];
            int[] chord = new Harmony().getNotesInChord(0, scale, degree - 1, 3, 0);

            // Use a random note from the chord
            int noteIndex = random.nextInt(chord.length);
            melody[i] = chord[noteIndex];

            // Add some rhythmic variation
            if (random.nextDouble() < 0.2) {
                melody[i] = -1; // Rest
            }
        }
        return melody;
    }

    public static int[] generateBassline(int[] chordProgression, int[] scale, int patternLength) {
        int[] bassline = new int[patternLength];
        for (int i = 0; i < patternLength; i++) {
            // Follow the root of the chord progression
            if (i % 2 == 0) { // Play on the beat
                int degree = chordProgression[(i / 4) % chordProgression.length];
                int note = new Harmony().getFromScale(degree - 1, scale) - 12;
                bassline[i] = Math.max(note, 0);
            } else {
                // Add some passing notes
                if (random.nextDouble() < 0.3) {
                    int degree = chordProgression[(i / 4) % chordProgression.length];
                    int[] chord = new Harmony().getNotesInChord(0, scale, degree - 1, 3, 0);
                    int note = chord[random.nextInt(chord.length)] - 12;
                    bassline[i] = Math.max(note, 0);
                } else {
                    bassline[i] = -1; // Rest
                }
            }
        }
        return bassline;
    }
}
