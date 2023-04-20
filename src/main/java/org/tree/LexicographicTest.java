package org.tree;

import java.util.HashMap;
import java.util.Map;

public class LexicographicTest {
    private final Node root;
    private int size;

    private static class Node{
        private final Map<Character,Node> children;
        private boolean isEndOfWord;

        public Node() {
            children = new HashMap<>();
            isEndOfWord = false;
        }
    }

    public LexicographicTest() {
        root = new Node();
        size = 0;
    }

    public void insertWord(String word){
        if (word == null || word.isEmpty()) {
            return;
        }
        Node node = root;
        for (char c : word.toCharArray()) {
            Node child = node.children.get(c);
            if (child == null) {
                child = new Node();
                node.children.put(c, child);
            }
            node = child;
        }
        if (!node.isEndOfWord) {
            node.isEndOfWord = true;
            size++;
        }
    }


    private static void testDictionarySize() {
        final int MB = 1024 * 1024;
        System.out.print(Runtime.getRuntime().totalMemory()/MB + " / ");
        System.out.println(Runtime.getRuntime().maxMemory()/MB);

        LexicographicTest dico = new LexicographicTest();
        long count = 0;
        while (true) {
            dico.insertWord(numberToWordBreadthFirst(count));
            count++;
            if (count % MB == 0) {
                System.out.println(count / MB + "M -> " + Runtime.getRuntime().freeMemory()/MB);
            }
        }
    }

    private static String numberToWordBreadthFirst(long number) {
        String word = "";
        int radix = 13;
        do {
            word = (char)('a' + (int)(number % radix)) + word;
            number = number / radix;
        } while(number != 0);
        return word;
    }

    public static void main(String[] args) {
        testDictionarySize();
    }

}
