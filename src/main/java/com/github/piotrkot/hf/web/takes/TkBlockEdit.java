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

import com.github.piotrkot.json.JsonObj;
import com.github.piotrkot.oojdbc.JdbcSessionTx;
import com.github.piotrkot.oojdbc.Outcome;
import com.github.piotrkot.oojdbc.Sql;
import com.github.piotrkot.oojdbc.statements.Args;
import com.github.piotrkot.oojdbc.statements.Insert;
import com.github.piotrkot.oojdbc.statements.Update;
import com.github.piotrkot.takes.Response;
import com.github.piotrkot.takes.facets.fork.RqRegex;
import com.github.piotrkot.takes.facets.fork.TkRegex;
import com.github.piotrkot.takes.rs.RsWithStatus;
import java.net.HttpURLConnection;
import java.util.Collection;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.cactoos.iterable.Repeated;
import org.cactoos.text.Joined;

/**
 * Take which edits a block.
 *
 * @since 1.0
 */
@RequiredArgsConstructor
public final class TkBlockEdit implements TkRegex {
    /**
     * Data source.
     */
    private final DataSource source;

    @Override
    public Response act(final RqRegex reqx) throws Exception {
        final int id = Integer.parseInt(reqx.matcher().group("id"));
        final JsonObj body = new JsonObj(reqx.body());
        final String start = body.get("start");
        final String end = body.get("end");
        new JdbcSessionTx<>(
            conn -> {
                new Update<>(
                    new Sql(
                        "UPDATE bookings SET start_d = ?::date, end_d = ?::date",
                        "WHERE id_i = ? AND book_cat = 'BLOCK'::cat AND NOT EXISTS(",
                        " SELECT id_i FROM bookings",
                        " WHERE book_cat = 'BOOKING'::cat",
                        " AND (",
                        "  (start_d <= ?::date AND end_d > ?::date)",
                        "  OR (start_d >= ?::date AND start_d < ?::date)",
                        " )",
                        ")"
                    ),
                    new Args(start, end, id, start, start, start, end),
                    Outcome.VOID
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
                new Insert<>(
                    new Sql(
                        "INSERT INTO bookings (start_d, end_d, book_cat)",
                        "VALUES (?, ?, 'BLOCK'::cat)"
                    ),
                    new Args(overlaping.getMin(), overlaping.getMax()),
                    Outcome.LAST_INSERT_ID
                ).using(conn);
                return null;
            }
        ).using(this.source);
        return new RsWithStatus(HttpURLConnection.HTTP_OK);
    }
}
