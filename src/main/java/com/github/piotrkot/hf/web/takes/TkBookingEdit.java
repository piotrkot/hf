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
import com.github.piotrkot.oojdbc.JdbcSession;
import com.github.piotrkot.oojdbc.Outcome;
import com.github.piotrkot.oojdbc.Sql;
import com.github.piotrkot.oojdbc.statements.Args;
import com.github.piotrkot.oojdbc.statements.Update;
import com.github.piotrkot.takes.Response;
import com.github.piotrkot.takes.facets.fork.RqRegex;
import com.github.piotrkot.takes.facets.fork.TkRegex;
import com.github.piotrkot.takes.rs.RsJson;
import com.github.piotrkot.takes.rs.RsWithStatus;
import java.net.HttpURLConnection;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;

/**
 * Take which edits a booking.
 *
 * @since 1.0
 */
@RequiredArgsConstructor
public final class TkBookingEdit implements TkRegex {
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
        final Long updated = new JdbcSession<>(
            new Update<>(
                new Sql(
                    "UPDATE bookings SET start_d = ?::date, end_d = ?::date",
                    "WHERE id_i = ? AND book_cat = 'BOOKING'::cat AND NOT EXISTS(",
                    " SELECT id_i FROM bookings",
                    " WHERE id_i != ? AND (",
                    "  (start_d <= ?::date AND end_d > ?::date)",
                    "  OR (start_d >= ?::date AND start_d < ?::date)",
                    " )",
                    ")"
                ),
                new Args(start, end, id, id, start, start, start, end),
                Outcome.LAST_INSERT_ID
            )
        ).using(this.source);
        return new RsWithStatus(
            new RsJson(
                new JsonObj(new Attr<>("id", updated)).jsonValue()
            ),
            HttpURLConnection.HTTP_OK
        );
    }
}
