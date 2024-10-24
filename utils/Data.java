package org.example.utils;

import java.io.Serializable;

public class Data implements Serializable {
    public String train;
    public String test;

    public Data(String train,String test){
        this.train = train;
        this.test = test;
    }


}
