package io.itlit.ItLit;

public class Friend implements Comparable<Friend> {
    public String fname;
    public String name;
    public boolean lit;

    public Friend(String fname) {
        this.fname = fname;
        this.name = "";
        this.lit = false;
    }

    public Friend(String fname, String name) {
        this(fname);
        this.name = name;
    }

    public Friend(String fname, String name, boolean lit) {
        this.fname = fname;
        this.name = name;
        this.lit = lit;
    }

    public int compareTo(Friend f) {
        if (f == null)
            return Integer.MAX_VALUE;
        return this.name.toLowerCase().compareTo(f.name.toLowerCase());
    }

    public boolean equals(Friend f) {
        return this.fname.equals(f.fname);
    }
}
