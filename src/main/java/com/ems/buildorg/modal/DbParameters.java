package com.ems.buildorg.modal;

public class DbParameters {
    public String parameter;
    public Object value;
    public int type;

    public DbParameters(String parameter, Object value, int type) {
        this.parameter = parameter;
        this.value = value;
        this.type = type;
    }
}
