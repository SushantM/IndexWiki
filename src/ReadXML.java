
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Uses the SAX parser to parse the xml corpus of Wikipedia
 * Calls LineProcess class to tokenise
 * and PrimaryCreator to created Indexes
 * @author sushant
 *
 */
public class ReadXML extends DefaultHandler {
	boolean pageFlag,
			idFlag,
			titleFlag,
			bodyFlag,
			infoFlag,
			catagoryFlag,
			refFlag,
			linkFlag,
			valid;
	int pageId,found,fileN;
	StringBuilder buffer = new StringBuilder("");
	StringBuilder title = new StringBuilder("");
	StringBuilder body = new StringBuilder();
	LineProcess processor=new LineProcess();
	static String outFileDir;
	int p;// current page count, when equals a threshold, dumps the posting list to disk
	long time;

	public void initiate(String args[]) throws Exception {
		
		deleteTempFiles(1500);
		File tempFile1 = new File(outFileDir+"/titles");
		if( tempFile1.exists() )
			tempFile1.delete();
		time = System.currentTimeMillis();
		XMLReader xr = XMLReaderFactory.createXMLReader();
		ReadXML handler = new ReadXML();
		
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);
		if( args.length < 2 )
			return;
		File handle = new File( args[0] );
		if( !handle.exists() ){
			return;
		}
	 	FileReader r = new FileReader(args[0]);
	 	outFileDir=args[1];

		try {
			xr.parse(new InputSource(r));
		}
		catch(Exception e ) {
			System.out.println("Invalid XML");
		}
	    System.out.println((System.currentTimeMillis() - time) / 1000f + " sec of total time");
    }
    public ReadXML () {
    	super();
    	pageFlag = false;
    	idFlag = false;
    	titleFlag  = false;
    	bodyFlag = false;
		infoFlag = false;
		catagoryFlag = false;
		refFlag = false;
		linkFlag = false;
		valid = false;
    	found = 0;
    	p=0;
    }
    /**
     * Deletes first n temporary files
     * @param n
     */
    public void deleteTempFiles(int n) {

    	for( Integer i=0; i<n; i++ ) {
    		File tempFile = new File(outFileDir+"/tempDump_"+i.toString());
    		if( tempFile.exists() )
    			tempFile.delete();
    	}
    }

    /**	Start Element method of SAX parser
     * 
     */
    public void startElement (String uri, String name,
			      String qName, Attributes atts) throws SAXException {
  
    	body.setLength(0);
		if( qName.equalsIgnoreCase("page") ) {
			pageFlag = true;
			found = 0;
			p++;
		}
		//value of P can be changed based on Size of XML corpus
		// p=3000 performs normally on system with 4Gb ram.
		// if Memory overflow occurs, set value of p  to lesser value like 500
		if( p==3000 ) {
			try {
				processor.dumpOnDisk();
				System.gc();
			} catch (IOException e) {
			} catch (Exception e) {
			}
			processor.posting.clear();
			System.out.println("Dumped Index for "+ p+" pages to disk");
			p=0;
			
		}
		if( qName.equalsIgnoreCase("text") ) {
			bodyFlag = true;
			return;
		}
		if( pageFlag == true && qName.equalsIgnoreCase("id") )
			idFlag = true;
		
		if( pageFlag == true && qName.equalsIgnoreCase("title"))
			titleFlag = true;
    }


    public void endElement (String uri, String name, String qName) throws SAXException {
    }
    public void startDocument() throws SAXException {
     	processor.setOutDir(outFileDir);

    }
    public void endDocument() throws SAXException {
    	try {
			processor.dumpOnDisk();
		} catch (IOException e) {}
    	fileN = processor.getFileCount();
    	PrimaryCreator optimusPrime = new PrimaryCreator(fileN, outFileDir);
    	try {
			optimusPrime.createPrimaryIndex();
		} catch (IOException e) {}
    	deleteTempFiles(fileN);
    }

    public void characters (char ch[], int start, int length) throws SAXException {
    	
    	buffer.append(ch,start,length);

    	if( pageFlag == true ) {
    		if( idFlag == true) {
    			pageId = Integer.parseInt(buffer.toString());
    			idFlag = false;
    			found++;
    		}
    		if( titleFlag == true ) {
    			title.setLength(0);
				title.append(buffer);
				
				titleFlag = false;
				found++;
    		}
    		if( found==2 ) {
    			pageFlag = false;
    		
    			try {
    		 		FileWriter fwr= new FileWriter( new File (outFileDir+"/titles"),true );
    		 		BufferedWriter wr = new BufferedWriter (fwr);
    		 		StringBuilder titleLine=new StringBuilder();
    		 		titleLine.append(pageId);
    		 		titleLine.append('=');
    		 		titleLine.append(title);
 
    		 		wr.write(titleLine.toString());
    		 		wr.write('\n');
    		 		wr.close();
					processor.parseLine( title, pageId, true );
					
				} catch (IOException e) {	}
    		}
    	}
    	else {
    		if( length-start > 2) {
    			
					try {
						processor.parseLine( buffer, pageId, false );
					} catch (IOException e) { }
    		}
    	}
    	buffer.setLength(0);
    }
}
