package com.example.Compressor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;


public class Huffman {
    
    HashMap<List<Byte>, Integer> getFrequencies(String filePath, int unitLen, List<List<Byte>> leftover) throws IOException
    {
        HashMap<List<Byte>, Integer> frequencies = new HashMap<List<Byte>, Integer>(Math.min(1000, (int)Math.pow(2, unitLen*8)));
        // FileInputStream reader = new FileInputStream(filePath);
        BufferedInputStream reader = new BufferedInputStream(new FileInputStream(filePath));
        int readRes;
        List<Byte> leftoverUnit = new ArrayList<Byte>();
        while(true)
        {
            byte[] bytes = new byte[unitLen];
            readRes = reader.read(bytes);
            if (readRes == -1) 
            break;
            List<Byte> unit = new ArrayList<Byte>(readRes);
            for (int i=0; i<readRes; i++)
                unit.add(bytes[i]);
            if (readRes != unitLen) leftoverUnit = unit;
            else if (frequencies.containsKey(unit))
                frequencies.replace(unit, frequencies.get(unit)+1);
            else frequencies.put(unit, 1);
        }
        leftover.add(leftoverUnit);
        reader.close();
        return frequencies;
    }

    public HashMap<List<Byte>, String> buildCodes(HashMap<List<Byte>, Integer> frequencies)
    {
        HashMap<List<Byte>, String> codes = new HashMap<List<Byte>, String>();
        PriorityQueue<Node> queue = new PriorityQueue<Node>();
        
        for (var entry : frequencies.entrySet())
            queue.add(new UnitFreqPair(entry.getKey(), entry.getValue()));
        
        while(queue.size() > 1)
        {
            Node left = queue.poll();
            Node right = queue.poll();
            Node parent = new Node(left, right, left.freq + right.freq);
            left.parent = parent;
            right.parent = parent;
            queue.add(parent);
        }
        int[] shortestLen = {-1};
        traverseTree(queue.poll(), new StringBuilder(), codes, shortestLen);
        return codes;
    }

    public void traverseTree(Node root, StringBuilder code, HashMap<List<Byte>, String> codes, int[] shortestCodeLen)
    {
        if (root.left == null && root.right == null)
        {
            codes.put(((UnitFreqPair)root).unit, code.toString());
            if(shortestCodeLen[0] == -1) 
                shortestCodeLen[0] = code.length();
            else 
                shortestCodeLen[0] = Math.min(shortestCodeLen[0], code.length());
            return;
        }
        code.append('1');
        traverseTree(root.right, code, codes, shortestCodeLen);
        code.deleteCharAt(code.length()-1);
        
        code.append('0');
        traverseTree(root.left, code, codes, shortestCodeLen);
        code.deleteCharAt(code.length()-1);
        
        return;
    }


    class UnitFreqPair extends Node
    {
        public List<Byte> unit;

        public UnitFreqPair(List<Byte> unit, int freq)
        {
            super(null, null, freq);
            this.unit = unit;
            this.freq = freq;
        }

        @Override
        public int compareTo(Node other)
        {
            return this.freq - other.freq;
        }
    }


    class Node implements Comparable<Node>
    {
        public int freq;
        public Node parent, left, right;

        public Node(Node left, Node right, int freq)
        {
            this.left = left;
            this.right = right;
            this.freq = freq;
        }
        
        @Override
        public int compareTo(Node other)
        {
            return this.freq - other.freq;
        }
    }

}
