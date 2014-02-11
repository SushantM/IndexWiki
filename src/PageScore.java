
/**
 * Data class
 * Stores the frequencies of a word for various Tag Elements
 * @author sushant
 *
 */
public class PageScore {
	int t;		//for title
	int b;		//for body
	int c;		//for category
	int i;		//for infobox
	int e;		//for external Links
	int r;		//for references
	public PageScore() {
		t=0;
		b=0;
		c=0;
		i=0;
		e=0;
		r=0;
	}
}
