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

import com.github.piotrkot.json.Attr;
import com.github.piotrkot.json.JsonObj;
import com.github.piotrkot.oojdbc.JdbcSessionTx;
import com.github.piotrkot.oojdbc.Outcome;
import com.github.piotrkot.oojdbc.Sql;
import com.github.piotrkot.oojdbc.statements.Args;
import com.github.piotrkot.oojdbc.statements.Insert;
import com.github.piotrkot.oojdbc.statements.Update;
import com.github.piotrkot.takes.Request;
import com.github.piotrkot.takes.Response;
import com.github.piotrkot.takes.Take;
import com.github.piotrkot.takes.rs.RsJson;
import com.github.piotrkot.takes.rs.RsWithStatus;
import java.net.HttpURLConnection;
import java.util.Collection;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.cactoos.iterable.Repeated;
import org.cactoos.text.Joined;

/**
 * Take which adds a block.
 *
 * @since 1.0
 */
@RequiredArgsConstructor
public final class TkBlockAdd implements Take {
    /**
     * Data source.
     */
    private final DataSource source;

    @Override
    public Response act(final Request req) throws Exception {
        final JsonObj body = new JsonObj(req.body());
        final String start = body.get("start");
        final String end = body.get("end");
        final Long inserted = new JdbcSessionTx<>(
            conn -> {
                new Insert<>(
                    new Sql(
                        "INSERT INTO bookings (start_d, end_d, book_cat)",
                        "SELECT ?::date, ?::date, 'BLOCK'::cat",
                        "WHERE NOT EXISTS(",
                        " SELECT id_i FROM bookings",
                        " WHERE book_cat = 'BOOKING'::cat",
                        " AND (",
                        "  (start_d <= ?::date AND end_d > ?::date)",
                        "  OR (start_d >= ?::date AND start_d < ?::date)",
                        " )",
                        ")"
                    ),
                    new Args(start, end, start, start, start, end),
                    Outcome.LAST_INSERT_ID
                ).using(conn);
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
                return new Insert<>(
                    new Sql(
                        "INSERT INTO bookings (start_d, end_d, book_cat)",
                        "VALUES (?, ?, 'BLOCK'::cat)"
                    ),
                    new Args(overlaping.getMin(), overlaping.getMax()),
                    Outcome.LAST_INSERT_ID
                ).using(conn);
            }
        ).using(this.source);
        return new RsWithStatus(
            new RsJson(
                new JsonObj(new Attr<>("id", inserted)).jsonValue()
            ),
            HttpURLConnection.HTTP_CREATED
        );
    }
}
