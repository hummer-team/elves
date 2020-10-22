package io.elves.core;

public interface ElvesServer {
    void init() throws Exception;
    void start(String[] args) throws Exception;
    void close();
}
