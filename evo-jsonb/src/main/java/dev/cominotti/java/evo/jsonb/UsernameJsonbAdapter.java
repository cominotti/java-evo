// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jsonb;

import dev.cominotti.java.evo.username.Username;

public class UsernameJsonbAdapter extends StringEvoJsonbAdapter<Username> {
    public UsernameJsonbAdapter() { super(Username::value, Username.class); }
}
