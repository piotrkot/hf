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

import com.github.piotrkot.hf.Marking;
import com.github.piotrkot.hf.web.takes.TkAllShow;
import com.github.piotrkot.hf.web.takes.TkBookingAdd;
import com.github.piotrkot.hf.web.takes.TkBookingEdit;
import com.github.piotrkot.hf.web.takes.TkSingleRemove;
import com.github.piotrkot.hf.web.takes.TkSingleShow;
import com.github.piotrkot.takes.facets.fork.FkMethods;
import com.github.piotrkot.takes.facets.fork.FkRegex;
import com.github.piotrkot.takes.facets.fork.TkFork;
import com.github.piotrkot.takes.misc.Opt;
import com.github.piotrkot.takes.rq.RqMethod;
import com.github.piotrkot.takes.tk.TkWrap;
import javax.sql.DataSource;

/**
 * Take with bookings.
 *
 * @since 1.0
 */
public final class TkBookings extends TkWrap {
    /**
     * Ctor.
     * @param source Data source
     */
    public TkBookings(final DataSource source) {
        super(
            new TkFork(
                new FkMethods(
                    RqMethod.POST, new TkBookingAdd(source)
                ),
                new FkMethods(
                    RqMethod.PUT,
                    new TkFork(
                        new FkRegex(
                            ".*/(?<id>\\d+)", new TkBookingEdit(source)
                        )
                    )
                ),
                new FkMethods(
                    RqMethod.DELETE,
                    new TkFork(
                        new FkRegex(
                            ".*/(?<id>\\d+)", new TkSingleRemove(source, Marking.Category.BOOKING)
                        )
                    )
                ),
                new FkMethods(
                    RqMethod.GET,
                    new TkFork(
                        new FkRegex(
                            ".*/(?<id>\\d+)", new TkSingleShow(source, Marking.Category.BOOKING)
                        ),
                        new FkRegex(
                            ".*", new TkAllShow(source, new Opt.Single<>(Marking.Category.BOOKING))
                        )
                    )
                )
            )
        );
    }
}
