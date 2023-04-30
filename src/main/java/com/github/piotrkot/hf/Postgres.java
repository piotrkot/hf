/*
 * The Closed-Source License
 *
 * Copyright (c) 2023 Fair-Kom.
 *
 * All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * The file is proprietary and confidential.
 */
package com.github.piotrkot.hf;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;

/**
 * PostgreSQL database.
 *
 * @since 1.0
 */
public final class Postgres {
    /**
     * Postgres URI pattern.
     */
    private static final Pattern PATT = Pattern.compile(
        "postgres://(?<user>.*):(?<pass>.*)@(?<host>.*):(?<port>\\d+)/(?<db>.*)"
    );

    /**
     * Hikari Data source.
     */
    private final HikariDataSource hikari;

    /**
     * Ctor.
     * @param uri URI as postgres://{user}:{pass}@{host}:{port}/{db}.
     */
    public Postgres(final String uri) {
        this.hikari = new HikariDataSource(Postgres.config(uri));
    }

    /**
     * Data source.
     * @return New data source.
     */
    public DataSource source() {
        return this.hikari;
    }

    /**
     * Close data source and its pool.
     */
    public void close() {
        this.hikari.close();
    }

    /**
     * Configuration.
     * @param uri URI as postgres://{user}:{pass}@{host}:{port}/{db}.
     * @return Hikari configuration.
     */
    private static HikariConfig config(final String uri) {
        final Matcher matcher = Postgres.PATT.matcher(uri);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                String.format("%s doesn't match %s", uri, Postgres.PATT)
            );
        }
        final String user = matcher.group("user");
        final String password = matcher.group("pass");
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
            String.format(
                "jdbc:postgresql://%s:%s/%s",
                matcher.group("host"),
                matcher.group("port"),
                matcher.group("db")
            )
        );
        config.setDriverClassName("org.postgresql.Driver");
        config.setUsername(user);
        config.setPassword(password);
        return config;
    }
}
