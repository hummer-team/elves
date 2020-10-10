package io.elves.core;

public interface ElvesServer {
    void init();
    void start(String[] args) throws Exception;
    void close();
}
