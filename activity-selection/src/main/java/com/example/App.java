package com.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.PatternSyntaxException;

public class App 
{
    public static void main( String[] args )
    {
        String filePath;
        if(args.length == 0)
        {
            System.out.println("Using default input file: test.in");
            filePath = "test.in";
        }
        else
        filePath = args[0];
        
        File file = new File(filePath);
        Activity[] activities = readInput(file);
        int sol = selectActivities(activities);
        //Write solution
        
        String inputName = filePath.split("[\\/]")[filePath.split("[\\/]").length-1];
        String outName = inputName.split("[.]")[0] + ".out";

        File outFile = new File(outName);
        try(Writer writer = new FileWriter(outFile))
        {
            writer.write(String.valueOf(sol));
        } catch (IOException e) {
            
            e.printStackTrace();
        }
    }


    static Activity[] readInput(File file)
    {
        Activity[] activities = null;
        try(Scanner scn = new Scanner(file))
        {
            int n = scn.nextInt();
            activities = new Activity[n];
            for(int i = 0; i < n; i++)
            {
                int s = scn.nextInt();
                int e = scn.nextInt();
                int w = scn.nextInt();
                activities[i] = new Activity(w, s, e);
            }
        }
        catch(FileNotFoundException e)
        {
            System.out.println("File not found");
        }
        return activities;
    }

    static int selectActivities(Activity[] activities)
    {
        Arrays.sort(activities);
        int[] memos = new int[activities.length];
        Arrays.fill(memos, -1);
        return selectActivitiesRecursive(activities, memos, 0);
    }

    static int selectActivitiesRecursive(Activity[] acts, int[] memos, int ptr)
    {
        if (memos[ptr] != -1) return memos[ptr];
        //Base case
        if (ptr == memos.length-1) return acts[ptr].w;
        
        int with = acts[ptr].w, without;
        for (int i=ptr+1; i<acts.length; i++)
        {
            if (acts[i].s >= acts[ptr].e)
            {
                with += selectActivitiesRecursive(acts, memos, i);
                break;
            }
        }
        without = selectActivitiesRecursive(acts, memos, ptr+1);
        memos[ptr] = Math.max(with, without);     
        return memos[ptr];   
    }

}


class Activity implements Comparable<Activity>
{
    public int w, s, e;
    public Activity(int w, int s, int e)
    {
        this.w = w;
        this.s = s;
        this.e = e;
    }

    public int compareTo(Activity other)
    {
        return this.s - other.s;
    }
}
