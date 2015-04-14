import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.Date;

//XML Document Creation Libraries
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class xmlCreator{

    public static void main(String[] args) {
        final File folder = new File("/heroes/u1/rm934/Spring15Sports/sortedsports");
        listFilesForFolder(folder);
    }

    public static void listFilesForFolder(final File folder) {
        int game = 0;
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                //System.out.println(fileEntry.getName());
                //This get the # of line in the file.  Used to initialize
                //Sports array for each sport
                String[][] Sports = null;
                try{
                    LineNumberReader lnr = new LineNumberReader(new FileReader(fileEntry));
                    lnr.skip(Long.MAX_VALUE);
                    Sports = new String[lnr.getLineNumber()-1][4];
                    lnr.close();
                }catch (Exception e){
                    System.out.println(e.getMessage());
                    return;
                }

                try (BufferedReader br = new BufferedReader(new FileReader(fileEntry))){
                    String line;
                    line = br.readLine();
                    for (; (line = br.readLine()) != null;){
                        tokenizer(Sports,game,line);
                        ++game;
                    }
                }catch (IOException e){
                    System.out.println(e.getMessage());
                    return;
                }               
                //pass writeXMLfile the name of the sport(from file name) + the Sports array
                writeXMLfile(Sports, fileEntry.getName());
                game = 0;
            }
        }
    }

    public static void tokenizer(String[][] Sports, int game, String line){
        String token = "";
        int startIndex = 1, endIndex = 0;
        endIndex = line.indexOf('\"',1);
        for (int i = 0; i < 4; ++i){
            token = line.substring(startIndex,endIndex);
            Sports[game][i] = token.trim();
            startIndex = endIndex+3;
            endIndex = line.indexOf('\"',startIndex);
        } 
    }

    public static void writeXMLfile(String[][] Sports, String filename){
        try{
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            //root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("rss");
            doc.appendChild(rootElement);
            rootElement.setAttribute("xmlns:event","http://ruevents.rutgers.edu/events");
            rootElement.setAttribute("version","2.0");

            Element channel = doc.createElement("channel");
            rootElement.appendChild(channel);

            Element title = doc.createElement("title");
            title.appendChild(doc.createTextNode("Rutgers Athletics Schedules"));
            channel.appendChild(title);

            //Element link = doc.createElement("link");
            //link.appendChild(doc.createTextNode("http://www.scarletknights.com/schedule/sports.asp"));
            //channel.appendChild(link);

            Element description = doc.createElement("description");
            description.appendChild(doc.createTextNode("Schedule of Rutgers"));
            channel.appendChild(description);

            Element language = doc.createElement("language");
            language.appendChild(doc.createTextNode("en-us"));
            channel.appendChild(language);

            Element event = doc.createElement("event:itemCount");
            event.appendChild(doc.createTextNode(Sports.length+""));
            channel.appendChild(event);

            //TODO: modify every instace of the date to the correct format
            //e.g. 2014-08-24 13:00:00 Sun
            //Element[] item = new Element[Sports.length];
            
            for (int i = 0; i<Sports.length; ++i){
                Element item = doc.createElement("item");
                channel.appendChild(item);

                Element item_title = doc.createElement("title");
                item_title.appendChild(doc.createTextNode("Rutgers vs "+Sports[i][2]));
                item.appendChild(item_title);

                Element item_description = doc.createElement("description");
                item_description.appendChild(doc.createTextNode(Sports[i][3]+""));
                item.appendChild(item_description);

                Element item_beginDateTime = doc.createElement("event:beginDateTime");
                item_beginDateTime.appendChild(doc.createTextNode(convertDateFormat(Sports[i][0])));
                item.appendChild(item_beginDateTime);

                Element item_location = doc.createElement("event:location");
                item_location.appendChild(doc.createTextNode(Sports[i][3]+""));
                item.appendChild(item_location);

                Element item_country = doc.createElement("event:countryCd");
                item_country.appendChild(doc.createTextNode("US"));
                item.appendChild(item_country);

                Element item_cancelStatus = doc.createElement("event:cancelStatus");
                item_cancelStatus.appendChild(doc.createTextNode("0"));
                item.appendChild(item_cancelStatus);

                Element item_statusName = doc.createElement("event:cancelStatusName");
                item_statusName.appendChild(doc.createTextNode("Scheduled"));
                item.appendChild(item_statusName);

                Element item_modDate = doc.createElement("event:statusModificationDate");
                item_modDate.appendChild(doc.createTextNode(convertDateFormat("null")));
                item.appendChild(item_modDate);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("/heroes/u1/rm934/Spring15Sports/xmlsports/"+filename+".xml"));
            System.out.println("Created "+filename+".xml");
            //For testing purposes: output xml to console
            //StreamResult result = new StreamResult(System.out);
            transformer.transform(source,result);

        }catch (ParserConfigurationException pce){
            pce.printStackTrace();
            return;
        }catch (TransformerException tfe){
            tfe.printStackTrace();
            return;
        }
    }

    public static String convertDateFormat(String date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd EEE");
        String newDate = "";
        if (date.equals("null")){
            newDate = format.format(new Date());
        }else{
            newDate = format.format(new Date(date));
        }
        return newDate.substring(0,newDate.indexOf(" "))+" "+"00:00:00"+" "+newDate.substring(newDate.indexOf(" ")+1);
    }

}
