package synth;

public class Harmonizer {

    public static int[] harmonize(int[] melody, int[] chordProgression, int[] scale, int patternLength) {
        int[] harmony = new int[patternLength];
        // a simple implementation for now
        for (int i = 0; i < patternLength; i++) {
            int degree = chordProgression[i % chordProgression.length];
            int[] chord = new Harmony().getNotesInChord(0, scale, degree, 3, 0);
            // find the closest chord note to the melody note
            int closestNote = -1;
            int minDistance = Integer.MAX_VALUE;
            for (int chordNote : chord) {
                int distance = Math.abs(melody[i] - chordNote);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestNote = chordNote;
                }
            }
            harmony[i] = closestNote;
        }
        return harmony;
    }

    public static int[] createCounterMelody(int[] melody, int[] chordProgression, int[] scale, int patternLength) {
        int[] counterMelody = new int[patternLength];
        // a simple implementation for now
        for (int i = 0; i < patternLength; i++) {
            int degree = chordProgression[i % chordProgression.length];
            int[] chord = new Harmony().getNotesInChord(0, scale, degree, 3, 0);
            // find a chord note that is not the melody note
            int counterNote = -1;
            for (int chordNote : chord) {
                if (chordNote != melody[i]) {
                    counterNote = chordNote;
                    break;
                }
            }
            counterMelody[i] = counterNote;
        }
        return counterMelody;
    }
}
