package com.taylorandtucker.jot.NLP;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by tuckerkirven on 9/27/15.
 */
public class ProcessedEntry {
    private Document doc;
    private static XPath xpath;
    private String entryBody;
    public ProcessedEntry(String rawXML, String entryBody){
        this.entryBody = entryBody;

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
            sum += i ;
        }

        return sum/((float)sents.size());
    }
    //returns a list of integer sentiment values for each sentence
    public List<Integer> getSentenceSentiments(){
     List sentenceSents = new ArrayList<Integer>();

        NodeList sNodes = getSentenceNodes();
        List<Integer> emojiSents = getSentenceEmojiSents();
        if(sNodes != null) {
            for (int i = 0; i < sNodes.getLength(); i++) {
                int val = sentenceSentiment(sNodes.item(i));
                if(emojiSents.get(i) !=0)
                    val = emojiSents.get(i);
                sentenceSents.add(val);
            }
        }
        return sentenceSents;
    }
    private List<String> getSentenceStrings(){
        String sentenceRegex = "[^.!?]+[.?!]";


        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile(sentenceRegex)
                .matcher(entryBody);
        while (m.find()) {
            System.out.println(m.group());
            allMatches.add(m.group());
        }

        return allMatches;

    }
    public List<Integer> getSentenceEmojiSents(){
        String positive = "U+1F603";
        String negative = "U+1F621";

        List<Integer> sentiments = new ArrayList<Integer>();
        for(String sentence: getSentenceStrings()){
            int sentSum = countOccurances(positive, sentence) - countOccurances(negative,sentence);

            if (sentSum > 2)
                sentSum = 2;
            else if(sentSum < -2)
                sentSum = -2;

            sentiments.add(sentSum);
        }
        return sentiments;
    }
    private int countOccurances(String reg, String sentence){
        Pattern pattern = Pattern.compile(reg);
        Matcher  matcher = pattern.matcher(sentence);

        int count = 0;
        while (matcher.find())
            count++;
        return count;
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

        return entityMentions(sentence, "PERSON");
    }
    public List<String> locationMentions(Node sentence){

        return entityMentions(sentence, "LOCATION");
    }
    public List<String> entityMentions(Node sentence, String entityType){
        //todo: use dependency info to determine if a name is a compound name: john brown
        List names = new ArrayList<String>();
        try {
            int LastNameIndex = -2;
            NodeList nodes = (NodeList) xpath.evaluate(".//tokens/token", sentence, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node token = (Node) xpath.evaluate(".//NER", nodes.item(i), XPathConstants.NODE);


                if (token.getTextContent().toString().contains(entityType)){
                    String name = xpath.evaluate(".//word", nodes.item(i), XPathConstants.STRING).toString();
                    if(LastNameIndex == i-1){
                        names.set(names.size()-1, names.get(names.size()-1).toString() +" "+name.trim());
                    }else {
                        names.add(name.trim());
                    }
                    LastNameIndex = i;
                }
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return names;
    }
    public Map<String, Integer> personSentiment(){
        return entitySentiment("PERSON");
    }
    public Map<String, Integer> locationSentiment(){
        return entitySentiment("LOCATION");
    }
    private Map<String, Integer> entitySentiment(String entityType){
        //todo: use coreference to attribute pronouns to a person and assign sentiment properly
        // i.e. John came over yesterday. he was mean to me.
        // currently john is not given a negative score
        DefaultHashMap<String, Integer> psMap = new DefaultHashMap<String, Integer>();

        NodeList sNodes = getSentenceNodes();
        List<Integer> sentSents = getSentenceSentiments();

        if(sNodes != null) {
            //for each sentence
            for (int i = 0; i < sNodes.getLength(); i++) {

                //find the sentiment
                int sentVal = sentenceSentiment(sNodes.item(i));

                //for all people in each sentence
                for (String name : entityMentions(sNodes.item(i), entityType)) {

                    //add the sentiment of the 'primary' sentence

                    int primarySentVal = psMap.getOrDefault(name, 0) + sentVal;
                    psMap.put(name, primarySentVal);

                    // for all other places this person is referenced
                    for(int sentence:  corefSentenceApearances(name)) {

                        //add the sentiment of that sentence to their existing score
                        int corefSentVal = psMap.getOrDefault(name, 0) + sentSents.get(sentence-1);
                        psMap.put(name, corefSentVal);
                    }
                }
            }
        }
        //todo: consider this. maybe not necessary? maybe even wrong ?
        combineAliasValues(psMap);
        return psMap;
    }
    public Map<String, Integer> combineAliasValues(Map<String, Integer> psMap){
       Map<String, String> Aliases = new DefaultHashMap<String, String>();

        for(Map.Entry<String, Integer> entry: psMap.entrySet()){
            for(String name: psMap.keySet()){

                String after = entry.getKey() + " ";
                if(fullNameContainsSingle(name, entry.getKey())){

                    psMap.put(name, psMap.get(name)+ entry.getValue());

                    Aliases.put(entry.getKey(), name);
                }
            }
        }
        for(String name: Aliases.keySet()){

            psMap.remove(name);
        }
        return psMap;
    }
    public Boolean fullNameContainsSingle(String full, String single){
        return (full!= single && Arrays.asList(full.split(" ")).contains(single));
    }
    private List<Integer> corefSentenceApearances(String entity){
        List<Integer> appearances = new ArrayList<Integer>();
        try {
            NodeList list = (NodeList) xpath.evaluate("//coreference/coreference[contains(., '" +entity+"')]/*[not(contains(@representative, 'true'))]/sentence/text()", doc, XPathConstants.NODESET);
            for(int i =0; i<list.getLength(); i++){
                appearances.add(Integer.parseInt(list.item(i).getTextContent()));
            }
        }catch (XPathExpressionException e){

        }
        return appearances;
    }
    public void print(String a){
        System.out.println(a);
    }

    public class DefaultHashMap<K,V> extends HashMap<K,V> {
        public V getOrDefault(K k, V d){
            return containsKey(k) ? super.get(k) : d;
        }
    }
}
