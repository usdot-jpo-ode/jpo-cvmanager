FROM flyway/flyway:10-alpine

# Copy only versioned migrations (V prefix).
# R__sample_data.sql is intentionally excluded — it is dev-only seed data.
COPY migration/V*.sql /flyway/sql/
COPY flyway.toml /flyway/conf/flyway.toml
