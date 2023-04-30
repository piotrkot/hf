/*
 * The Closed-Source License
 *
 * Copyright (c) 2023 Fair-Kom.
 *
 * All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * The file is proprietary and confidential.
 */
package com.github.piotrkot.hf.web.takes;

import com.github.piotrkot.oojdbc.JdbcSessionTx;
import com.github.piotrkot.oojdbc.Outcome;
import com.github.piotrkot.oojdbc.Sql;
import com.github.piotrkot.oojdbc.statements.Args;
import com.github.piotrkot.oojdbc.statements.Insert;
import com.github.piotrkot.oojdbc.statements.Update;
import com.github.piotrkot.takes.Request;
import com.github.piotrkot.takes.Response;
import com.github.piotrkot.takes.Take;
import com.github.piotrkot.takes.rq.RqHref;
import com.github.piotrkot.takes.rs.RsEmpty;
import java.sql.Date;
import java.util.Collection;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.cactoos.iterable.Repeated;
import org.cactoos.text.Joined;

/**
 * Take which removes a block by range.
 *
 * @since 1.0
 */
@RequiredArgsConstructor
public final class TkBlockRemoveByRange implements Take {
    /**
     * Data source.
     */
    private final DataSource source;

    @Override
    public Response act(final Request req) throws Exception {
        final RqHref.Smart href = new RqHref.Smart(req);
        final String start = href.single("start");
        final String end = href.single("end");
        new JdbcSessionTx<>(
            conn -> {
                final OverlappingBlocks.Overlap overlaping =
                    new OverlappingBlocks(start, end).findOverlaping(conn);
                final Collection<Long> ids = overlaping.getIds();
                new Update<>(
                    new Sql(
                        "DELETE FROM bookings WHERE id_i IN (",
                        new Joined(",", new Repeated<>(ids.size(), "?")).asString(),
                        ")"
                    ),
                    new Args(ids),
                    Outcome.VOID
                ).using(conn);
                if (overlaping.getMin().isBefore(Date.valueOf(start).toLocalDate())) {
                    new Insert<>(
                        new Sql(
                            "INSERT INTO bookings (start_d, end_d, book_cat)",
                            "VALUES (?::date, ?::date, 'BLOCK'::cat)"
                        ),
                        new Args(overlaping.getMin(), start),
                        Outcome.LAST_INSERT_ID
                    ).using(conn);
                }
                if (overlaping.getMax().isAfter(Date.valueOf(end).toLocalDate())) {
                    new Insert<>(
                        new Sql(
                            "INSERT INTO bookings (start_d, end_d, book_cat)",
                            "VALUES (?::date, ?::date, 'BLOCK'::cat)"
                        ),
                        new Args(end, overlaping.getMax()),
                        Outcome.LAST_INSERT_ID
                    ).using(conn);
                }
                return null;
            }
        ).using(this.source);
        return new RsEmpty();
    }
}
