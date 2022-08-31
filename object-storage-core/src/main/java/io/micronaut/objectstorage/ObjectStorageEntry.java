/*
 * Copyright 2017-2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.objectstorage;

import io.micronaut.core.annotation.NonNull;

import java.io.InputStream;

/**
 * @author Pavol Gressa
 * @since 1.0
 */
public interface ObjectStorageEntry {

    /**
     * The object path on object storage. For example {@code /path/to}
     *
     * @return object path or empty string if the object is placed at the root of bucket
     */
    @NonNull
    String getKey();

    /**
     * The object content.
     *
     * @return object content.
     */
    InputStream getInputStream();
}