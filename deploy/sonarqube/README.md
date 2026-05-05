# Standalone SonarQube

Use this compose file to bootstrap SonarQube on a server before the main project deployment is available.

## Linux host limits

SonarQube uses Elasticsearch, so set the required host limits before the first start:

```bash
sudo tee /etc/sysctl.d/99-sonarqube.conf > /dev/null <<EOF
vm.max_map_count=524288
fs.file-max=131072
EOF

sudo sysctl --system
```

## Start

Copy this directory to the server, then run:

```bash
cp .env.example .env
nano .env
docker compose up -d
docker compose logs -f sonarqube
```

Open:

```text
http://SERVER_IP:9000
```

Default first login:

```text
admin / admin
```

After login, create a token and add these GitHub Actions secrets:

```text
SONAR_HOST_URL=http://SERVER_IP:9000
SONAR_TOKEN=your-token
```

If the server is not public, install a GitHub self-hosted runner on the same server and use:

```text
SONAR_HOST_URL=http://localhost:9000
```
