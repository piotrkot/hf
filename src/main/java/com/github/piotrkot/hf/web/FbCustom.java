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

import com.github.piotrkot.takes.facets.fallback.FbChain;
import com.github.piotrkot.takes.facets.fallback.FbWrap;
import com.github.piotrkot.takes.misc.Opt;
import com.github.piotrkot.takes.rs.RsText;
import com.github.piotrkot.takes.rs.RsWithStatus;
import java.net.HttpURLConnection;
import lombok.extern.slf4j.Slf4j;

/**
 * Fallback for web application.
 *
 * @since 1.0
 */
@Slf4j
public final class FbCustom extends FbWrap {

    /**
     * Ctor.
     */
    public FbCustom() {
        super(
            new FbChain(
                req -> {
                    log.error("{}", req.throwable().getCause());
                    return new Opt.Single<>(
                        new RsWithStatus(
                            new RsText("Bad request!"),
                            HttpURLConnection.HTTP_BAD_REQUEST
                        )
                    );
                }
            )
        );
    }
}
