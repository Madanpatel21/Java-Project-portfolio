#!/usr/bin/env python3
"""Generates a production project skeleton from the shared kit.
Usage: python3 scripts/new_project.py <id> <pkg> <artifact> <MainClass> "<App Name>" "<Description>"
"""
import os, shutil, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
KIT = os.path.join(ROOT, "scripts", "kit")

def main():
    pid, pkg, artifact, main_cls, app_name, desc = sys.argv[1:7]
    project_dir = f"java-{int(pid):03d}"
    pkg_name = f"com.java700.{pkg}"
    pkg_path = pkg_name.replace(".", "/")
    tokens = {
        "%%PKG%%": pkg_name,
        "%%PKG_ESCAPED%%": pkg_name.replace(".", "\\."),
        "%%APP_CLASS%%": main_cls,
        "%%ARTIFACT%%": artifact,
        "%%APP_NAME%%": app_name.replace("&", "&amp;"),
        "%%DESC%%": desc,
        "%%MAIN_FQCN%%": f"{pkg_name}.{main_cls}",
        "%%PROJECT_DIR%%": project_dir,
        "%%JAR%%": f"{artifact}-1.0.0.jar",
        "%%DB%%": pkg,
        "%%RABBIT_USER%%": pkg,
        "%%DASH_TITLE%%": app_name,
        "%%DASH_UID%%": f"java{int(pid):03d}",
        "%%APP_ID%%": f"JAVA-{int(pid):03d}",
        "%%COMPOSE_NAME%%": f"java{int(pid):03d}-{pkg}",
    }
    out = os.path.join(ROOT, "projects", project_dir)
    shutil.rmtree(out, ignore_errors=True)
    os.makedirs(os.path.join(out, "src", "main", "java", pkg_path))
    os.makedirs(os.path.join(out, "src", "main", "resources", "db", "migration"))
    os.makedirs(os.path.join(out, "src", "test", "java", pkg_path))
    os.makedirs(os.path.join(out, "src", "test", "resources"))
    os.makedirs(os.path.join(out, "docs", "adr"))

    def copy(src, dst):
        shutil.copy(src, dst)

    # ---- java kit (common/security/messaging/observability) ----
    for sub in ["common", "security", "messaging", "observability"]:
        shutil.copytree(os.path.join(KIT, sub), os.path.join(out, "src", "main", "java", pkg_path, sub))
    os.makedirs(os.path.join(out, "src", "main", "java", pkg_path, "bootstrap"))
    shutil.copy(os.path.join(KIT, "OpenApiConfig.java"),
                os.path.join(out, "src", "main", "java", pkg_path, "bootstrap", "OpenApiConfig.java"))

    # ---- app class ----
    with open(os.path.join(out, "src", "main", "java", pkg_path, f"{main_cls}.java"), "w") as f:
        f.write(f"""package {pkg_name};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * {app_name}.
 *
 * <p>{desc}</p>
 */
@SpringBootApplication
@EnableScheduling
public class {main_cls} {{

    public static void main(String[] args) {{
        SpringApplication.run({main_cls}.class, args);
    }}
}}
""")

    # ---- stubs (projects overwrite) ----
    with open(os.path.join(out, "src", "main", "java", pkg_path, "security", "Roles.java"), "w") as f:
        f.write(f"package {pkg_name}.security;\n\n/** RBAC roles — override per project. */\npublic final class Roles {{\n\n    private Roles() {{\n    }}\n}}\n")
    with open(os.path.join(out, "src", "main", "java", pkg_path, "observability", "Metrics.java"), "w") as f:
        f.write(f"package {pkg_name}.observability;\n\nimport io.micrometer.core.instrument.MeterRegistry;\nimport org.springframework.stereotype.Component;\n\n/** Typed business metrics — override per project. */\n@Component\npublic class Metrics {{\n\n    public Metrics(MeterRegistry registry) {{\n    }}\n}}\n")
    with open(os.path.join(out, "src", "main", "java", pkg_path, "bootstrap", "SeedData.java"), "w") as f:
        f.write(f"""package {pkg_name}.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Dev-profile demo dataset — override per project. */
@Configuration
@Profile("dev")
public class SeedData {{

    private static final Logger log = LoggerFactory.getLogger(SeedData.class);

    @Bean
    CommandLineRunner seed() {{
        return args -> log.info("Dev seed: no demo data configured for {app_name}");
    }}
}}
""")

    # ---- resources ----
    for res in ["application.yml", "application-dev.yml", "application-local.yml", "application-test.yml", "logback-spring.xml"]:
        copy(os.path.join(KIT, "resources", res), os.path.join(out, "src", "main", "resources", res))
    copy(os.path.join(KIT, "resources", "application-test.yml"), os.path.join(out, "src", "test", "resources", "application-test.yml"))
    copy(os.path.join(KIT, "migrations", "V1__common_schema.sql"),
         os.path.join(out, "src", "main", "resources", "db", "migration", "V1__common_schema.sql"))

    # ---- test kit ----
    os.makedirs(os.path.join(out, "src", "test", "java", pkg_path, "common"), exist_ok=True)
    os.makedirs(os.path.join(out, "src", "test", "java", pkg_path, "audit"), exist_ok=True)
    shutil.copy(os.path.join(KIT, "testkit", "TestDb.java"), os.path.join(out, "src", "test", "java", pkg_path, "common", "TestDb.java"))
    shutil.copy(os.path.join(KIT, "testkit", "TestFixtures.java"), os.path.join(out, "src", "test", "java", pkg_path, "common", "TestFixtures.java"))
    shutil.copy(os.path.join(KIT, "testkit", "PostgreSQLMigrationIT.java"), os.path.join(out, "src", "test", "java", pkg_path, "audit", "PostgreSQLMigrationIT.java"))

    # ---- root infra ----
    for d in ["jmeter", ".github/workflows", "checkstyle"]:
        os.makedirs(os.path.join(out, d), exist_ok=True)
    copy(os.path.join(KIT, "pom-template.xml"), os.path.join(out, "pom.xml"))
    copy(os.path.join(KIT, "Dockerfile"), os.path.join(out, "Dockerfile"))
    copy(os.path.join(KIT, ".env.example"), os.path.join(out, ".env.example"))
    copy(os.path.join(KIT, "jmeter.jmx"), os.path.join(out, "jmeter", "plan.jmx"))
    for c in ["checkstyle.xml", "spotbugs-exclude.xml"]:
        copy(os.path.join(KIT, c), os.path.join(out, "checkstyle", c))
    shutil.copytree(os.path.join(KIT, "docker"), os.path.join(out, "docker"))

    # ---- docker-compose ----
    with open(os.path.join(out, "docker-compose.yml"), "w") as f:
        f.write(COMPOSE)

    # ---- grafana generic dashboard ----
    with open(os.path.join(out, "docker", "grafana", "dashboards", "dashboard.json"), "w") as f:
        f.write(DASHBOARD)

    # ---- CI ----
    with open(os.path.join(out, ".github", "workflows", "ci.yml"), "w") as f:
        f.write(CI)

    # ---- README placeholder (projects replace with the full one) ----
    with open(os.path.join(out, "README.md"), "w") as f:
        f.write(f"# {app_name}\n\n{desc}\n\nFull documentation is written during project implementation.\n")

    # ---- token replacement everywhere ----
    for dirpath, _, files in os.walk(out):
        for fn in files:
            p = os.path.join(dirpath, fn)
            try:
                s = open(p).read()
            except UnicodeDecodeError:
                continue
            orig = s
            for k, v in tokens.items():
                s = s.replace(k, v)
            s = s.replace("com.java700.kit", pkg_name)
            s = s.replace("kit-service", artifact)
            s = s.replace("kitdb", pkg)
            if s != orig:
                open(p, "w").write(s)
    print(f"generated projects/{project_dir} ({pkg_name})")


