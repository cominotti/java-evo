// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Marker annotation for Enterprise Value Objects (EVOs).
 */
@Documented
@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EvoType {
}
