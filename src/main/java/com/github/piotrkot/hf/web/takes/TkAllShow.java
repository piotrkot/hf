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
import com.github.piotrkot.oojdbc.statements.Select;
import com.github.piotrkot.takes.Request;
import com.github.piotrkot.takes.Response;
import com.github.piotrkot.takes.Take;
import com.github.piotrkot.takes.misc.Opt;
import com.github.piotrkot.takes.rs.RsJson;
import com.github.piotrkot.takes.rs.RsWithStatus;
import java.net.HttpURLConnection;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;

/**
 * Take which shows all markings of given category.
 *
 * @since 1.0
 */
@RequiredArgsConstructor
public final class TkAllShow implements Take {
    /**
     * Data source.
     */
    private final DataSource source;

    /**
     * Marking category.
     */
    private final Opt<Marking.Category> category;

    @Override
    public Response act(final Request req) throws Exception {
        final String condition;
        if (this.category.has()) {
            condition = String.format(
                "WHERE book_cat = '%s'::cat", this.category.get().name()
            );
        } else {
            condition = "";
        }
        final List<JsonObj> found = new JdbcSession<>(
            new Select<>(
                new Sql(
                    "SELECT id_i, start_d, end_d, book_cat FROM bookings",
                    condition,
                    "ORDER BY start_d"
                ),
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