COMPOSE = """name: %%COMPOSE_NAME%%

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-%%DB%%}
      POSTGRES_USER: ${POSTGRES_USER:-%%DB%%}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-%%DB%%}
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-%%DB%%}"]
      interval: 10s
      timeout: 5s
      retries: 10

  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    environment:
      RABBITMQ_DEFAULT_USER: ${RABBIT_USER:-%%RABBIT_USER%%}
      RABBITMQ_DEFAULT_PASS: ${RABBIT_PASSWORD:-%%RABBIT_USER%%}
    ports:
      - "${RABBIT_PORT:-5672}:5672"
      - "${RABBIT_MGMT_PORT:-15672}:15672"
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 15s
      timeout: 10s
      retries: 10

  app:
    build: .
    environment:
      SPRING_PROFILES_ACTIVE: local
      POSTGRES_HOST: postgres
      POSTGRES_PORT: 5432
      POSTGRES_DB: ${POSTGRES_DB:-%%DB%%}
      POSTGRES_USER: ${POSTGRES_USER:-%%DB%%}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-%%DB%%}
      RABBIT_HOST: rabbitmq
      RABBIT_USER: ${RABBIT_USER:-%%RABBIT_USER%%}
      RABBIT_PASSWORD: ${RABBIT_PASSWORD:-%%RABBIT_USER%%}
      JWT_SECRET: ${JWT_SECRET:-dev-only-please-rotate-this-secret-0123456789abcdef}
    ports:
      - "${APP_PORT:-8080}:8080"
    depends_on:
      postgres:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy

  prometheus:
    image: prom/prometheus:v2.54.1
    ports:
      - "${PROMETHEUS_PORT:-9090}:9090"
    volumes:
      - ./docker/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
    depends_on:
      - app

  grafana:
    image: grafana/grafana:11.2.0
    environment:
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_ADMIN_PASSWORD:-grafana}
      GF_AUTH_ANONYMOUS_ENABLED: "true"
      GF_AUTH_ANONYMOUS_ORG_ROLE: Viewer
    ports:
      - "${GRAFANA_PORT:-3000}:3000"
    volumes:
      - ./docker/grafana/provisioning:/etc/grafana/provisioning:ro
      - ./docker/grafana/dashboards:/var/lib/grafana/dashboards:ro
    depends_on:
      - prometheus

volumes:
  pgdata:
"""

