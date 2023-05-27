package org.boggle;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.tree.LexicographicTree;

import java.util.*;

public class Boggle {

	/*
	 * PRIVATE ATTRIBUTES
	 */
	private final int size;
	private final LexicographicTree dict;
	private final Graph<CharSommet, DefaultEdge> grid;

	private static class CharSommet {
		private final char letter;

		public CharSommet(char letter) {
			this.letter = letter;

		}
	}
	/*
	 * CONSTRUCTORS
	 */

	/**
	 * Constructor : creates a Boggle grid filled with random letters.
	 * @param size The size of the squared grid
	 * @param dict A dictionary of allowed words
	 */
	public Boggle(int size, LexicographicTree dict) {
		if (size < 1) {
			throw new IllegalArgumentException("La taille de la grille doit être supérieure à 0.");
		}

		this.size = size;
		this.dict = dict;
		StringBuilder letters = new StringBuilder();
		for(int i = 0; i < size*size; i++) {
			letters.append(getRandomLetter());
		}
		this.grid = buildGrid(size, letters.toString());
	}








	/**
	 * Constructor : creates a Boggle grid filled with the supplied letters.
	 * @param size The size of the squared grid
	 * @param letters A string containing the (size x size) letters used to fill the grid
	 * @param dict A dictionary of allowed words
	 */
	public Boggle(int size, String letters, LexicographicTree dict) {
		if (size < 1) {
			throw new IllegalArgumentException("La taille de la grille doit être supérieure à 0.");
		}

		if (letters.length() < size * size) {
			throw new IllegalArgumentException("Le nombre de lettres fournies est insuffisant pour remplir la grille.");
		}

		this.size = size;
		this.dict = dict;
		this.grid = buildGrid(size, letters);
	}

	private char getRandomLetter() {
		return "abcdefghijklmnopqrstuvwxyz".charAt((int) (Math.random() * 26));
	}

