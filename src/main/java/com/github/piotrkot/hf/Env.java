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

import java.io.IOException;

/**
 * Environment.
 *
 * @since 1.0
 */
public interface Env {
    /**
     * Reads environment variable.
     * @param name Name of the variable.
     * @return Variable value
     * @throws IOException When fails.
     */
    String variable(String name) throws IOException;

    /**
     * Runtime environment.
     * @since 1.0
     */
    final class Runtime implements Env {

        // @checkstyle NonStaticMethod (2 lines)
        @Override
        public String variable(final String name) throws IOException {
            if (System.getenv().containsKey(name)) {
                return System.getenv(name);
            }
            throw new IOException(
                String.format("Environment variable '%s' not set.", name)
            );
        }
    }
}
