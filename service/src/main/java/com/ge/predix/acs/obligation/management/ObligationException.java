/*******************************************************************************
 * Copyright 2017 General Electric Company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.ge.predix.acs.obligation.management;

public class ObligationException extends RuntimeException {

    private static final long serialVersionUID = -1231913762426270378L;

    public ObligationException() {
        super();
    }

    public ObligationException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ObligationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ObligationException(final String message) {
        super(message);
    }

    public ObligationException(final Throwable cause) {
        super(cause);
    }

}
