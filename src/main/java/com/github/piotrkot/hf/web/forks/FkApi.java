/*
 * The Closed-Source License
 *
 * Copyright (c) 2023 Fair-Kom.
 *
 * All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * The file is proprietary and confidential.
 */
package com.github.piotrkot.hf.web.forks;

import com.github.piotrkot.hf.web.takes.TkAllShow;
import com.github.piotrkot.takes.facets.fork.FkChain;
import com.github.piotrkot.takes.facets.fork.FkRegex;
import com.github.piotrkot.takes.facets.fork.FkWrap;
import com.github.piotrkot.takes.misc.Opt;
import javax.sql.DataSource;

/**
 * Fork with API web services.
 *
 * @since 1.0
 */
public final class FkApi extends FkWrap {
    /**
     * Ctor.
     * @param source Data source
     */
    public FkApi(final DataSource source) {
        super(
            new FkChain(
                new FkRegex("/", new TkAllShow(source, new Opt.Empty<>())),
                new FkRegex("/bookings(/\\d+)?", new TkBookings(source)),
                new FkRegex("/blocks(/\\d+)?", new TkBlocks(source))
            )
        );
    }
}
