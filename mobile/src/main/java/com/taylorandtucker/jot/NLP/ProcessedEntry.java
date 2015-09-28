package com.taylorandtucker.jot.NLP;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    //returns a value between -2 and 2 for the overall entry sentiment
    public double getEntrySentiment(){
        int sum =0;
        List<Integer> sents = getSentenceSentiments();
        //todo: bound this
        for (Integer i: sents){
            sum += (i -2);
        }
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(sum/((double)sents.size())));
    }
    //returns a list of integer sentiment values for each sentence
    private List<Integer> getSentenceSentiments(){
     List sentenceSents = new ArrayList<Integer>();
        try {
            XPathExpression expr = xpath.compile("/root/document/sentences/sentence[@sentimentValue]");
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                int val = Integer.parseInt(nodes.item(i).getAttributes().getNamedItem("sentimentValue").getNodeValue().toString());
                sentenceSents.add(val);
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return sentenceSents;
    }

    public List<String> personMentions(){
        List names = new ArrayList<Integer>();
        try {
            XPathExpression expr = xpath.compile("//sentence/tokens/token");
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {


                Node token = (Node) xpath.evaluate(".//NER", nodes.item(i), XPathConstants.NODE);

                print(token.getTextContent().toString());
                if (token.getTextContent().toString().contains("PERSON")){
                    String name = xpath.evaluate(".//word", nodes.item(i), XPathConstants.STRING).toString();
                    print("HERE");
                    names.add(name);
                }

            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return names;
    }
    public void print(String a){
        System.out.println(a);
    }
}
