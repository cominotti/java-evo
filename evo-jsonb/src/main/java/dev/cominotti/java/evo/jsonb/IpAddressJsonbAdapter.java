// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.jsonb;

import dev.cominotti.java.evo.net.IpAddress;

public class IpAddressJsonbAdapter extends StringEvoJsonbAdapter<IpAddress> {
    public IpAddressJsonbAdapter() { super(IpAddress::value, IpAddress.class); }
}
