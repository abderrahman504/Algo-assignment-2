package com.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class App 
{
    public static void main( String[] args )
    {
        File file;
        if(args.length == 0)
        {
            System.out.println("Using default input file: test.in");
            file = new File("test.in");
        }
        else
            file = new File(args[0]);

        Activity[] activities = readInput(file);
        int sol = selectActivities(activities);
        System.out.println(sol);
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
