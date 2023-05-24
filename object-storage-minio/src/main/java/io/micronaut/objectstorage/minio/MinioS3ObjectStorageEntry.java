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
package io.micronaut.objectstorage.minio;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.objectstorage.ObjectStorageEntry;
import io.minio.GetObjectResponse;
import io.minio.StatObjectResponse;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * An {@link ObjectStorageEntry} implementation for AWS S3.
 *
 * @author Pavol Gressa
 * @since 1.0
 */
public class MinioS3ObjectStorageEntry implements ObjectStorageEntry<GetObjectResponse> {

    @NonNull
    private final String key;

    @NonNull
    private final GetObjectResponse responseInputStream;

    @NonNull final StatObjectResponse properties;

    public MinioS3ObjectStorageEntry(@NonNull String key,
                                     @NonNull GetObjectResponse responseInputStream) {
        this.responseInputStream = responseInputStream;
        //TODO: what should be the 4th parameter here?
        this.properties = new StatObjectResponse(responseInputStream.headers(), responseInputStream.bucket(), responseInputStream.region(), responseInputStream.object());
        this.key = key;
    }

    @Override
    @NonNull
    public String getKey() {
        return key;
    }

    @NonNull
    @Override
    public InputStream getInputStream() {
        return responseInputStream;
    }

    @NonNull
    @Override
    public GetObjectResponse getNativeEntry() {
        return responseInputStream;
    }

    @NonNull
    @Override
    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(this.properties.userMetadata());
    }

    @NonNull
    @Override
    public Optional<String> getContentType() {
        return Optional.ofNullable(properties.contentType());
    }
}
