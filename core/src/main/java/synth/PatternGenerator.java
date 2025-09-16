package synth;

import com.acid.DrumData;
import com.acid.SequencerData;
import com.acid.Statics;

import java.util.HashMap;
import java.util.Map;

public class PatternGenerator {

    private static Map<String, Map<String, Map<String, int[]>>> synthGenreBank;
    private static Map<String, Map<String, int[][]>> drumGenreBank;

    static {
        // Synth Patterns
        synthGenreBank = new HashMap<>();

        // House
        Map<String, Map<String, int[]>> houseBanks = new HashMap<>();
        Map<String, int[]> houseBankA = new HashMap<>();
        houseBankA.put("Bassline 1", new int[]{36, 0, 0, 36, 0, 0, 36, 0, 36, 0, 0, 36, 0, 0, 36, 0});
        houseBankA.put("Melody 1", new int[]{48, 50, 52, 50, 48, 50, 52, 50, 48, 50, 52, 50, 48, 50, 52, 50});
        houseBanks.put("A", houseBankA);
        synthGenreBank.put("House", houseBanks);

        // Techno
        Map<String, Map<String, int[]>> technoBanks = new HashMap<>();
        Map<String, int[]> technoBankA = new HashMap<>();
        technoBankA.put("Bassline 1", new int[]{36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36});
        technoBankA.put("Stab 1", new int[]{60, 0, 60, 0, 60, 0, 60, 0, 60, 0, 60, 0, 60, 0, 60, 0});
        technoBanks.put("A", technoBankA);
        synthGenreBank.put("Techno", technoBanks);


        // Drum Patterns
        drumGenreBank = new HashMap<>();

        Map<String, int[][]> houseDrumBanks = new HashMap<>();
        houseDrumBanks.put("A", new int[][]{
                {1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0},
                {0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1},
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
    }

    public static String[] getGenres() {
        return synthGenreBank.keySet().toArray(new String[0]);
    }

    public static String[] getBanks(String genre) {
        if (synthGenreBank.get(genre) == null) return new String[0];
        return synthGenreBank.get(genre).keySet().toArray(new String[0]);
    }

    public static String[] getPatterns(String genre, String bank) {
        if (synthGenreBank.get(genre) == null || synthGenreBank.get(genre).get(bank) == null) return new String[0];
        return synthGenreBank.get(genre).get(bank).keySet().toArray(new String[0]);
    }

    public static int[] getPattern(String genre, String bank, String name) {
        return synthGenreBank.get(genre).get(bank).get(name);
    }

    public static String[] getDrumGenres() {
        return drumGenreBank.keySet().toArray(new String[0]);
    }

    public static String[] getDrumBanks(String genre) {
        if (drumGenreBank.get(genre) == null) return new String[0];
        return drumGenreBank.get(genre).keySet().toArray(new String[0]);
    }

    public static int[][] getDrumPattern(String genre, String bank) {
        if (drumGenreBank.containsKey(genre) && drumGenreBank.get(genre).containsKey(bank)) {
            return drumGenreBank.get(genre).get(bank);
        }
        return null;
    }

    public static void applySynthPattern(int[] pattern, int synthIndex) {
        if (pattern == null) return;
        SequencerData sd = new SequencerData(synthIndex);
        for (int i = 0; i < 16; i++) {
            sd.note[i] = (byte) (pattern[i % pattern.length]);
            sd.pause[i] = pattern[i % pattern.length] == 0;
        }
        sd.refresh();
        ((BasslineSynthesizer)Statics.output.getTrack(synthIndex)).initOscillator();
    }

    public static void applySynthPattern(int[] pattern, boolean[] pauses, boolean[] accents, boolean[] slides, int synthIndex) {
        if (pattern == null) return;
        SequencerData sd = new SequencerData(synthIndex);
        for (int i = 0; i < 16; i++) {
            sd.note[i] = (byte) (pattern[i % pattern.length]);
            sd.pause[i] = pauses[i];
            sd.accent[i] = accents[i];
            sd.slide[i] = slides[i];
        }
        sd.refresh();
        ((BasslineSynthesizer)Statics.output.getTrack(synthIndex)).initOscillator();
    }

    public static int[] mutatePattern(int[] pattern, int[] scale, float mutationRate) {
        int[] mutatedPattern = new int[pattern.length];
        System.arraycopy(pattern, 0, mutatedPattern, 0, pattern.length);

        for (int i = 0; i < mutatedPattern.length; i++) {
            if (Math.random() < mutationRate) {
                // Mutate this note
                int randomNoteFromScale = scale[(int) (Math.random() * scale.length)];
                int octave = (int) (Math.random() * 3) + 2; // C3 to C5
                mutatedPattern[i] = randomNoteFromScale + (12 * octave);
            }
        }
        return mutatedPattern;
    }

    public static void mutateRhythm(boolean[] pauses, float mutationRate) {
        for (int i = 0; i < pauses.length; i++) {
            if (Math.random() < mutationRate) {
                pauses[i] = !pauses[i];
            }
        }
    }

    public static void mutateAccents(boolean[] accents, float mutationRate) {
        for (int i = 0; i < accents.length; i++) {
            if (Math.random() < mutationRate) {
                accents[i] = !accents[i];
            }
        }
    }

    public static void mutateSlides(boolean[] slides, float mutationRate) {
        for (int i = 0; i < slides.length; i++) {
            if (Math.random() < mutationRate) {
                slides[i] = !slides[i];
            }
        }
    }

    public static int[] arpeggiate(int[] pattern, int octaves, String direction) {
        java.util.ArrayList<Integer> notes = new java.util.ArrayList<Integer>();
        for (int i = 0; i < pattern.length; i++) {
            if (pattern[i] != 0 && !notes.contains(pattern[i])) {
                notes.add(pattern[i]);
            }
        }

        if (notes.isEmpty()) {
            return new int[16]; // Return empty pattern if no notes
        }

        java.util.Collections.sort(notes);

        java.util.ArrayList<Integer> arpNotes = new java.util.ArrayList<Integer>();
        if (direction.equalsIgnoreCase("up")) {
            for (int o = 0; o < octaves; o++) {
                for (int note : notes) {
                    arpNotes.add(note + (12 * o));
                }
            }
        } else if (direction.equalsIgnoreCase("down")) {
            for (int o = 0; o < octaves; o++) {
                for (int i = notes.size() - 1; i >= 0; i--) {
                    arpNotes.add(notes.get(i) + (12 * o));
                }
            }
        } else { // "up-down"
            for (int o = 0; o < octaves; o++) {
                for (int note : notes) {
                    arpNotes.add(note + (12 * o));
                }
                for (int i = notes.size() - 2; i > 0; i--) {
                    arpNotes.add(notes.get(i) + (12 * o));
                }
            }
        }

        if (arpNotes.isEmpty()) {
            return new int[16]; // Return empty pattern if no notes to arpeggiate
        }

        int[] arpeggiatedPattern = new int[16];
        for (int i = 0; i < 16; i++) {
            arpeggiatedPattern[i] = arpNotes.get(i % arpNotes.size());
        }

        return arpeggiatedPattern;
    }
}
