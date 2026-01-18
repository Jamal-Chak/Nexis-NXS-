package com.nexis.core;

public class SmartContract {
    public String address;
    public String owner;
    public String script;
    public int state;

    public SmartContract(String address, String owner, String script) {
        this.address = address;
        this.owner = owner;
        this.script = script;
        this.state = 0;
    }
}
