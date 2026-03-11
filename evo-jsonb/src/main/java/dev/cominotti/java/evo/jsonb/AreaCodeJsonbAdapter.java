// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jsonb;

import dev.cominotti.java.evo.phone.AreaCode;

public class AreaCodeJsonbAdapter extends StringEvoJsonbAdapter<AreaCode> {
    public AreaCodeJsonbAdapter() { super(AreaCode::value, AreaCode.class); }
}
