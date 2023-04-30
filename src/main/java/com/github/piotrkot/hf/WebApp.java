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

import com.github.piotrkot.hf.web.Server;
import lombok.extern.slf4j.Slf4j;

/**
 * Web application.
 *
 * @since 1.0
 */
@Slf4j
public final class WebApp {
    /**
     * Private constructor.
     */
    private WebApp() {
    }

    /**
     * Main runnable method.
     * @param args Arguments.
     * @throws Exception When fails
     */
    public static void main(final String... args) throws Exception {
        final Env env = new Env.Runtime();
        final int port = Integer.parseInt(env.variable("PORT"));
        final int threads = Integer.parseInt(env.variable("THREADS"));
        log.info("Server starting on port {} with {} threads", port, threads);
        new Server(port, threads).start(
            new Postgres(env.variable("DATABASE_URL")).source()
        );
    }
}
