package synth;

import com.acid.DrumData;
import com.acid.SequencerData;
import com.acid.Statics;

import java.util.HashMap;
import java.util.Map;

public class PatternGenerator {

    private static String currentGenre = "house";
    private static int rootKey = Harmony.C;
    private static int[] scale = Harmony.SCALE_NATURAL_MINOR;
    private static int[] progression = Harmony.Pop;

    private static Map<String, Map<String, String[]>> genreBank;
    private static Map<String, Map<String, int[][]>> drumGenreBank;

    static {
        genreBank = new HashMap<>();
        Map<String, String[]> houseBanks = new HashMap<>();
        houseBanks.put("A", new String[]{"Bassline 1", "Melody 1", "Pad 1", "Arp 1"});
        houseBanks.put("B", new String[]{"Bassline 2", "Melody 2", "Pad 2", "Arp 2"});
        houseBanks.put("C", new String[]{"Bassline 3", "Melody 3", "Pad 3", "Arp 3"});
        houseBanks.put("D", new String[]{"Bassline 4", "Melody 4", "Pad 4", "Arp 4"});
        genreBank.put("House", houseBanks);

        Map<String, String[]> dubstepBanks = new HashMap<>();
        dubstepBanks.put("A", new String[]{"Wobble 1", "Growl 1", "Melody 1", "Pad 1"});
        dubstepBanks.put("B", new String[]{"Wobble 2", "Growl 2", "Melody 2", "Pad 2"});
        dubstepBanks.put("C", new String[]{"Wobble 3", "Growl 3", "Melody 3", "Pad 3"});
        dubstepBanks.put("D", new String[]{"Wobble 4", "Growl 4", "Melody 4", "Pad 4"});
        genreBank.put("Dubstep", dubstepBanks);

        Map<String, String[]> technoBanks = new HashMap<>();
        technoBanks.put("A", new String[]{"Bassline 1", "Stab 1", "Noise 1", "Melody 1"});
        technoBanks.put("B", new String[]{"Bassline 2", "Stab 2", "Noise 2", "Melody 2"});
        technoBanks.put("C", new String[]{"Bassline 3", "Stab 3", "Noise 3", "Melody 3"});
        technoBanks.put("D", new String[]{"Bassline 4", "Stab 4", "Noise 4", "Melody 4"});
        genreBank.put("Techno", technoBanks);

        Map<String, String[]> tranceBanks = new HashMap<>();
        tranceBanks.put("A", new String[]{"Arp 1", "Lead 1", "Pad 1", "Bassline 1"});
        tranceBanks.put("B", new String[]{"Arp 2", "Lead 2", "Pad 2", "Bassline 2"});
        tranceBanks.put("C", new String[]{"Arp 3", "Lead 3", "Pad 3", "Bassline 3"});
        tranceBanks.put("D", new String[]{"Arp 4", "Lead 4", "Pad 4", "Bassline 4"});
        genreBank.put("Trance", tranceBanks);

        Map<String, String[]> dnbBanks = new HashMap<>();
        dnbBanks.put("A", new String[]{"Reese 1", "Pad 1", "Bassline 1", "Melody 1"});
        dnbBanks.put("B", new String[]{"Reese 2", "Pad 2", "Bassline 2", "Melody 2"});
        dnbBanks.put("C", new String[]{"Reese 3", "Pad 3", "Bassline 3", "Melody 3"});
        dnbBanks.put("D", new String[]{"Reese 4", "Pad 4", "Bassline 4", "Melody 4"});
        genreBank.put("DnB", dnbBanks);

        Map<String, String[]> hardstyleBanks = new HashMap<>();
        hardstyleBanks.put("A", new String[]{"Lead 1", "Kick 1", "Screech 1", "Melody 1"});
        hardstyleBanks.put("B", new String[]{"Lead 2", "Kick 2", "Screech 2", "Melody 2"});
        hardstyleBanks.put("C", new String[]{"Lead 3", "Kick 3", "Screech 3", "Melody 3"});
        hardstyleBanks.put("D", new String[]{"Lead 4", "Kick 4", "Screech 4", "Melody 4"});
        genreBank.put("Hardstyle", hardstyleBanks);

        Map<String, String[]> ambientBanks = new HashMap<>();
        ambientBanks.put("A", new String[]{"Pad 1", "Drone 1", "Texture 1", "Atmo 1"});
        ambientBanks.put("B", new String[]{"Pad 2", "Drone 2", "Texture 2", "Atmo 2"});
        ambientBanks.put("C", new String[]{"Pad 3", "Drone 3", "Texture 3", "Atmo 3"});
        ambientBanks.put("D", new String[]{"Pad 4", "Drone 4", "Texture 4", "Atmo 4"});
        genreBank.put("Ambient", ambientBanks);

        Map<String, String[]> industrialBanks = new HashMap<>();
        industrialBanks.put("A", new String[]{"Noise 1", "Clang 1", "Rhythm 1", "Distortion 1"});
        industrialBanks.put("B", new String[]{"Noise 2", "Clang 2", "Rhythm 2", "Distortion 2"});
        industrialBanks.put("C", new String[]{"Noise 3", "Clang 3", "Rhythm 3", "Distortion 3"});
        industrialBanks.put("D", new String[]{"Noise 4", "Clang 4", "Rhythm 4", "Distortion 4"});
        genreBank.put("Industrial", industrialBanks);


        drumGenreBank = new HashMap<>();

        Map<String, int[][]> houseDrumBanks = new HashMap<>();
        houseDrumBanks.put("A", new int[][]{
                {1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0},
                {0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1},
                {0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1}
        });
        houseDrumBanks.put("B", new int[][]{
                {1,0,0,1,0,0,1,0,1,0,0,1,0,0,1,0},
                {0,0,1,0,0,1,0,0,0,0,1,0,0,1,0,0},
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {0,0,0,1,0,0,1,0,0,0,0,1,0,0,1,0}
        });
        houseDrumBanks.put("C", new int[][]{
                {1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0},
                {0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0},
                {0,0,1,1,0,0,1,1,0,0,1,1,0,0,1,1},
                {0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1}
        });
        houseDrumBanks.put("D", new int[][]{
                {1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0},
                {0,0,1,0,0,0,1,1,0,0,1,0,0,0,1,1},
                {1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0},
                {0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1}
        });
        drumGenreBank.put("House", houseDrumBanks);

        Map<String, int[][]> technoDrumBanks = new HashMap<>();
        technoDrumBanks.put("A", new int[][]{
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1},
                {0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1}
        });
        drumGenreBank.put("Techno", technoDrumBanks);

