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

import com.github.piotrkot.hf.Marking;
import com.github.piotrkot.json.JsonArr;
import com.github.piotrkot.json.JsonObj;
import com.github.piotrkot.oojdbc.JdbcSession;
import com.github.piotrkot.oojdbc.Sql;
import com.github.piotrkot.oojdbc.outcomes.ListOutcome;
import com.github.piotrkot.oojdbc.statements.Args;
import com.github.piotrkot.oojdbc.statements.Select;
import com.github.piotrkot.takes.Response;
import com.github.piotrkot.takes.facets.fork.RqRegex;
import com.github.piotrkot.takes.facets.fork.TkRegex;
import com.github.piotrkot.takes.rs.RsJson;
import com.github.piotrkot.takes.rs.RsWithStatus;
import java.net.HttpURLConnection;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;

/**
 * Take which shows a single marking.
 *
 * @since 1.0
 */
@RequiredArgsConstructor
public final class TkSingleShow implements TkRegex {
    /**
     * Data source.
     */
    private final DataSource source;

    /**
     * Marking category.
     */
    private final Marking.Category category;

    @Override
    public Response act(final RqRegex reqx) throws Exception {
        final int id = Integer.parseInt(reqx.matcher().group("id"));
        final List<JsonObj> found = new JdbcSession<>(
            new Select<>(
                new Sql(
                    "SELECT id_i, start_d, end_d, book_cat FROM bookings",
                    "WHERE id_i = ? AND book_cat = ?::cat"
                ),
                new Args(id, this.category.name()),
                new ListOutcome<>(
                    rset -> new Marking(
                        rset.getInt("id_i"),
                        rset.getDate("start_d").toLocalDate(),
                        rset.getDate("end_d").toLocalDate(),
                        rset.getString("book_cat")
                    ).value()
                )
            )
        ).using(this.source);
        return new RsWithStatus(
            new RsJson(
                new JsonArr<>(found).jsonValue()
            ),
            HttpURLConnection.HTTP_OK
        );
    }
}
