package com.app.trackmymeds;

import java.util.ArrayList;

/**
 * Created by Declan on 23/06/2017.
 */

public class ExpandListGroup {

    public String getName() {
        return m_name;
    }

    public void setName (String _name) {
        m_name = _name;
    }

    public ArrayList<ExpandListChild> getItems() {
        return m_items;
    }

    public void setItems(ArrayList<ExpandListChild> _items) {
        m_items = _items;
    }

    //Properties.
    private String m_name;
    private ArrayList<ExpandListChild> m_items;
}
