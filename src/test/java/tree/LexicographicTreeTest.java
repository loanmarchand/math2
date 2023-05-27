package tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tree.LexicographicTree;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/* ---------------------------------------------------------------- */

/*
 * Constructor
 */
public class LexicographicTreeTest {
	private static final String[] WORDS = new String[] {"aide", "as", "au", "aux",
			"bu", "bus", "but", "et", "ete"};
	private LexicographicTree dict;

	public void initTestdictionary() {
		for (String word : WORDS) {
			dict.insertWord(word);
		}
	}

	@BeforeEach
	void setUp() {
		dict = new LexicographicTree();
	}

	@Test
	void constructor_Emptydictionary() {
		LexicographicTree dict = new LexicographicTree();
		assertNotNull(dict);
		assertEquals(0, dict.size());
	}

	@Test
	void insertWord_General() {
		LexicographicTree dict = new LexicographicTree();
		for (int i=0; i<WORDS.length; i++) {
			dict.insertWord(WORDS[i]);
			assertEquals(i+1, dict.size(), "Mot " + WORDS[i] + " non inséré");
			dict.insertWord(WORDS[i]);
			assertEquals(i+1, dict.size(), "Mot " + WORDS[i] + " en double");
		}
	}

	@Test
	void containsWord_General() {
		initTestdictionary();
		for (String word : WORDS) {
			assertTrue(dict.containsWord(word), "Mot " + word + " non trouvé");
		}
		for (String word : new String[] {"", "aid", "ai", "aides", "mot", "e"}) {
			assertFalse(dict.containsWord(word), "Mot " + word + " inexistant trouvé");
		}
	}
	@Test
	void getWords_General() {
		initTestdictionary();
		assertEquals(WORDS.length, dict.getWords("").size());
		assertArrayEquals(WORDS, dict.getWords("").toArray());

		assertEquals(0, dict.getWords("x").size());

		assertEquals(3, dict.getWords("bu").size());
		assertArrayEquals(new String[] {"bu", "bus", "but"}, dict.getWords("bu").toArray());
	}

	@Test
	void getWordsOfLength_General() {
		initTestdictionary();
		assertEquals(4, dict.getWordsOfLength(3).size());
		assertArrayEquals(new String[] {"aux", "bus", "but", "ete"}, dict.getWordsOfLength(3).toArray());
	}

	@Test
	void testEmptyTree() {
		assertEquals(0, dict.size());
	}

	@Test
	void testInsertWord() {
		dict.insertWord("chat");
		assertEquals(1, dict.size());
	}

	@Test
	void testInsertWordWithSpecialCharacters() {
		dict.insertWord("chien-berger");
		assertEquals(1, dict.size());
	}

	@Test
	void testInsertWordWithApostrophe() {
		dict.insertWord("aujourd'hui");
		assertEquals(1, dict.size());
	}

	@Test
	void testInsertDuplicateWord() {
		dict.insertWord("chat");
		dict.insertWord("chat");
		assertEquals(1, dict.size());
	}

	@Test
	void testContainsWord() {
		dict.insertWord("chat");
		assertTrue(dict.containsWord("chat"));
	}

	@Test
	void testContainsWordCaseInsensitive() {
		dict.insertWord("Chat");
		assertTrue(dict.containsWord("chat"));
	}

	@Test
	void testDoesNotContainWord() {
		assertFalse(dict.containsWord("chien"));
	}

	@Test
	void testIsPrefix() {
		dict.insertWord("chien");
		assertTrue(dict.isPrefix("chi"));
	}

	@Test
	void testIsNotPrefix() {
		dict.insertWord("chien");
		assertFalse(dict.isPrefix("cha"));
	}

	@Test
	void testGetWordsWithPrefix() {
		dict.insertWord("chat");
		dict.insertWord("chien");
		dict.insertWord("cheval");

		List<String> words = dict.getWords("ch");
		assertEquals(3, words.size());
		assertTrue(words.contains("chat"));
		assertTrue(words.contains("chien"));
		assertTrue(words.contains("cheval"));
	}

	@Test
	void testGetWordsWithNoMatchingPrefix() {
		dict.insertWord("chat");
		dict.insertWord("chien");
		dict.insertWord("cheval");

		List<String> words = dict.getWords("ci");
		assertEquals(0, words.size());
	}

	@Test
	void testGetWordsWithEmptyPrefix() {
		dict.insertWord("chat");
		dict.insertWord("chien");
		dict.insertWord("cheval");

		List<String> words = dict.getWords("");
		assertEquals(3, words.size());
		assertTrue(words.contains("chat"));
		assertTrue(words.contains("chien"));
		assertTrue(words.contains("cheval"));
	}


	@Test
	void testGetWordsOfZeroLength() {
		dict.insertWord("chat");
		dict.insertWord("chien");
		dict.insertWord("cheval");

		List<String> words = dict.getWordsOfLength(0);
		assertEquals(0, words.size());
	}

}
