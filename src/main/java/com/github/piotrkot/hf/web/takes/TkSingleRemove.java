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
import com.github.piotrkot.oojdbc.JdbcSession;
import com.github.piotrkot.oojdbc.Outcome;
import com.github.piotrkot.oojdbc.Sql;
import com.github.piotrkot.oojdbc.statements.Args;
import com.github.piotrkot.oojdbc.statements.Update;
import com.github.piotrkot.takes.Response;
import com.github.piotrkot.takes.facets.fork.RqRegex;
import com.github.piotrkot.takes.facets.fork.TkRegex;
import com.github.piotrkot.takes.rs.RsEmpty;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;

/**
 * Take which removes a marking of given category.
 *
 * @since 1.0
 */
@RequiredArgsConstructor
public final class TkSingleRemove implements TkRegex {
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
        new JdbcSession<>(
            new Update<>(
                new Sql("DELETE FROM bookings WHERE id_i = ? AND book_cat = ?::cat"),
                new Args(id, this.category.name()),
                Outcome.VOID
            )
        ).using(this.source);
        return new RsEmpty();
    }
}
