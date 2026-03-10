// SPDX-License-Identifier: Apache-2.0

package dev.cominotti.java.evo.greeting;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GreetingRepository extends JpaRepository<Greeting, Long> {
}
