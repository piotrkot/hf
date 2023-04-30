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

import com.github.piotrkot.oojdbc.Sql;
import com.github.piotrkot.oojdbc.outcomes.ColumnOutcome;
import com.github.piotrkot.oojdbc.outcomes.SingleOutcome;
import com.github.piotrkot.oojdbc.statements.Args;
import com.github.piotrkot.oojdbc.statements.Select;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Overlapping blocks.
 *
 * @since 1.0
 */
@RequiredArgsConstructor
public final class OverlappingBlocks {
    /**
     * Start date.
     */
    private final String start;

    /**
     * End date.
     */
    private final String end;

    /**
     * Find overlapping.
     * @param conn SQL connection
     * @return Overlapping data.
     * @throws Exception When fails
     */
    public Overlap findOverlaping(final Connection conn) throws Exception {
        final Date min = new Select<>(
            new Sql(
                "WITH overlapping AS (",
                " SELECT id_i, start_d, end_d",
                "  FROM bookings",
                "  WHERE book_cat = 'BLOCK'::cat",
                "  AND (",
                "   (start_d <= ?::date AND end_d > ?::date)",
                "   OR (start_d >= ?::date AND start_d < ?::date)",
                "  )",
                ")",
                "SELECT min(start_d) FROM overlapping"
            ),
            new Args(this.start, this.start, this.start, this.end),
            new SingleOutcome<>(Date.class)
        ).using(conn);
        final Date max = new Select<>(
            new Sql(
                "WITH overlapping AS (",
                " SELECT id_i, start_d, end_d",
                "  FROM bookings",
                "  WHERE book_cat = 'BLOCK'::cat",
                "  AND (",
                "   (start_d <= ?::date AND end_d > ?::date)",
                "   OR (start_d >= ?::date AND start_d < ?::date)",
                "  )",
                ")",
                "SELECT max(end_d) FROM overlapping"
            ),
            new Args(this.start, this.start, this.start, this.end),
            new SingleOutcome<>(Date.class)
        ).using(conn);
        final Collection<Long> ids = new Select<>(
            new Sql(
                "WITH overlapping AS (",
                " SELECT id_i, start_d, end_d",
                "  FROM bookings",
                "  WHERE book_cat = 'BLOCK'::cat",
                "  AND (",
                "   (start_d <= ?::date AND end_d > ?::date)",
                "   OR (start_d >= ?::date AND start_d < ?::date)",
                "  )",
                ")",
                "SELECT id_i FROM overlapping"
            ),
            new Args(this.start, this.start, this.start, this.end),
            new ColumnOutcome<>(Long.class)
        ).using(conn);
        return new Overlap(
            java.sql.Date.valueOf(min.toString()).toLocalDate(),
            java.sql.Date.valueOf(max.toString()).toLocalDate(),
            ids
        );
    }

    /**
     * Overlap.
     * @since 1.0
     */
    @RequiredArgsConstructor
    @Getter
    static final class Overlap {
        /**
         * Minimal date of the overlapping blocks.
         * @checkstyle ConstantUsage (2 lines)
         */
        private final LocalDate min;

        /**
         * Maximal date of the overlapping blocks.
         * @checkstyle ConstantUsage (2 lines)
         */
        private final LocalDate max;

        /**
         * Collection of block identities that overlap.
         * @checkstyle ConstantUsage (2 lines)
         */
        private final Collection<Long> ids;
    }
}
