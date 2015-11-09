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
    private boolean fakeDemo = false;
    public ProcessedEntry(String rawXML, String entryBody, boolean fakeDemo){
        this.entryBody = entryBody;
        this.fakeDemo = fakeDemo;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder =factory.newDocumentBuilder();
            System.out.println(rawXML);
            doc = builder.parse(new InputSource(new StringReader(rawXML)));
        }catch (Exception e){
            System.out.println(e);
        }
        XPathFactory xPathfactory = XPathFactory.newInstance();
        xpath = xPathfactory.newXPath();
    }

    //returns a value between -1 and 1 for the overall entry sentiment
    public double getEntrySentiment(){
        double sum =0;
        List<Double> sents = getSentenceSentiments(fakeDemo);
        //todo: bound this
        for (Double i: sents){
            System.out.println("zzz i: " + i);
            sum += i ;
        }
        System.out.println("zzz sum: " + sum);

        return (double) sum/((double)sents.size());
    }
    //returns a list of integer sentiment values for each sentence
    public List<Double> getSentenceSentiments(boolean fakeForDemo){
     List sentenceSents = new ArrayList<Double>();

        NodeList sNodes = getSentenceNodes();
        List<Double> emojiSents = getSentenceEmojiSents(fakeForDemo);
        if(sNodes != null) {
            for (int i = 0; i < sNodes.getLength(); i++) {
                double val = sentenceSentiment(sNodes.item(i));

                sentenceSents.add(val);
            }
        }
        for(int i=0; i<emojiSents.size();i++){
            double sentVal = emojiSents.get(i);
            if(sNodes==null || sNodes.getLength()<= i){
                sentenceSents.add(sentVal);
            }else{
                if(sentVal != 0 || fakeForDemo) {
                    sentenceSents.set(i, sentVal);
                }
            }

        }
        return sentenceSents;
    }
    private List<String> getSentenceStrings(){

        String[] sents = entryBody.split("(?<!\\w\\.\\w.)(?<![A-Z][a-z]\\.)(?<=\\.|\\?|!)\\s");
        return Arrays.asList(sents);
    }
    public List<Double> getSentenceEmojiSents(Boolean useFakeSent){
        String positive = "\uD83D\uDE01";
        String negative = "\uD83D\uDE20";

        List<Double> sentiments = new ArrayList<Double>();
        for(String sentence: getSentenceStrings()){

            double sentSum=0;

            if(!useFakeSent) {
                sentSum = countOccurances(positive, sentence) - countOccurances(negative, sentence);

                if (sentSum > 2)
                    sentSum = 2;
                else if (sentSum < -2)
                    sentSum = -2;
            }else{
                Pattern p = Pattern.compile("\\[\\[(.+)\\]\\]");
                Matcher m = p.matcher(sentence);

                if (m.find()) {
                    sentSum = Double.parseDouble(m.group(1));
                }

            }
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
    public Map<String, Double> personSentiment(){
        return entitySentiment("PERSON");
    }
    public Map<String, Double> locationSentiment(){
        return entitySentiment("LOCATION");
    }
    private Map<String, Double> entitySentiment(String entityType){
        //todo: use coreference to attribute pronouns to a person and assign sentiment properly
        // i.e. John came over yesterday. he was mean to me.
        // currently john is not given a negative score
        DefaultHashMap<String, Double> psMap = new DefaultHashMap<String, Double>();

        NodeList sNodes = getSentenceNodes();
        List<Double> sentSents = getSentenceSentiments(fakeDemo);

        if(sNodes != null) {
            //for each sentence

            for (int i = 0; i < sNodes.getLength(); i++) {

                //find the sentiment

                double sentVal = sentSents.get(i);

                //for all people in each sentence
                for (String name : entityMentions(sNodes.item(i), entityType)) {

                    //add the sentiment of the 'primary' sentence

                    double primarySentVal = psMap.getOrDefault(name, 0.0) + sentVal;
                    psMap.put(name, primarySentVal);

                    // for all other places this person is referenced
                    for(int sentence:  corefSentenceApearances(name)) {

                        //add the sentiment of that sentence to their existing score
                        double corefSentVal = psMap.getOrDefault(name, 0.0) + sentSents.get(sentence-1);
                        psMap.put(name, corefSentVal);
                    }
                }
            }
        }
        for(Map.Entry<String, Double> mapEntry: psMap.entrySet()){
            if (mapEntry.getValue() > 2)
                psMap.put(mapEntry.getKey(), 2.0);
            else if (mapEntry.getValue() < -2)
                psMap.put(mapEntry.getKey(), -2.0);
        }
        //todo: consider this. maybe not necessary? maybe even wrong ?
        combineAliasValues(psMap);
        return psMap;
    }
    public Map<String, Double> combineAliasValues(Map<String, Double> psMap){
       Map<String, String> Aliases = new DefaultHashMap<String, String>();

        for(Map.Entry<String, Double> entry: psMap.entrySet()){
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
