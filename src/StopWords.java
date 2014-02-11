
import java.util.HashSet;
/**
 * Standard set of English Stopwords
 * @author sushant
 *
 */
public class StopWords {
	int noOfStopWords;
	String[] stopSt = {	"a",	"able",		"about",	"across",	"after",
						"all",	"almost",	"also",		"am",		"among",
						"an",	"and",		"any",		"are",		"as",
						"at",	"be",		"because",	"been",		"but",
						"by",	"can",		"cannot",	"could",	"dear",
						"did",	"do",		"does",		"either",	"else",
						"ever",	"every",	"for",		"from",		"get",
						"got",	"had",		"has",		"have",		"he",
						"her",	"hers",		"him",		"his",		"how",
						"however","i",		"if",		"in",		"into",
						"is",	"it",		"its",		"just",		"least",
						"let",	"like",		"likely",	"may",		"me",
						"might","most",		"must",		"my",		"neither",
						"no",	"nor",		"not",		"of",		"off",
						"often","on",		"only",		"or",		"other",
						"our",	"own",		"rather",	"said",		"say",
						"says",	"she",		"should",	"since",	"so",
						"some",	"than",		"that",		"the",		"their",
						"them",	"then",		"there",	"these",	"they",
						"this",	"tis",		"to",		"too",		"twas",
						"us",	"wants",	"was",		"we",		"were",
						"what",	"when",		"where",	"which",	"while",
						"who",	"whom",		"why",		"will",		"with",
						"would","yet",		"you",		"your", 	"",
						"\n",	"\t",		"infobox",	"references","external",
						"category"	};
	HashSet <String> stop = new HashSet <String>();
	public StopWords() {
		int i;
		noOfStopWords = stopSt.length;
		for( i=0; i<noOfStopWords; i++)
			stop.add(stopSt[i]);
	}
	/**
	 * Returns true if word is a stopword or its lenght is less than 3
	 * false for valid words
	 * @param word
	 * @return
	 */
	public boolean isStopWord(StringBuilder word ) {
		if( word.length() < 3 )
			return true;
		return stop.contains(word.toString());
	}
}
