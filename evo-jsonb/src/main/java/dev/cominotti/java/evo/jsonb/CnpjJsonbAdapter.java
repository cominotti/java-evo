// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jsonb;

import dev.cominotti.java.evo.taxid.Cnpj;

public class CnpjJsonbAdapter extends StringEvoJsonbAdapter<Cnpj> {
    public CnpjJsonbAdapter() { super(Cnpj::value, Cnpj.class); }
}
