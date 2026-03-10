package dev.cominotti.java.evo.jsonb;

import dev.cominotti.java.evo.Cpf;

public class CpfJsonbAdapter extends StringEvoJsonbAdapter<Cpf> {
    public CpfJsonbAdapter() { super(Cpf::value, Cpf.class); }
}
