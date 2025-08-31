package synth;

public class MelodyGenerator {

    public static int[] generateMelody(int[] chordProgression, int[] scale, int patternLength) {
        int[] melody = new int[patternLength];
        // a simple implementation for now
        for (int i = 0; i < patternLength; i++) {
            int degree = chordProgression[i % chordProgression.length];
            int[] chord = new Harmony().getNotesInChord(0, scale, degree, 3, 0);
            melody[i] = chord[0];
        }
        return melody;
    }

    public static int[] generateBassline(int[] chordProgression, int[] scale, int patternLength) {
        int[] bassline = new int[patternLength];
        // a simple implementation for now
        for (int i = 0; i < patternLength; i++) {
            int degree = chordProgression[i % chordProgression.length];
            int[] chord = new Harmony().getNotesInChord(0, scale, degree, 3, 0);
            bassline[i] = chord[0] - 12; // one octave lower
        }
        return bassline;
    }
}
