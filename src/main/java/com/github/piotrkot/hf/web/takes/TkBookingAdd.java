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
import com.github.piotrkot.oojdbc.statements.Insert;
import com.github.piotrkot.takes.Request;
import com.github.piotrkot.takes.Response;
import com.github.piotrkot.takes.Take;
import com.github.piotrkot.takes.rs.RsJson;
import com.github.piotrkot.takes.rs.RsWithStatus;
import java.net.HttpURLConnection;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;

/**
 * Take which adds a booking.
 *
 * @since 1.0
 */
@RequiredArgsConstructor
public final class TkBookingAdd implements Take {
    /**
     * Data source.
     */
    private final DataSource source;

    @Override
    public Response act(final Request req) throws Exception {
        final JsonObj body = new JsonObj(req.body());
        final String start = body.get("start");
        final String end = body.get("end");
        final Long inserted = new JdbcSession<>(
            new Insert<>(
                new Sql(
                    "INSERT INTO bookings (start_d, end_d, book_cat)",
                    "SELECT ?::date, ?::date, 'BOOKING'::cat",
                    "WHERE NOT EXISTS(",
                    " SELECT id_i FROM bookings",
                    " WHERE (start_d <= ?::date AND end_d > ?::date)",
                    "  OR (start_d >= ?::date AND start_d < ?::date)",
                    ")"
                ),
                new Args(start, end, start, start, start, end),
                Outcome.LAST_INSERT_ID
            )
        ).using(this.source);
        return new RsWithStatus(
            new RsJson(
                new JsonObj(new Attr<>("id", inserted)).jsonValue()
            ),
            HttpURLConnection.HTTP_CREATED
        );
    }
}