        Map<String, int[][]> dnbDrumBanks = new HashMap<>();
        dnbDrumBanks.put("A", new int[][]{
                {1,0,0,0,0,1,0,0,1,0,0,0,0,1,0,0},
                {0,0,1,0,0,0,0,1,0,0,1,0,0,0,0,1},
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1}
        });
        drumGenreBank.put("DnB", dnbDrumBanks);
    }

    public static String[] getGenres() {
        return genreBank.keySet().toArray(new String[0]);
    }

    public static String[] getBanks(String genre) {
        if (genreBank.get(genre) == null) return new String[0];
        return genreBank.get(genre).keySet().toArray(new String[0]);
    }

    public static String[] getPatterns(String genre, String bank) {
        if (genreBank.get(genre) == null || genreBank.get(genre).get(bank) == null) return new String[0];
        return genreBank.get(genre).get(bank);
    }

    public static String[] getDrumGenres() {
        return drumGenreBank.keySet().toArray(new String[0]);
    }

    public static String[] getDrumBanks(String genre) {
        if (drumGenreBank.get(genre) == null) return new String[0];
        return drumGenreBank.get(genre).keySet().toArray(new String[0]);
    }

    public static String[] getDrumPatterns(String genre, String bank) {
        return new String[]{bank};
    }

    public static int[][] getDrumPattern(String genre, String bank) {
        if (drumGenreBank.containsKey(genre) && drumGenreBank.get(genre).containsKey(bank)) {
            return drumGenreBank.get(genre).get(bank);
        }
        return null;
    }

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
        for (int i = 0; i < 16; i++) {
            sd.note[i] = (byte) (pattern[i % pattern.length] - 12);
            sd.pause[i] = Math.random() > 0.8; // Add some random pauses
        }
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

        if (arpNotes.length == 0) {
            randomize(synthIndex);
            return;
        }

        for (int i=0; i<16; i++) {
            int degree = progression[(i/4) % progression.length];
            chord = new Harmony().getNotesInChord(0, scale, degree-1, 3, 0);
            arpNotes = new Harmony().arpeggiate(chord, 2);
            if (arpNotes.length > 0) {
                arpeggio[i] = arpNotes[i % arpNotes.length];
            }
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