DASHBOARD = """{
  "title": "%%DASH_TITLE%%",
  "uid": "%%DASH_UID%%",
  "panels": [
    {"title": "HTTP request rate", "type": "timeseries",
     "targets": [{"expr": "rate(http_server_requests_seconds_count[1m])", "legendFormat": "{{method}} {{uri}}"}],
     "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0}},
    {"title": "JVM heap used", "type": "timeseries",
     "targets": [{"expr": "jvm_memory_used_bytes{area=\\"heap\\"}", "legendFormat": "{{id}}"}],
     "gridPos": {"h": 8, "w": 12, "x": 12, "y": 0}},
    {"title": "HTTP 5xx rate", "type": "timeseries",
     "targets": [{"expr": "rate(http_server_requests_seconds_count{status=~\\"5..\\"}[1m])", "legendFormat": "5xx"}],
     "gridPos": {"h": 8, "w": 12, "x": 0, "y": 8}},
    {"title": "Process uptime", "type": "stat",
     "targets": [{"expr": "process_uptime_seconds"}],
     "gridPos": {"h": 8, "w": 12, "x": 12, "y": 8}}
  ],
  "schemaVersion": 39,
  "version": 1
}
"""

CI = """name: ci

on:
  push:
    paths:
      - "projects/%%PROJECT_DIR%%/**"
  pull_request:
    paths:
      - "projects/%%PROJECT_DIR%%/**"

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: projects/%%PROJECT_DIR%%
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "21"
          cache: maven
      - name: Verify (unit + integration + security tests, Testcontainers PostgreSQL)
        run: mvn -B verify
      - name: Static analysis (Checkstyle + SpotBugs)
        run: mvn -B verify -Pstatic-analysis -DskipTests
      - name: Package
        run: mvn -B -DskipTests package
      - name: Upload artifact
        uses: actions/upload-artifact@v4
        with:
          name: %%ARTIFACT%%-jar
          path: projects/%%PROJECT_DIR%%/target/%%JAR%%
"""

if __name__ == "__main__":
    main()
