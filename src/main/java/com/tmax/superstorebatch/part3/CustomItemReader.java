package com.tmax.superstorebatch.part3;

import org.springframework.batch.item.ItemReader;

import java.util.ArrayList;
import java.util.List;

public class CustomItemReader<T> implements ItemReader<T> {

    private final List<T> items;

    public CustomItemReader(List<T> items){
        this.items = new ArrayList<>(items);
    }
    @Override
    public T read() {
        if(!items.isEmpty()){
            return items.remove(0);
        }
        return null;
    }
}
