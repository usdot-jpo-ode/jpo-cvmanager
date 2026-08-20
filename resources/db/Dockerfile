FROM flyway/flyway:10-alpine

# Create a non-root user to run Flyway migrations
RUN addgroup -S flyway && adduser -S -G flyway flyway \
    && mkdir -p /flyway/sql /flyway/conf \
    && chown -R flyway:flyway /flyway

# Copy only versioned migrations (V prefix).
# R__sample_data.sql is intentionally excluded — it is dev-only seed data.
COPY migration/V*.sql /flyway/sql/
COPY flyway.toml /flyway/conf/flyway.toml

USER flyway
