package com.m3c.ttg.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FileMap {
    private HashMap<String,Integer> myHash;
    private UsageCount[] topThree;
    private String myFilePath;

    public FileMap(String filePath){
        myHash=new HashMap<>(2^17);
        UsageCount minCase = new UsageCount("",0);
        topThree = new UsageCount[]{minCase, minCase, minCase};
        myFilePath = filePath;
    }

    public void buildMap(){
        String line;
        try {
            FileReader file = new FileReader(myFilePath);
            BufferedReader reader = new BufferedReader(file);
            System.out.println("Iterating has started.");
            while ((line=reader.readLine())!=null) {
                if(line.length()!=0){
                    String[] words = line.split("[^A-z]");
                    for(String word:words) {
                        int newValue = 1;
                        String newWord = word.toLowerCase();
                        try{newValue = myHash.get(newWord)+1;}catch(NullPointerException e){}
                        myHash.put(newWord, newValue);
                    }
                }
            }
        }catch (IOException e){e.printStackTrace();}
    }

    private void checkTopThree(String theKey, int usage) {
        int difference = 0;
        int minIndex = -1;
        if((usage-topThree[0].myUsage)>difference){
            difference = usage - topThree[0].myUsage;
            minIndex = 0;
        }
        if(((usage-topThree[1].myUsage)>difference)){
            difference = usage - topThree[1].myUsage;
            minIndex=1;
        }
        if(((usage-topThree[2].myUsage)>difference)){
            minIndex=2;
        }
        if(minIndex!=-1){
            topThree[minIndex]=new UsageCount(theKey,usage);
        }

    }

    public String tellTopThree(){
        String output = "The top three words used (Word:Usage) are as follows: ";

        myHash.remove("");
        Iterator<Map.Entry<String,Integer>> it = myHash.entrySet().iterator();

        while(it.hasNext()){
            Map.Entry<String,Integer> ent = it.next();
            if(!ent.getKey().equals("")) {
                checkTopThree(ent.getKey(), ent.getValue());
            }
        }

        try{
            if(topThree.length==0) { throw new InvalidFileException();}
        }catch(InvalidFileException e){e.printStackTrace(); return "Error: no words in document!";}

        for(int x = 0; x < topThree.length; x++){
            output = output + topThree[x].myWord + ":" + topThree[x].myUsage + " ";
        }

        return output;
    }

    private class UsageCount {
        private String myWord;
        private int myUsage;
        private UsageCount(String myWord, int myUsage){this.myWord = myWord; this.myUsage = myUsage;}
    }
    private class InvalidFileException extends Exception{}
}
