package boggle;

import org.boggle.Boggle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.tree.LexicographicTree;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class BoggleTest {
	private static final Set<String> EXPECTED_WORDS = new TreeSet<>(Arrays.asList("ces", "cesse", "cessent", "cresson", "ego", "encre",
			"encres", "engonce", "engoncer", "engonces", "esse", "gens", "gent", "gesse", "gnose", "gosse", "nes", "net", "nos", "once",
			"onces", "ose", "osent", "pre", "pres", "presse", "pressent", "ressent", "sec", "secs", "sen", "sent", "set", "son",
			"songe", "songent", "sons", "tenson", "tensons", "tes"));
	private static final String GRID_LETTERS = "rhreypcswnsntego";
	private static LexicographicTree dictionary = null;

	@BeforeAll
	public static void initTestDictionary() {
		System.out.print("Loading dictionary...");
		dictionary = new LexicographicTree("src/main/resources/mots/dictionnaire_FR_sans_accents.txt");
		System.out.println(" done.");
	}
	
	@Test
	void wikipediaExample() {
		Boggle b = new Boggle(4, GRID_LETTERS, dictionary);
		assertNotNull(b);
		assertEquals(GRID_LETTERS, b.letters());
		assertTrue(b.contains("songent"));
		assertFalse(b.contains("sono"));
		assertEquals(EXPECTED_WORDS, b.solve());
	}

	@Test
	void testConstructorWithRandomLetters() {
		Boggle b = new Boggle(4, dictionary);
		assertNotNull(b);
		assertEquals(16, b.letters().length());
	}

	@Test
	void testToString() {
		Boggle b = new Boggle(4, GRID_LETTERS, dictionary);
		String expected = "|r|h|r|e|\n|y|p|c|s|\n|w|n|s|n|\n|t|e|g|o|\n";
		assertEquals(expected, b.toString());
	}

	@Test
	void testSolveEmptyGrid() {
		Boggle b = new Boggle(4, "aaaaaaaaaaaaaaaa", dictionary);
		Set<String> result = b.solve();
		assertTrue(result.isEmpty());
	}

	@Test
	void testSolveWithInvalidSize() {
		assertThrows(IllegalArgumentException.class, () -> new Boggle(0, dictionary));
	}

	@Test
	void testSolveWithInvalidLetters() {
		assertThrows(IllegalArgumentException.class, () -> new Boggle(4, "abcdefghijklmno", dictionary));
	}



}
