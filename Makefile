DOCKER=docker
NETWORK_NAME=bb_network

# Remove network if it exists
.PHONY: clean-network
clean-network:
	@if $(DOCKER) network inspect $(NETWORK_NAME) >/dev/null 2>&1; then \
		echo "Network $(NETWORK_NAME) exists, removing..."; \
		$(DOCKER) network rm $(NETWORK_NAME); \
	fi

# Check if .env file exists, if not, clone .env.sample
.PHONY: check-env
check-env:
	@if [ ! -f .env.sample ]; then \
		echo ".env.sample not found"; \
		exit 1; \
	fi
	@if [ ! -f .env ]; then \
		cp .env.sample .env; \
		echo ".env created from .env.sample"; \
	fi

# Run base infrastructure for backend application
.PHONY: up-infra
up-infra: clean-network check-env
	$(DOCKER) network create $(NETWORK_NAME)
	$(DOCKER) compose --env-file .env -f ./docker/docker-compose.yml up -d

# Shut down base infrastructure, docker volumes are preserved
.PHONY: down
down:
	$(DOCKER) compose --env-file .env -f ./docker/docker-compose.yml down
	$(DOCKER) network remove $(NETWORK_NAME)