// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jsonb;

import dev.cominotti.java.evo.country.CountryCode;

public class CountryCodeJsonbAdapter extends StringEvoJsonbAdapter<CountryCode> {
    public CountryCodeJsonbAdapter() { super(CountryCode::value, CountryCode.class); }
}