	private Graph<CharSommet, DefaultEdge> buildGrid(int size, String letters) {
		Graph<CharSommet, DefaultEdge> grid = new SimpleGraph<>(DefaultEdge.class);
		CharSommet[][] sommets = new CharSommet[size][size];

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				CharSommet sommet = new CharSommet(letters.charAt(i * size + j));
				sommets[i][j] = sommet;
				grid.addVertex(sommet);
			}
		}

		// Ajout des arêtes verticales, horizontales et diagonales
		//TODO
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				CharSommet sommet = sommets[i][j];
				int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
				int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};

				for (int k = 0; k < dx.length; k++) {
					int newX = i + dx[k];
					int newY = j + dy[k];

					if (newX >= 0 && newX < size && newY >= 0 && newY < size) {
						grid.addEdge(sommet, sommets[newX][newY]);
					}
				}
			}
		}

		return grid;
	}

	/*
	 * PUBLIC METHODS
	 */

	/**
	 * Returns the letters in the Boggle grid.
	 * @return a string of letters
	 */
	public String letters() {
		StringBuilder sb = new StringBuilder();
		for(CharSommet sommet : grid.vertexSet()) {
			sb.append(sommet.letter);
		}
		return sb.toString();
	}

	/**
	 * Determines if a word can be found in the Boggle grid.
	 * @param word a word
	 * @return true if the word is present, false otherwise
	 */
	public boolean contains(String word) {
		for (CharSommet sommet : grid.vertexSet()) {
			if (searchWordFromVertex(sommet, word, new HashSet<>())) {
				return true;
			}
		}
		return false;
	}

	private boolean searchWordFromVertex(CharSommet vertex, String word, HashSet<Object> visited) {
		if (word.isEmpty()) {
			return true;
		}

		if (visited.contains(vertex) || vertex.letter != word.charAt(0)) {
			return false;
		}

		visited.add(vertex);
		String remainingWord = word.substring(1);
		for (CharSommet neighbor : Graphs.neighborSetOf(grid, vertex)) {
			if (searchWordFromVertex(neighbor, remainingWord, visited)) {
				return true;
			}
		}
		visited.remove(vertex);

		return false;
	}


	/**
	 * Searches for words in the Boggle grid.
	 * @return the set of found words
	 */

	public Set<String> solve() {
		Set<String> result = new HashSet<>();
		for (CharSommet sommet : grid.vertexSet()) {
			Set<CharSommet> visited = new HashSet<>();
			StringBuilder currentWord = new StringBuilder();
			solveRecursively(sommet, visited, currentWord, result);
		}
		return result;
	}

	private void solveRecursively(CharSommet vertex, Set<CharSommet> visited, StringBuilder currentWord, Set<String> result) {
		visited.add(vertex);
		currentWord.append(vertex.letter);

		String currentWordStr = currentWord.toString();
		if (currentWord.length() >= 3 && dict.containsWord(currentWordStr)) {
			result.add(currentWordStr);
		}

		if (dict.isPrefix(currentWordStr)) {
			for (CharSommet neighbor : Graphs.neighborSetOf(grid, vertex)) {
				if (!visited.contains(neighbor)) {
					solveRecursively(neighbor, visited, currentWord, result);
				}
			}
		}

		visited.remove(vertex);
		currentWord.setLength(currentWord.length() - 1);
	}


	/**
	 * Returns a textual representation of the Boggle grid.
	 * @return a textual representation of the Boggle grid
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append("|");
			for (int j = 0; j < size; j++) {
				int finalI = i;
				int finalJ = j;
				char letter = grid.vertexSet()
						.stream()
						.filter(sommet -> sommet.letter == letters().charAt(finalI * size + finalJ))
						.findFirst()
						.orElse(new CharSommet('\0'))
						.letter;
				if (letter != '\0') {
					sb.append(letter);
				} else {
					sb.append(" ");
				}
				sb.append("|");
			}
			sb.append("\n");
		}
		return sb.toString();
	}



	/*
	 * MAIN PROGRAM
	 */

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		System.out.println("Loading dictionary...");
		LexicographicTree dictionary = new LexicographicTree("src/main/resources/mots/dictionnaire_FR_sans_accents.txt");
		long loadDictTime = System.currentTimeMillis();
		System.out.println("Duration : " + (loadDictTime - startTime)/1000.0);
		System.out.println("Number of words : " + dictionary.size());
		System.out.println();

		// Sample grids
		String grid4x4 = "rhreypcswnsntego"; // Wikipedia example
		Boggle boggle1 = new Boggle(4, grid4x4, dictionary);
		printSolve(boggle1);


		String grid10x10 = "eymmccsrltjttsdiraoarliuniepeousrcgoiseerreeistiedtomcteevcmkaualilaretneerectresieenspgizeoeceecuds";
		Boggle boggle2 = new Boggle(10, grid10x10, dictionary);
		printSolve(boggle2);

		String grid20x20 = "eymmccsrltjttsdiraoarliuniepeousrcgoiseerreeistiedtomcteevcmkaualilaretneerectresieenspgizeoeceecudsrrsrvfianrsicwtdieioeiufnidlaaeeoeieitmntleavieacalischvzeatuisiupatolauaernetasatttadvtthzraaneuzfpneenabiielhcnitesaouelsenxrtojlcastieklkrupeletaiztleapqgaeocbpteutnetrtozatluuarapepsvipesxolteatmylttumelctahsowlsadoelouamisparejpmuaasoaeszsuilubrdrannyosfewnolneudpatcrwatblttpensaaunvkslrekiittc";
		Boggle boggle3 = new Boggle(20, grid20x20, dictionary);
		printSolve(boggle3);

		String grid50x50 = "eymmccsrltjttsdiraoarliuniepeousrcgoiseerreeistiedtomcteevcmkaualilaretneerectresieenspgizeoeceecudsrrsrvfianrsicwtdieioeiufnidlaaeeoeieitmntleavieacalischvzeatuisiupatolauaernetasatttadvtthzraaneuzfpneenabiielhcnitesaouelsenxrtojlcastieklkrupeletaiztleapqgaeocbpteutnetrtozatluuarapepsvipesxolteatmylttumelctahsowlsadoelouamisparejpmuaasoaeszsuilubrdrannyosfewnolneudpatcrwatblttpensaaunvkslrekiittciivsomuestiurfuaxreeunuennetemubenanvsucimozentlvptnsoyaoatospesvaesasyysdlbdoraguhpleonvfrelentickiwzrnmimsaeimralovhetscejsdsnrtcsgporubtewesdklorlvteselauxieusieetfmiplllneuyprlpiiujiewverneussnnaxoaswclermderupyurmaareuescriqesbeeadldnlhtsnaucxeadstciqneeetcwtctcltavxgiiuorlomewbleeaoanrjeqeaqhzetmamisirasceranivleteeuaedeaatnsostwtbtonuasilsodhxsmnetecuoesepmotlndamvdcaeebiualneltdrtnwgerifterpepdetdbgollulneoynesonnrpesaustieundaevansmspaisinusitiaagrhoaeeewotnlagtlinjdssnocmeigvultkamnarvcloohslgiueawnyterddduepeislsmaemaiensuytiraesliehotcmaeoeovtsoiostialfertapbuptefeeleeonkeeectcdtneuidrlrpeenmeauvztltsetaeidlsrgscvlsenmetyeoueqesassooiajprrsytioqesugwvatixluutotimwlpesreeicylreeeseauueeeeapornntulivlonansipvoeeactiuecmeudnenrqaieordhluomrtsrmetetswlieqcmltslvsadeuspglmyruteoixiuoepdnectntentdaualdpcsoaeljvonkeftneiuedeeztsatencaectoeptluatriocdocrdtmudleueornptmeintlzejaaaneeradibraeaoaanpoisieeurtettrxvtneoegleltagkasosrastluadxsepnlsaadoaiepjswyedatmrsnivmriseaweinvatepciuuesssnllsssmixlesiedettssyoeuipwltetitececoieeozweaenmlaoroospptusidpkdvsrnqaajituspuuleiisheiogeinpbbsitbsvetofsncnaetaowooekmuntavroonjraduuacknoqqknnnrjoopeeofzdyseaoltsclvaaapiueceauofcdbntmxtneetpoitwiinfaeltgueeispzeacneqmviaiusaettplhiuaetqaewtfuuipoueuesnsoxaixaeeyavqllssqareessnmeolsetlvttbpbeoosesiuincpnersriiterrincnhsemaunvaseeueprldkiecnwtisultmmenensaojgidntetselyzagtctaisiraipzegeienjreosuuszuynlnpooesurddauuuitnouspiiuaeeqerelumdalnohdhueuuiiaiaaltlunnsnamleoprecysucviuirsatenctssjeniinreuenvirsntrwzntaeeieouapntlmayotrpsuunnuiptsxaevplkmuruasocuimontijceksmaeaaearurosaeitpimvtityevsualpsosallkkimiaaievplozjirncedcismssamnerotsprnltlhfiokolroleeaejexjslihseaoelqnsrwizluirhuraarefssdsealtkuediqtdpwekselinealineozeremtjandnrerracqoakiltrcsnwataavalommuslrdqawqpcneaiotajsaiedrkoxtasfyvermeyrnaibrdeiixlefsesvsqrlobkatcptiuxpmvanohcedlemkgevsuoexjjmenoteatptylewesoeotzbveiugseaswoeueoirpupdpulsidsiosueeealdepeltuwssipsecinicloeantylscemtsbairodutathtceeutmrsiarnptamasrrildiuwntaisaatculursrgeierrheeiteacuroruyfretvcxegadiiunguenunubreuflnccretdeetwmdunttrosyntooieeeutvenra";
		Boggle boggle4 = new Boggle(50, grid50x50, dictionary);
		printSolve(boggle4);










	}

	private static void printSolve(Boggle boggle) {
		System.out.printf("Boggle grid : %d x %d%n", boggle.size, boggle.size);
		System.out.println(boggle);
		long loadDictTime = System.currentTimeMillis();
		Set<String> results = boggle.solve();
		long solveTime = System.currentTimeMillis();
		System.out.println("Duration : " + (solveTime - loadDictTime)/1000.0);
		System.out.println("Number of words found : " + results.size());
		System.out.println(new TreeSet<>(results));

	}
}
