# SPDX-License-Identifier: Apache-2.0

SHELL := /usr/bin/env bash
.SHELLFLAGS := -eu -o pipefail -c

.PHONY: help verify test license-check license-fix sonar-local

help:
	@echo "Targets:"
	@echo "  license-check  Maven validate guardrail for Apache-2.0 SPDX headers"
	@echo "  license-fix    Maven profile to auto-apply missing Apache-2.0 SPDX headers"
	@echo "  verify         Run Maven verify (compile + test + integration checks)"
	@echo "  test           Run Maven test (compile + unit tests only)"
	@echo "  sonar-local    Fetch SonarCloud quality gate and unresolved issues via REST API"

license-check:
	mvn -B -ntp validate

license-fix:
	mvn -B -ntp -Plicense-fix validate

verify:
	mvn -B -ntp verify

test:
	mvn -B -ntp test

sonar-local:
	@./scripts/sonar-local.sh
