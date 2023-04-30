/*
 * The Closed-Source License
 *
 * Copyright (c) 2023 Fair-Kom.
 *
 * All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * The file is proprietary and confidential.
 */
package com.github.piotrkot.hf.web;

import com.github.piotrkot.hf.web.forks.FkApi;
import com.github.piotrkot.takes.facets.fallback.TkFallback;
import com.github.piotrkot.takes.facets.fork.TkFork;
import com.github.piotrkot.takes.http.BkBasic;
import com.github.piotrkot.takes.http.BkParallel;
import com.github.piotrkot.takes.http.BkSkipEmpty;
import com.github.piotrkot.takes.http.Exit;
import com.github.piotrkot.takes.http.FtBasic;
import com.github.piotrkot.takes.tk.TkSlf4j;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;

/**
 * Application server.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (2 lines)
 */
@RequiredArgsConstructor
public final class Server {
    /**
     * Server port.
     */
    private final int port;

    /**
     * Number of threads.
     */
    private final int threads;

    /**
     * Starts the server.
     * @param source Data source.
     * @throws Exception When fails.
     */
    public void start(final DataSource source) throws Exception {
        new FtBasic(
            new BkParallel(
                new BkSkipEmpty(
                    new BkBasic(
                        new TkSlf4j(
                            new TkFallback(
                                new TkFork(
                                    new FkApi(source)
                                ),
                                new FbCustom()
                            )
                        )
                    )
                ),
                this.threads
            ),
            this.port
        ).start(Exit.NEVER);
    }
}
