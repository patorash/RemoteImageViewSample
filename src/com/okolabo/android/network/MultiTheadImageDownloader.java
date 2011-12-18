package com.okolabo.android.network;

import java.util.ArrayList;

import android.content.Context;

public class MultiTheadImageDownloader {

    public static void execute(Context context, ArrayList<String> urls) {
        ArrayList<String> list1 = new ArrayList<String>();
        ArrayList<String> list2 = new ArrayList<String>();
        ArrayList<String> list3 = new ArrayList<String>();
        int i = 0;
        for (String url : urls) {
            switch (i % 3) {
                case 0: list1.add(url); break;
                case 1: list2.add(url); break;
                case 2: list3.add(url); break;
            }
            i++;
        }
        new ImageDownloadCacheTask(context).execute((String[])list1.toArray(new String[0]));
        new ImageDownloadCacheTask(context).execute((String[])list2.toArray(new String[0]));
        new ImageDownloadCacheTask(context).execute((String[])list3.toArray(new String[0]));
    }
}
