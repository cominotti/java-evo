// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jsonb;

import dev.cominotti.java.evo.slug.Slug;

public class SlugJsonbAdapter extends StringEvoJsonbAdapter<Slug> {
    public SlugJsonbAdapter() { super(Slug::value, Slug.class); }
}
