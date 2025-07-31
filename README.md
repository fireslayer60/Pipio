# Pipio

A new open source CI/CD Orchestrator coming soon :)

## Running Pipio with Docker

The entire Pipio application can be easily run using Docker and Docker Compose.

### Prerequisites

*   Docker
*   Docker Compose

### Instructions

1.  **Clone the repository:**
    ```bash
    git clone <your-repo-url>
    cd pipio
    ```

2.  **Build and run the application:**
    ```bash
    docker-compose up --build
    ```

3.  **Access the application:**
    Once the services are up and running, you can access the Pipio frontend at [http://localhost:18080](http://localhost:18080).

### GitHub Webhook URL

To integrate with GitHub, use the following webhook URL:

`http://<your-server-ip-or-domain>:18080/api/webhooks/github`

For local development, you can use a tool like [ngrok](https://ngrok.com/) to expose your local server to the internet.