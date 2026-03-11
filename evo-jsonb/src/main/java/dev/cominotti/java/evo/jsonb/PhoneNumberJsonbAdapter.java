// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jsonb;

import dev.cominotti.java.evo.phone.PhoneNumber;

public class PhoneNumberJsonbAdapter extends StringEvoJsonbAdapter<PhoneNumber> {
    public PhoneNumberJsonbAdapter() { super(PhoneNumber::value, PhoneNumber.class); }
}
