package org.pranavt84.model;

import java.util.ArrayList;
import java.util.List;

public class DataInput {

    private List<Object> elements = new ArrayList<>();

    public DataInput() {}

    public void add(Object obj) {
        elements.add(obj);
    }

    public List<Object> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        return "DataInput{" +
                "elements=" + elements +
                '}';
    }
}
