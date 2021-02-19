package io.elves.core;

/**
 * @author edz
 */
public interface ElvesServer {
    void init(ElvesApplication application) throws Exception;
    void start(String[] args) throws Exception;
    void close();
}
