.PHONY: help start assembly test clean start-docker test-integration-docker clean-docker version env
SHELL := /bin/bash

VERSION=2.0.0

export ROOT_LOG_LEVEL ?= ERROR
export LOG_LEVEL ?= DEBUG
export PORT ?= 8089

help: ## Print help.
	@IFS=$$'\n' ; \
	help_lines=(`fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/##/:/'`); \
	printf "%-30s %s\n" "target" "help" ; \
	printf "%-30s %s\n" "------" "----" ; \
	for help_line in $${help_lines[@]}; do \
		IFS=$$':' ; \
		help_split=($$help_line) ; \
		help_command=`echo $${help_split[0]} | sed -e 's/^ *//' -e 's/ *$$//'` ; \
		help_info=`echo $${help_split[2]} | sed -e 's/^ *//' -e 's/ *$$//'` ; \
		printf '\033[36m'; \
		printf "%-30s %s" $$help_command ; \
		printf '\033[0m'; \
		printf "%s\n" $$help_info; \
	done

start: assembly ## Run the application locally.
	./start.sh xatu.jar

assembly: clean ## Generate assembly xatu.jar in the root folder.
	sbt assembly
	cp target/scala-2.13/xatu-observer-assembly-$(VERSION).jar xatu.jar

test: env clean ## Run all unit tests.
	sbt test

clean: ## Run sbt clean and delete generated files.
	rm output.json || true
	rm xatu.jar || true
	sbt clean

start-docker: env clean-docker assembly ## Start docker compose with all required services.
	docker compose down || true
	docker compose up --build

test-integration-docker: env clean-docker assembly ## Start docker compose and check all integrations.
	@echo "Starting services..."
	docker compose up --build -d --wait
	@echo "Testing healthcheck..."
	@curl -o output.json -w 'Status Code: %{http_code}\n' --connect-timeout 20 --max-time 20 --fail-with-body http://localhost:8089/healthcheck || (cat output.json & $(MAKE) clean-docker & exit 1)
	$(MAKE) clean-docker

clean-docker: ## Stop and remove containers.
	@echo "Stopping and removing containers..."
	docker compose down --remove-orphans || true
	yes | docker compose rm

version: ## Print current version.
	@echo $(VERSION)

env: ## Print information about environment variables.
	@echo "You can change any environment variable prepending ENV=VALUE in front of the make command or exporting in your environment."
	@echo "PORT is set to $(PORT)."
	@echo "ROOT_LOG_LEVEL is set to $(ROOT_LOG_LEVEL)."
	@echo "LOG_LEVEL is set to $(LOG_LEVEL)."
