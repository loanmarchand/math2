package org.tree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class LexicographicTree {

	/*
	 * PRIVATE ATTRIBUTES
	 */
	private final Node root;
	private int size;

	public static class Node {
		private Node[] children;
		private boolean isEndOfWord;

		public Node() {
			children = null;
			isEndOfWord = false;
		}


		public Node[] getChildren() {
			if (children == null) {
				children = new Node[28];
			}
			return children;
		}


		public boolean isEndOfWord() {
			return isEndOfWord;
		}

		public void setEndOfWord(boolean isEndOfWord) {
			this.isEndOfWord = isEndOfWord;
		}
	}
	/*
	 * CONSTRUCTORS
	 */

	/**
	 * Constructor : creates an empty lexicographic tree.
	 */
	public LexicographicTree() {
		root = new Node();
		size = 0;
	}

	/**
	 * Constructor : creates a lexicographic tree populated with words
	 * @param filename A text file containing the words to be inserted in the tree
	 */
	public LexicographicTree(String filename) {
		this();
		try(BufferedReader reader = new BufferedReader(new FileReader(filename))){
			String line;
			while ((line = reader.readLine()) != null){
				insertWord(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * PUBLIC METHODS
	 */

	/**
	 * Returns the number of words present in the lexicographic tree.
	 * @return The number of words present in the lexicographic tree
	 */
	public int size() {
		return size;
	}


	/**
	 * Inserts a word in the lexicographic tree if not already present.
	 * @param word A word
	 */
	public void insertWord(String word) {
		word = sanitize(word);
		Node current = root;
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			if (!Character.isLetter(c) && c != '-' && c != '\'') {
				continue;
			}
			Node[] children = current.getChildren();
			Node child = children[getIndex(c)];
			if (child == null) {
				child = new Node();
				children[getIndex(c)] = child;
			}
			current = child;
		}

		if (!current.isEndOfWord()) {
			current.setEndOfWord(true);
			size++;
		}
	}


	/**
	 * Determines if a word is present in the lexicographic tree.
	 * @param word A word
	 * @return True if the word is present, false otherwise
	 */
	public boolean containsWord(String word) {
		word = sanitize(word);
		Node current = getNode(word);
		if (current == null) return false;

		return current.isEndOfWord();
	}



	public boolean isPrefix(String word) {
		Node current = getNode(word);
		return current != null;
	}

	/**
	 * Returns an alphabetic list of all words starting with the supplied prefix.
	 * If 'prefix' is an empty string, all words are returned.
	 * @param prefix Expected prefix
	 * @return The list of words starting with the supplied prefix
	 */
	public List<String> getWords(String prefix) {
		prefix = sanitize(prefix);
		List<String> words = new ArrayList<>();
		Node current = root;

		// On parcourt le préfixe
		for (int i = 0; i < prefix.length(); i++) {
			char c = prefix.charAt(i);
			Node[] children = current.getChildren();
			Node child = children[getIndex(c)];
			if (child == null) {
				// Si un caractère du préfixe ne correspond à aucun nœud de l'arbre, on retourne une liste vide
				return words;
			}
			current = child;
		}
		getWords(current, new StringBuilder(prefix), words);
		return words;
	}


	/**
	 * Returns an alphabetic list of all words of a given length.
	 * If 'length' is lower than or equal to zero, an empty list is returned.
	 * @param length Expected word length
	 * @return The list of words with the given length
	 */
	public List<String> getWordsOfLength(int length) {
		if (length <= 0) {
			return new ArrayList<>();
		}
		List<String> words = new ArrayList<>();
		char[] prefix = new char[length];
		getWordsOfLength(root, prefix, length, words, 0);
		return words;
	}






	/*
	 * PRIVATE METHODS
	 */

	private void getWords(Node node, StringBuilder prefix, List<String> words) {
		if (node.isEndOfWord()) {
			// Si le nœud correspond à la fin d'un mot, on l'ajoute à la liste
			words.add(prefix.toString());
		}

		// On parcourt tous les enfants du nœud actuel
		for (char c = 'a'; c <= 'z'; c++) {
			Node[] children = node.getChildren();
			Node child = children[c - 'a'];
			if (child != null) {
				// Si l'enfant existe, on l'ajoute au préfixe et on appelle la méthode récursive avec ce nœud
				prefix.append(c);
				getWords(child, prefix, words);
				prefix.deleteCharAt(prefix.length() - 1);
			}
		}

		// On gère les cas spéciaux pour les caractères "-" et "'"
		Node[] children = node.getChildren();
		Node child = children[26];
		if (child != null) {
			prefix.append('-');
			getWords(child, prefix, words);
			prefix.deleteCharAt(prefix.length() - 1);
		}

		child = children[27];
		if (child != null) {
			prefix.append('\'');
			getWords(child, prefix, words);
			prefix.deleteCharAt(prefix.length() - 1);
		}

	}

	private void getWordsOfLength(Node node, char[] prefix, int length, List<String> words, int i) {
		if (node.isEndOfWord() && i == length) {
			words.add(new String(prefix));
		}

		if (i == length) {
			return;
		}

		Node[] children = node.getChildren();
		for (int j = 0; j < children.length; j++) {
			Node child = children[j];
			if (child != null) {
				prefix[i] = (char) (j == 26 ? '-' : j == 27 ? '\'' : j + 'a');
				getWordsOfLength(child, prefix, length, words, i + 1);
			}
		}
	}

	private Node getNode(String word) {
		word = sanitize(word);
		Node current = root;
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			Node[] children = current.getChildren();
			if (children.length <= getIndex(c)) {
				return null;
			}
			Node child = children[getIndex(c)];
			if (child == null) {
				return null;
			}
			current = child;
		}
		return current;
	}

	private String sanitize(String word) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			if (Character.isLetter(c) || c == '-' || c == '\'') {
				sb.append(c);
			}
		}
		return sb.toString().toLowerCase();
	}

	private int getIndex(char c) {
		return c == '-' ? 26 : c == '\'' ? 27 : c - 'a';
	}




	/*
	 * TEST FUNCTIONS
	 */


	private static String numberToWordBreadthFirst(long number) {
		String word = "";
		int radix = 13;
		do {
			word = (char)('a' + (int)(number % radix)) + word;
			number = number / radix;
		} while(number != 0);
		return word;
	}

	private static void testDictionaryPerformance(String filename) {
		long startTime;
		int repeatCount = 20;

		// Create tree from list of words
		startTime = System.currentTimeMillis();
		System.out.println("Loading dictionary...");
		LexicographicTree dico = null;
		for (int i = 0; i < repeatCount; i++) {
			dico = new LexicographicTree(filename);
		}
		System.out.println("Load time : " + (System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println("Number of words : " + dico.size());
		System.out.println();

		// Search existing words in dictionary
		startTime = System.currentTimeMillis();
		System.out.println("Searching existing words in dictionary...");
		File file = new File(filename);
		for (int i = 0; i < repeatCount; i++) {
			Scanner input;
			try {
				input = new Scanner(file);
				while (input.hasNextLine()) {
					String word = input.nextLine();
					boolean found = dico.containsWord(word);
					if (!found) {
						System.out.println(word + " / " + word.length() + " -> " + found);
					}
				}
				input.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Search time : " + (System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println();

		// Search non-existing words in dictionary
		startTime = System.currentTimeMillis();
		System.out.println("Searching non-existing words in dictionary...");
		for (int i = 0; i < repeatCount; i++) {
			Scanner input;
			try {
				input = new Scanner(file);
				while (input.hasNextLine()) {
					String word = input.nextLine() + "xx";
					boolean found = dico.containsWord(word);
					if (found) {
						System.out.println(word + " / " + word.length() + " -> " + found);
					}
				}
				input.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Search time : " + (System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println();

		// Search words of increasing length in dictionary
		startTime = System.currentTimeMillis();
		System.out.println("Searching for words of increasing length...");
		for (int i = 0; i < 4; i++) {
			int total = 0;
			for (int n = 0; n <= 28; n++) {
				int count = dico.getWordsOfLength(n).size();
				total += count;
			}
			if (dico.size() != total) {
				System.out.printf("Total mismatch : dict size = %d / search total = %d\n", dico.size(), total);
			}
		}
		System.out.println("Search time : " + (System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println();
	}

	private static void testDictionarySize() {
		final int MB = 1024 * 1024;
		System.out.print(Runtime.getRuntime().totalMemory()/MB + " / ");
		System.out.println(Runtime.getRuntime().maxMemory()/MB);

		LexicographicTree dico = new LexicographicTree();
		long count = 0;
		while (true) {
			dico.insertWord(numberToWordBreadthFirst(count));
			count++;
			if (count % MB == 0) {
				System.out.println(count / MB + "M -> " + Runtime.getRuntime().freeMemory()/MB);
			}
		}
	}

	/*
	 * MAIN PROGRAM
	 */

	public static void main(String[] args) {
		// CTT : test de performance insertion/recherche
		testDictionaryPerformance("src/main/resources/mots/dictionnaire_FR_sans_accents.txt");

		// CST : test de taille maximale si VM -Xms2048m -Xmx2048m
		testDictionarySize();
	}
}
