/*
 * The Closed-Source License
 *
 * Copyright (c) 2023 Fair-Kom.
 *
 * All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * The file is proprietary and confidential.
 */
package com.github.piotrkot.hf;

import com.github.piotrkot.json.Attr;
import com.github.piotrkot.json.JsonObj;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cactoos.Scalar;

/**
 * Marking object.
 *
 * @since 1.0
 */
@RequiredArgsConstructor
@Getter
public final class Marking implements Scalar<JsonObj> {
    /**
     * Date format.
     */
    private static final DateTimeFormatter FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

    /**
     * Identity.
     */
    private final int id;

    /**
     * Start date including.
     */
    private final LocalDate start;

    /**
     * End date excluding.
     */
    private final LocalDate end;

    /**
     * Category.
     */
    private final Category cat;

    /**
     * Ctor.
     * @param id Identity
     * @param start Start date
     * @param end End date
     * @param cat Category
     * @checkstyle ParameterNumber (2 lines)
     */
    public Marking(final int id, final LocalDate start, final LocalDate end, final String cat) {
        this(id, start, end, Category.valueOf(cat));
    }

    @Override
    public JsonObj value() {
        return new JsonObj(
            new Attr<>("id", this.id),
            new Attr<>("start", this.start.format(Marking.FORMAT)),
            new Attr<>("end", this.end.format(Marking.FORMAT)),
            new Attr<>("category", this.cat.name())
        );
    }

    /**
     * Category.
     */
    public enum Category {
        /**
         * User booking.
         */
        BOOKING,
        /**
         * Blocked for booking.
         */
        BLOCK

    }
}
