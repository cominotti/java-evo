// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.net;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IpAddressTest {

    // --- IPv4 ---

    @Test
    void validIpv4Loopback() {
        var ip = new IpAddress("127.0.0.1");
        assertThat(ip.value()).isEqualTo("127.0.0.1");
    }

    @Test
    void validIpv4PrivateAddress() {
        var ip = new IpAddress("192.168.1.1");
        assertThat(ip.value()).isEqualTo("192.168.1.1");
    }

    @Test
    void validIpv4AllZeros() {
        var ip = new IpAddress("0.0.0.0");
        assertThat(ip.value()).isEqualTo("0.0.0.0");
    }

    @Test
    void validIpv4Broadcast() {
        var ip = new IpAddress("255.255.255.255");
        assertThat(ip.value()).isEqualTo("255.255.255.255");
    }

    // --- IPv6 ---

    @Test
    void validIpv6Loopback() {
        var ip = new IpAddress("::1");
        assertThat(ip.value()).isEqualTo("::1");
    }

    @Test
    void validIpv6FullForm() {
        var ip = new IpAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
        assertThat(ip.value()).isEqualTo("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
    }

    @Test
    void validIpv6Compressed() {
        var ip = new IpAddress("2001:db8::1");
        assertThat(ip.value()).isEqualTo("2001:db8::1");
    }

    @Test
    void validIpv6AllZeros() {
        var ip = new IpAddress("::");
        assertThat(ip.value()).isEqualTo("::");
    }

    @Test
    void validIpv4MappedIpv6() {
        var ip = new IpAddress("::ffff:192.168.1.1");
        assertThat(ip.value()).isEqualTo("::ffff:192.168.1.1");
    }

    // --- Invalid ---

    @Test
    void nullThrowsIllegalArgument() {
        assertThatThrownBy(() -> new IpAddress(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void blankThrowsIllegalArgument() {
        assertThatThrownBy(() -> new IpAddress(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void whitespaceOnlyThrowsIllegalArgument() {
        assertThatThrownBy(() -> new IpAddress("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void hostnameRejected() {
        assertThatThrownBy(() -> new IpAddress("example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid IPv4 or IPv6");
    }

    @Test
    void ipv4WithExtraOctetRejected() {
        assertThatThrownBy(() -> new IpAddress("192.168.1.1.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid IPv4 or IPv6");
    }

    @Test
    void ipv4WithValueOver255Rejected() {
        assertThatThrownBy(() -> new IpAddress("256.1.1.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid IPv4 or IPv6");
    }

    @Test
    void invalidStringRejected() {
        assertThatThrownBy(() -> new IpAddress("not-an-ip"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid IPv4 or IPv6");
    }

    // --- Equality ---

    @Test
    void equalityBasedOnValue() {
        var a = new IpAddress("192.168.1.1");
        var b = new IpAddress("192.168.1.1");
        assertThat(a).isEqualTo(b)
                .hasSameHashCodeAs(b);
    }

    @Test
    void inequalityForDifferentValues() {
        var a = new IpAddress("192.168.1.1");
        var b = new IpAddress("10.0.0.1");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void toStringReturnsRawValue() {
        var ip = new IpAddress("192.168.1.1");
        assertThat(ip).hasToString("192.168.1.1");
    }
}
