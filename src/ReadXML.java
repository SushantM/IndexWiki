
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;


public class ReadXML extends DefaultHandler {
	
	boolean pageFlag, idFlag, titleFlag, bodyFlag;
	int pageId,found,fileN;
	StringBuilder buffer = new StringBuilder("");
	StringBuilder title = new StringBuilder("");
	LineProcess processor=new LineProcess();
	SecondIndex secIndex = new SecondIndex();
	static String outFileDir;
	int p;
	long time;

	//public static void main (String args[]) throws Exception {
	public void initiate(String args[]) throws Exception {
		
		deleteTempFiles(1500);
		
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
			e.printStackTrace();
		}
	    System.out.println((System.currentTimeMillis() - time) / 1000f + " sec of total time");
    }
    public ReadXML () {
    	super();
    	pageFlag = false;
    	idFlag = false;
    	titleFlag  = false;
    	bodyFlag = false;
    	found = 0;
    	p=0;
    }
    public void deleteTempFiles(int n) {
    	for( Integer i=0; i<n; i++ ) {
    		File tempFile = new File(outFileDir+"/tempDump_"+i.toString());
    		if( tempFile.exists() )
    			tempFile.delete();
    	}
    }
    public void startElement (String uri, String name,
			      String qName, Attributes atts) throws SAXException {
  
		if( qName.equalsIgnoreCase("page") ) {
			pageFlag = true;
			found = 0;
			p++;
		}
		if( p==500 ) {
			try {
				long t=System.currentTimeMillis();
				processor.dumpOnDisk();
				System.out.print("dumped "+processor.getFileCount()+" ");
				System.out.println((System.currentTimeMillis() - t) / 1000f + " sec");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			processor.posting.clear();
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
		

		/*if( 	qName.equalsIgnoreCase("revision")		||
				qName.equalsIgnoreCase("timestamp")		||
				qName.equalsIgnoreCase("contributor")	|| 
				qName.equalsIgnoreCase("username")		||
				qName.equalsIgnoreCase("comment")	)
			skip = true;*/
    }


    public void endElement (String uri, String name, String qName) throws SAXException {
    }
    public void startDocument() throws SAXException {
     	processor.setOutDir(outFileDir);
     	secIndex.setOutDir(outFileDir);
    }
    public void endDocument() throws SAXException {
    	try {
			processor.dumpOnDisk();
		} catch (IOException e) {}
    	fileN = processor.getFileCount();
    	System.out.print("Starting merge");
    	long t=System.currentTimeMillis();
    	PrimaryCreator optimusPrime = new PrimaryCreator(fileN, outFileDir);
    	try {
			optimusPrime.createPrimaryIndex();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println((System.currentTimeMillis() - t) / 1000f + " sec Merge complete");
    	deleteTempFiles(fileN);
    }

    public void characters (char ch[], int start, int length) throws SAXException {
    	
    	buffer.append(ch,start,length);

    	if( pageFlag == true ) {
    		if( idFlag == true) {
    			pageId = Integer.parseInt(buffer.toString());
    			//System.out.println("Page id is : "+ pageId);
    			idFlag = false;
    			found++;
    		}
    		if( titleFlag == true ) {
    			//System.out.println("Title is "+buffer);
    			title.setLength(0);
				title.append(buffer);
				
				titleFlag = false;
				found++;
    		}
    		if( found==2 ) {
    			pageFlag = false;
    			try {
					processor.parseLine( title, pageId, true );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    	else {
    		if( length-start > 2) {
    				//System.out.println("Other is "+buffer);
    				try {
						processor.parseLine( buffer, pageId, false );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    		}
    	}
    	buffer.setLength(0);
    }
}
