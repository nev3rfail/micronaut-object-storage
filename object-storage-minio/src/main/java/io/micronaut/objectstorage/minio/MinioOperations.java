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

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.objectstorage.InputStreamMapper;
import io.micronaut.objectstorage.ObjectStorageException;
import io.micronaut.objectstorage.ObjectStorageOperations;
import io.micronaut.objectstorage.response.UploadResponse;
import io.micronaut.objectstorage.request.UploadRequest;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * AWS implementation of {@link ObjectStorageOperations}.
 *
 * @author Pavol Gressa
 * @since 1.0
 */
@EachBean(MinioConfiguration.class)
public class MinioOperations implements ObjectStorageOperations<
    PutObjectArgs.Builder, ObjectWriteResponse, Boolean> {

    private final MinioClient s3Client;
    private final MinioConfiguration configuration;
    private final InputStreamMapper inputStreamMapper;

    /**
     *
     * @param configuration AWS S3 Configuration
     * @param s3Client S3 Client
     * @param inputStreamMapper InputStream Mapper
     */
    public MinioOperations(@Parameter MinioConfiguration configuration,
                           MinioClient s3Client,
                           InputStreamMapper inputStreamMapper) {
        this.s3Client = s3Client;
        this.configuration = configuration;
        this.inputStreamMapper = inputStreamMapper;
    }

    @Override
    @NonNull
    public UploadResponse<ObjectWriteResponse> upload(@NonNull UploadRequest uploadRequest,
                                                      @NonNull Consumer<PutObjectArgs.Builder> requestConsumer) {
        PutObjectArgs.Builder objectRequest = getRequestBuilder(uploadRequest);
        Map<String, String> metadata = uploadRequest.getMetadata();
        objectRequest.userMetadata(metadata.isEmpty()?null:metadata);
        requestConsumer.accept(objectRequest);
        try {
            ObjectWriteResponse response = s3Client.putObject(objectRequest.build());
            return UploadResponse.of(uploadRequest.getKey(), response.etag(), response);
        } catch (ServerException | InsufficientDataException | ErrorResponseException |
                 IOException | NoSuchAlgorithmException |
                 InvalidResponseException | XmlParserException | InternalException |
                 InvalidKeyException e) {
            String msg = String.format("Error when trying to upload a file with key [%s] to Amazon S3", uploadRequest.getKey());
            throw new ObjectStorageException(msg, e);
        }
    }

    @Override
    @NonNull
    public UploadResponse<ObjectWriteResponse> upload(@NonNull UploadRequest request) {
        return upload(request, (obj -> {}));
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public Optional<MinioS3ObjectStorageEntry> retrieve(@NonNull String key) {
        try {
            GetObjectResponse responseInputStream = s3Client.getObject(GetObjectArgs.builder()
                .bucket(configuration.getBucket())
                .object(key)
                .build());
            MinioS3ObjectStorageEntry entry = new MinioS3ObjectStorageEntry(key, responseInputStream);
            return Optional.of(entry);
        } catch (ErrorResponseException e) {
            return Optional.empty();
        } catch (InsufficientDataException | InternalException |
            InvalidKeyException | InvalidResponseException | IOException |
            NoSuchAlgorithmException |ServerException | XmlParserException e) {
            String msg = String.format("Error when trying to retrieve a file with key [%s] from Amazon S3", key);
            throw new ObjectStorageException(msg, e);
        }
    }

    @Override
    @NonNull
    public Boolean delete(@NonNull String key) {
        try {
             s3Client.removeObject(RemoveObjectArgs.builder()
                .bucket(configuration.getBucket())
                .object(key)
                .build());
             return true;
        } catch (ServerException | InsufficientDataException | ErrorResponseException |
                 IOException | NoSuchAlgorithmException | InvalidKeyException |
                 InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(@NonNull String key) {
        try {
            s3Client.statObject(StatObjectArgs.builder()
                .bucket(configuration.getBucket())
                .object(key)
                .build());
            return true;
        } catch (ServerException | InsufficientDataException |
                 IOException | NoSuchAlgorithmException | InvalidKeyException |
                 InvalidResponseException | XmlParserException | InternalException e) {
            String msg = String.format("Error when trying to check the existence of a file with key [%s] in Amazon S3", key);
            throw new ObjectStorageException(msg, e);
        } catch (ErrorResponseException  e) {
            return false;
        }
    }

    @NonNull
    @Override
    public Set<String> listObjects() {
        return
            listObjects(c -> {})
                .map(Item::objectName).collect(Collectors.toSet());
    }

    public Stream<Item> listObjects(Consumer<ListObjectsArgs.Builder> args) {
        return StreamSupport.stream(listMinioObjects(args).spliterator(), false)
            .map(itemResult -> {
                try {
                    return itemResult.get();
                } catch (ErrorResponseException | XmlParserException | ServerException |
                         NoSuchAlgorithmException | IOException | InvalidResponseException |
                         InvalidKeyException | InternalException | InsufficientDataException e) {
                    String msg = String.format("Error when listing the objects of the bucket [%s] in Amazon S3", configuration.getBucket());
                    throw new ObjectStorageException(msg, e);
                }
            });//.collect(Collectors.toSet());
    }

    public Iterable<Result<Item>> listMinioObjects(@NotNull Consumer<ListObjectsArgs.Builder> args) {
        ListObjectsArgs.Builder builder = ListObjectsArgs.builder()
            .bucket(configuration.getBucket());
        args.accept(builder);
        return s3Client.listObjects(
            builder.build()
        );
    }




    @Override
    public void copy(@NonNull String sourceKey, @NonNull String destinationKey) {
        try {
            s3Client.copyObject(CopyObjectArgs.builder()
                .source(
                    CopySource.builder()
                        .bucket(configuration.getBucket()).object(sourceKey).build()
                )
                .bucket(configuration.getBucket())
                .object(sourceKey)
                .build());
        } catch (ServerException | InternalException | IOException | XmlParserException |
                 InvalidResponseException | InvalidKeyException | NoSuchAlgorithmException |
                 ErrorResponseException | InsufficientDataException e) {
            String msg = String.format("Error when trying to copy a file from key [%s] to key [%s] in Amazon S3", sourceKey, destinationKey);
            throw new ObjectStorageException(msg, e);
        }
    }

    /**
     * @param request the upload request
     * @return A Minio's {@link PutObjectArgs.Builder} from a Micronaut's {@link UploadRequest}.
     */
    protected PutObjectArgs.Builder getRequestBuilder(@NonNull UploadRequest request) {
        PutObjectArgs.Builder builder = PutObjectArgs.builder()
            .object(request.getKey());
        request.getContentType().ifPresent(builder::contentType);
        Optional<Long> size = request.getContentSize();

        if(size.isPresent()) {
            // TODO: Here we pass -1 so MinioClient will set the partSize automatically.
            //  Maybe we should add an option to chunk our upload?

            builder.stream(request.getInputStream(), size.get(),-1);
        } else {
            builder.stream(request.getInputStream(), -1,10485760);
        }

        return builder;
    }
}
