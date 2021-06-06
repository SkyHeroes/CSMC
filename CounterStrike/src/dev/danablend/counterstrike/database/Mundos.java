package dev.danablend.counterstrike.database;

public class Mundos {
    public Integer id;
    public String nome;
    public boolean modoCs;

    public Mundos(Integer myid, String mynome, boolean mymodoCs) {
        id = myid;
        nome = mynome;
        modoCs = mymodoCs;
    }
}
