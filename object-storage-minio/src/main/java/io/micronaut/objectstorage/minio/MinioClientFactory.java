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
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.*;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import io.minio.credentials.Credentials;
import io.minio.credentials.MinioClientConfigProvider;
import jakarta.inject.Inject;
import okhttp3.OkHttpClient;

/**
 * <p>Creates beans of the following types:</p>
 * <ul>
 *     <li>For each {@link AzureBlobStorageConfiguration}, creates a {@link BlobServiceClientBuilder}.</li>
 *     <li>For each {@link BlobServiceClientBuilder}, creates a {@link BlobServiceClient}</li>
 *     <li>For each {@link BlobServiceClient}, creates a {@link BlobContainerClient}</li>
 * </ul>
 *
 * @author Pavol Gressa
 * @since 1.0
 */
@Factory
public class MinioClientFactory {

    private final BeanContext beanContext;

    public MinioClientFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @EachBean(MinioConfiguration.class)
    public MinioClient getClient(@Parameter String name, MinioConfiguration configuration) {

        return MinioClient.builder().endpoint("http://localhost:9000").credentials("minioadmin", "minioadmin").build();
    }

}
