package com.example.peach;

import java.util.Comparator;

public class UnreadComparator implements Comparator<Unread> {

    @Override
    public int compare(Unread o1, Unread o2) {
        return Long.compare(o1.getDateTime(), o2.getDateTime());
    }
}
