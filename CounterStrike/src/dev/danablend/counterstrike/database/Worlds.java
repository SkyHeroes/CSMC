package dev.danablend.counterstrike.database;

public class Worlds {
    public Integer id;
    public String nome;
    public boolean modoCs;

    public Worlds(Integer myid, String mynome, boolean mymodoCs) {
        id = myid;
        nome = mynome;
        modoCs = mymodoCs;
    }
}
