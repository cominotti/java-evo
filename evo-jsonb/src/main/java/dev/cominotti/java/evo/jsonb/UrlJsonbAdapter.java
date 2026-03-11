// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jsonb;

import dev.cominotti.java.evo.url.Url;

public class UrlJsonbAdapter extends StringEvoJsonbAdapter<Url> {
    public UrlJsonbAdapter() { super(Url::value, Url.class); }
}
