package synth;

import com.acid.SequencerData;
import com.acid.Statics;

public class PatternGenerator {

    private static String currentGenre = "house";
    private static int rootKey = Harmony.C;
    private static int[] scale = Harmony.SCALE_NATURAL_MINOR;
    private static int[] progression = Harmony.Pop;

    public static void setGenre(String genre) {
        currentGenre = genre;
        // Here you could add logic to change scale/progression based on genre
        if (genre.equals("psytrance")) {
            scale = Harmony.SCALE_HUNGARIAN_MINOR;
            progression = new int[]{1, 4, 5, 1};
        } else if (genre.equals("dubstep")) {
            scale = Harmony.SCALE_NATURAL_MINOR;
            progression = new int[]{1, 6, 4, 5};
        } else if (genre.equals("techno")) {
            scale = Harmony.SCALE_NATURAL_MINOR;
            progression = new int[]{1, 1, 1, 1};
        } else if (genre.equals("trance")) {
            scale = Harmony.SCALE_NATURAL_MINOR;
            progression = new int[]{6, 4, 1, 5};
        } else if (genre.equals("dnb")) {
            scale = Harmony.SCALE_NATURAL_MINOR;
            progression = new int[]{1, 5, 6, 4};
        } else { // house
            scale = Harmony.SCALE_DORIAN;
            progression = Harmony.Jazz;
        }
    }

    public static void newKey() {
        rootKey = (int) (Math.random() * 12);
        scale = Harmony.SCALE_ALL[(int) (Math.random() * Harmony.SCALE_ALL.length)];
    }

    private static SequencerData applyPatternToSequencer(int[] pattern, int synthIndex) {
        SequencerData sd = new SequencerData(synthIndex);
        sd.randomize();
        sd.refresh();
        return sd;
    }

    public static void generateBassline(int synthIndex) {
        int[] bassline = MelodyGenerator.generateBassline(progression, scale, 16);
        applyPatternToSequencer(bassline, synthIndex);
    }

    public static void generateHarmony(int synthIndex) {
        // This is tricky for a monosynth, so we'll just outline the root notes of the chords
        int[] harmony = new int[16];
        for (int i = 0; i < 16; i++) {
             int degree = progression[ (i/4) % progression.length];
             harmony[i] = new Harmony().getFromScale(degree-1, scale);
        }
        applyPatternToSequencer(harmony, synthIndex);
    }

    public static void generateArpeggio(int synthIndex) {
        int[] arpeggio = new int[16];
        int[] chord = new Harmony().getNotesInChord(0, scale, progression[0]-1, 3, 0);
        int[] arpNotes = new Harmony().arpeggiate(chord, 2);

        for (int i=0; i<16; i++) {
            int degree = progression[(i/4) % progression.length];
            chord = new Harmony().getNotesInChord(0, scale, degree-1, 3, 0);
            arpNotes = new Harmony().arpeggiate(chord, 2);
            arpeggio[i] = arpNotes[i % arpNotes.length];
        }
        applyPatternToSequencer(arpeggio, synthIndex);
    }

    public static void generateMelody(int synthIndex) {
        int[] melody = MelodyGenerator.generateMelody(progression, scale, 16);
         for(int i=0; i<melody.length; i++) {
            melody[i] += rootKey;
        }
        applyPatternToSequencer(melody, synthIndex);
    }

    public static void generateMusical(int synthIndex) {
        // Find a harmony from another synth to base the new melody on
        boolean foundBase = false;
        for (int i = 0; i < Statics.NUM_SYNTHS; i++) {
            if (i != synthIndex && !isSilent(Statics.output.getSequencer().basslines[i])) {
                 int[] baseMelody = new int[16];
                 for(int j=0; j<16; j++) {
                     baseMelody[j] = Statics.output.getSequencer().basslines[i].note[j];
                 }
                 int[] counterMelody = Harmonizer.createCounterMelody(baseMelody, progression, scale, 16);
                 for(int j=0; j<counterMelody.length; j++) {
                counterMelody[j] += rootKey;
                 }
                 applyPatternToSequencer(counterMelody, synthIndex);
                 foundBase = true;
                 break;
            }
        }
        // If no other synth is playing, harmonize with self
        if (!foundBase) {
            if (!isSilent(Statics.output.getSequencer().basslines[synthIndex])) {
                int[] baseMelody = new int[16];
                for(int j=0; j<16; j++) {
                    baseMelody[j] = Statics.output.getSequencer().basslines[synthIndex].note[j];
                }
                int[] harmony = Harmonizer.harmonize(baseMelody, progression, scale, 16);
                for(int j=0; j<harmony.length; j++) {
                harmony[j] += rootKey;
                }
                applyPatternToSequencer(harmony, synthIndex);
            }
        }
    }

    private static boolean isSilent(BasslinePattern pattern) {
        for(int i=0; i<16; i++) {
            if (!pattern.pause[i]) return false;
        }
        return true;
    }

    public static void randomize(int synthIndex) {
        SequencerData sd = new SequencerData(synthIndex);
        sd.randomize();
        sd.refresh();
    }
}
