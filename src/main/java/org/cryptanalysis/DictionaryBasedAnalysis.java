package org.cryptanalysis;

import org.tree.LexicographicTree;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DictionaryBasedAnalysis {

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DICTIONARY = "src/main/resources/mots/dictionnaire_FR_sans_accents.txt";

    private static final String CRYPTOGRAM_FILE = "src/main/resources/txt/Plus fort que Sherlock Holmes (cryptogram).txt";
    private static final String DECODING_ALPHABET = "VNSTBIQLWOZUEJMRYGCPDKHXAF"; // Sherlock

    private String cryptogram;
    private LexicographicTree dict;

    /*
     * CONSTRUCTOR
     */
    public DictionaryBasedAnalysis(String cryptogram, LexicographicTree dict) {
        this.cryptogram = cryptogram;
        this.dict = dict;
    }

    /*
     * PUBLIC METHODS
     */

    /**
     * Performs a dictionary-based analysis of the cryptogram and returns an approximated decoding alphabet.
     *
     * @param alphabet The decoding alphabet from which the analysis starts
     * @return The decoding alphabet at the end of the analysis process
     */
    public String guessApproximatedAlphabet(String alphabet) {
        // 1. On découpe le cryptogramme en mots et on classe ces mots à traiter par longueur décroissante.
        List<String> words = new ArrayList<>(Arrays.asList(cryptogram.split("[^a-zA-Z]")));
        Map<Character, Character> alphabetMap = new HashMap<>();
        String alphabetAprox= " ".repeat(26);

        words.removeIf(w -> w.length() < 3);
        //trier par ordre de longueur décroissant
        words.sort((w1, w2) -> w2.length() - w1.length());
        //4. On applique le processus jusqu'à avoir épuisé tous les mots du cryptogramme.
        for (String word : words) {
            //3. On prend le mot le plus long et on cherche dans le dictionnaire les mots de même longueur.
            List<String> wordsOfLengths = dict.getWordsOfLength(word.length());
            //On retire les mots qui ont autre chose que des lettres
            //5. On prend le mot compatible le plus fréquent dans le dictionnaire.
            List<String> compatibleWords = getCompatibleWord(word, wordsOfLengths);
            if(compatibleWords.size()>0){
                getAlphabet(word,compatibleWords.get(0),alphabetMap);
            }
            alphabetAprox = modifyAlphabet(alphabetMap);
            //Compter le nombre d'espace dans alphabetAprox
            int nbSpace = 0;
            for (int i = 0; i < alphabetAprox.length(); i++) {
                if(alphabetAprox.charAt(i) == ' '){
                    nbSpace++;
                }
            }
            if(nbSpace <=5){
                return guessAlphabet(alphabetAprox, words);
            }
        }

        return alphabetAprox;

    }

    private String guessAlphabet(String alphabet, List<String> words) {
        // Utiliser un HashSet pour une suppression plus rapide
        Set<Character> missingLetters = new HashSet<>();
        for (char c : LETTERS.toCharArray()){
            missingLetters.add(c);
        }
        for (char c : alphabet.toCharArray()){
            if(c != ' '){
                missingLetters.remove(c);
            }
        }
        String alphabetAprox = alphabet;

        List<String> permutations = generatePermutations(new ArrayList<>(missingLetters), "", missingLetters.size(), new ArrayList<>());
        int bestScore = 0;
        for (String permutation : permutations) {
            String testAlphabet = insertPermutationIntoAlphabet(alphabet, permutation);
            int nbWords = testAlphabet(testAlphabet, words);
            if (nbWords > bestScore) {
                bestScore = nbWords;
                alphabetAprox = testAlphabet;
            }
        }

        return alphabetAprox;
    }

    /**
     * Generates all the permutations of the given letters.
     * @param letters The letters to permute
     * @param current The current permutation
     * @param size The size of the permutation
     * @param result The list of permutations
     * @return The list of permutations
     */
    private List<String> generatePermutations(List<Character> letters, String current, int size, List<String> result) {
        if (current.length() == size) {
            result.add(current);
        } else {
            for (char c : letters) {
                // Utiliser un StringBuilder pour une concaténation plus rapide
                if (!current.contains(String.valueOf(c))) {
                    generatePermutations(letters, current + c, size, result);
                }
            }
        }
        return result;
    }


    private String insertPermutationIntoAlphabet(String alphabet, String permutation) {
        StringBuilder result = new StringBuilder(alphabet);
        int permutationIndex = 0;
        for (int i = 0; i < result.length(); i++) {
            if (result.charAt(i) == ' ') {
                result.setCharAt(i, permutation.charAt(permutationIndex));
                permutationIndex++;
            }
        }
        return result.toString();
    }

    public int testAlphabet(String alphabet, List<String> words) {
        int nbWords = 0;
        for (String word : words) {
            if (dict.containsWord(applySubstitution(word,alphabet))) {
                nbWords++;
            }
        }
        return nbWords;
    }

    private String modifyAlphabet(Map<Character, Character> alphabetMap) {
        StringBuilder string = new StringBuilder();
        for (char c : LETTERS.toCharArray()){
            string.append(alphabetMap.getOrDefault(c, ' '));
        }
        return string.toString();
    }


    private void getAlphabet(String encryptedWord, String originalWord, Map<Character, Character> alphabet) {
        for (int i = 0; i < encryptedWord.length(); i++) {
            char encryptedChar = encryptedWord.charAt(i);
            char originalChar = originalWord.charAt(i);

            alphabet.putIfAbsent(encryptedChar, originalChar);
        }

    }

    private List<String> getCompatibleWord(String firstWord, List<String> words) {
        List<String> compatibleWords = new ArrayList<>();

        for (String word : words) {
            Map<Character, Character> mapping = new HashMap<>();
            boolean isCompatible = true;

            for (int i = 0; i < word.length(); i++) {
                char wordChar = word.charAt(i);
                char firstWordChar = firstWord.charAt(i);

                if (mapping.containsKey(wordChar)) {
                    if (mapping.get(wordChar) != firstWordChar) {
                        isCompatible = false;
                        break;
                    }
                } else {
                    if (mapping.containsValue(firstWordChar)) {
                        isCompatible = false;
                        break;
                    } else {
                        mapping.put(wordChar, firstWordChar);
                    }
                }
            }

            if (isCompatible) {
                compatibleWords.add(word.toUpperCase());
                // Arreter tôt dès qu'on trouve un mot compatible
                break;
            }
        }

        return compatibleWords;
    }



    /**
     * Applies an alphabet-specified substitution to a text.
     *
     * @param text     A text
     * @param alphabet A substitution alphabet
     * @return The substituted text
     */
    public static String applySubstitution(String text, String alphabet) {
        StringBuilder substituted = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                int index = LETTERS.indexOf(Character.toUpperCase(c));
                if (index >= 0 && index < alphabet.length()) {
                    char newChar = (Character.isUpperCase(c) ? alphabet.charAt(index) : Character.toLowerCase(alphabet.charAt(index)));
                    substituted.append(newChar);
                } else {
                    substituted.append(c);
                }
            } else {
                substituted.append(c);
            }
        }

        return substituted.toString();
    }



    /*
     * PRIVATE METHODS
     */

    /**
     * Compares two substitution alphabets.
     *
     * @param a First substitution alphabet
     * @param b Second substitution alphabet
     * @return A string where differing positions are indicated with an 'x'
     */
    private static String compareAlphabets(String a, String b) {
        String result = "";
        for (int i = 0; i < a.length(); i++) {
            result += (a.charAt(i) == b.charAt(i)) ? " " : "x";
        }
        return result;
    }

    /**
     * Load the text file pointed to by pathname into a String.
     *
     * @param pathname A path to text file.
     * @param encoding Character set used by the text file.
     * @return A String containing the text in the file.
     * @throws IOException
     */
    private static String readFile(String pathname, Charset encoding) {
        String data = "";
        try {
            data = Files.readString(Paths.get(pathname), encoding);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    /*
     * MAIN PROGRAM
     */

    public static void main(String[] args) {
        /*
         * Load dictionary
         */
        System.out.print("Loading dictionary... ");
        LexicographicTree dict = new LexicographicTree(DICTIONARY);
        System.out.println("done.");
        System.out.println();

        /*
         * Load cryptogram
         */
        String cryptogram = readFile(CRYPTOGRAM_FILE, StandardCharsets.UTF_8);
//		System.out.println("*** CRYPTOGRAM ***\n" + cryptogram.substring(0, 100));
//		System.out.println();
        List<String> words = Arrays.asList(cryptogram.split("[^A-Za-z]"));


        /*
         *  Decode cryptogram
         */
        DictionaryBasedAnalysis dba = new DictionaryBasedAnalysis(cryptogram, dict);
        String startAlphabet = LETTERS;
//		String startAlphabet = "ZISHNFOBMAVQLPEUGWXTDYRJKC"; // Random alphabet
        //calculer le temps d'execution
        long startTime = System.currentTimeMillis();
        String finalAlphabet = dba.guessApproximatedAlphabet(startAlphabet);
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        //afficher le temps d'execution en secondes
        System.out.println("Execution time in seconds: " + timeElapsed / 1000.0);

        // Display final results
        System.out.println();
        System.out.println("Decoding     alphabet : " + DECODING_ALPHABET);
        System.out.println("Approximated alphabet : " + finalAlphabet);
        System.out.println("Remaining differences : " + compareAlphabets(DECODING_ALPHABET, finalAlphabet));
        System.out.println();

        // Display decoded text
        System.out.println("*** DECODED TEXT ***\n" + applySubstitution(cryptogram, finalAlphabet).substring(0, 200));
        System.out.println();
    }
}
