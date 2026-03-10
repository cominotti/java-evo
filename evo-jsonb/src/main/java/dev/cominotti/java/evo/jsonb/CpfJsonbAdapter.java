// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jsonb;

import dev.cominotti.java.evo.taxid.Cpf;

public class CpfJsonbAdapter extends StringEvoJsonbAdapter<Cpf> {
    public CpfJsonbAdapter() { super(Cpf::value, Cpf.class); }
}
