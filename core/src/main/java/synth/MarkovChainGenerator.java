package synth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MarkovChainGenerator {

    private Map<Integer, List<Integer>> transitionMatrix = new HashMap<>();
    private Random random = new Random();

    public void train(int[] pattern) {
        transitionMatrix.clear();
        for (int i = 0; i < pattern.length - 1; i++) {
            int currentNote = pattern[i];
            int nextNote = pattern[i + 1];
            if (!transitionMatrix.containsKey(currentNote)) {
                transitionMatrix.put(currentNote, new ArrayList<>());
            }
            transitionMatrix.get(currentNote).add(nextNote);
        }
    }

    public int[] generate(int length, int startNote) {
        int[] newPattern = new int[length];
        newPattern[0] = startNote;
        for (int i = 1; i < length; i++) {
            int previousNote = newPattern[i - 1];
            List<Integer> nextNotes = transitionMatrix.get(previousNote);
            if (nextNotes == null || nextNotes.isEmpty()) {
                // If there's no transition from the previous note, pick a random note from the matrix
                List<Integer> allNotes = new ArrayList<>(transitionMatrix.keySet());
                if (allNotes.isEmpty()) {
                    newPattern[i] = 0; // Default to a rest if the matrix is empty
                } else {
                    newPattern[i] = allNotes.get(random.nextInt(allNotes.size()));
                }
            } else {
                newPattern[i] = nextNotes.get(random.nextInt(nextNotes.size()));
            }
        }
        return newPattern;
    }
}
