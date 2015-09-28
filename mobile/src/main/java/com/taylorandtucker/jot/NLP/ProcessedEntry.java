import com.sun.org.apache.xpath.internal.NodeSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by tuckerkirven on 9/27/15.
 */
public class ProcessedEntry {
    private Document doc;
    private static XPath xpath;
    public ProcessedEntry(String rawXML){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder =factory.newDocumentBuilder();
            doc = builder.parse(new InputSource(new StringReader(rawXML)));
        }catch (Exception e){
            System.out.println(e);
        }
        XPathFactory xPathfactory = XPathFactory.newInstance();
        xpath = xPathfactory.newXPath();
    }

    //returns a value between -1 and 1 for the overall entry sentiment
    public float getEntrySentiment(){
        int sum =0;
        List<Integer> sents = getSentenceSentiments();
        //todo: bound this
        for (Integer i: sents){
            sum += (i -2);
        }

        return sum/((float)sents.size());
    }
    //returns a list of integer sentiment values for each sentence
    private List<Integer> getSentenceSentiments(){
     List sentenceSents = new ArrayList<Integer>();

        NodeList sNodes = getSentenceNodes();
        if(sNodes != null) {
            for (int i = 0; i < sNodes.getLength(); i++) {
                int val = sentenceSentiment(sNodes.item(i));
                sentenceSents.add(val);
            }
        }
        return sentenceSents;
    }

    private int sentenceSentiment(Node sentence){
        return Integer.parseInt(sentence.getAttributes().getNamedItem("sentimentValue").getNodeValue())-2;
    }
    private NodeList getSentenceNodes(){
        try {
            return (NodeList) xpath.evaluate("/root/document/sentences/sentence", doc, XPathConstants.NODESET);
        }catch(Exception e){
            return null;
        }
    }

    public List<String> personMentions(Node sentence){
        //todo: use dependency info to determine if a name is a compound name: john brown
        List names = new ArrayList<Integer>();
        try {

            NodeList nodes = (NodeList) xpath.evaluate(".//tokens/token", sentence, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node token = (Node) xpath.evaluate(".//NER", nodes.item(i), XPathConstants.NODE);

                if (token.getTextContent().toString().contains("PERSON")){
                    String name = xpath.evaluate(".//word", nodes.item(i), XPathConstants.STRING).toString();

                    names.add(name);
                }

            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return names;
    }

    public Map<String, Integer> personSentiment(){
        //todo: use coreference to attribute pronouns to a person and assign sentiment properly
        // i.e. John came over yesterday. he was mean to me.
        // currently john is not given a negative score
        Map<String, Integer> psMap = new HashMap<>();

        NodeList sNodes = getSentenceNodes();
        if(sNodes != null) {
            for (int i = 0; i < sNodes.getLength(); i++) {
                int val = sentenceSentiment(sNodes.item(i));

                for (String name : personMentions(sNodes.item(i))) {
                    print(name);
                    int newVal = psMap.getOrDefault(name, 0) + val;
                    psMap.put(name, newVal);


                }
            }
        }
        return psMap;
    }
    public void print(String a){
        System.out.println(a);
    }
}
