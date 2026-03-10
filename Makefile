SHELL := /usr/bin/env bash
.SHELLFLAGS := -eu -o pipefail -c

.PHONY: help verify test sonar-local

help:
	@echo "Targets:"
	@echo "  verify         Run Maven verify (compile + test + integration checks)"
	@echo "  test           Run Maven test (compile + unit tests only)"
	@echo "  sonar-local    Run local SonarCloud analysis and print unresolved issues (requires SONAR_TOKEN env var)"

verify:
	mvn -B -ntp verify

test:
	mvn -B -ntp test

sonar-local:
	@./scripts/sonar-local.sh
