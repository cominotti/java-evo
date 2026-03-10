package dev.cominotti.java.evo.jsonb;

import dev.cominotti.java.evo.Cnpj;

public class CnpjJsonbAdapter extends StringEvoJsonbAdapter<Cnpj> {
    public CnpjJsonbAdapter() { super(Cnpj::value, Cnpj.class); }
}
