package io.micronaut.objectstorage.minio

import io.micronaut.objectstorage.ObjectStorageOperations
import io.micronaut.objectstorage.ObjectStorageOperationsSpecification
import io.micronaut.test.support.TestPropertyProvider
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.ObjectWriteResponse
import io.minio.PutObjectArgs
import io.minio.RemoveBucketArgs
import jakarta.inject.Inject
import jakarta.inject.Named

import static io.micronaut.objectstorage.minio.MinioConfiguration.PREFIX

abstract class AbstractMinioSpec extends ObjectStorageOperationsSpecification implements TestPropertyProvider {

    public static final String BUCKET_NAME = System.currentTimeMillis()
    public static final String OBJECT_STORAGE_NAME = 'default'

    @Inject
    MinioClient s3

    @Inject
    @Named(OBJECT_STORAGE_NAME)
    MinioOperations awsS3Bucket

    void setup() {
        s3.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build() as MakeBucketArgs)
    }

    void cleanup() {
        s3.removeBucket(RemoveBucketArgs.builder().bucket(BUCKET_NAME).build() as RemoveBucketArgs)
    }

    @Override
    ObjectStorageOperations<PutObjectArgs.Builder, ObjectWriteResponse, Boolean> getObjectStorage() {
        return awsS3Bucket
    }

    @Override
    Map<String, String> getProperties() {
        [(PREFIX + '.' + OBJECT_STORAGE_NAME + '.bucket'): BUCKET_NAME]
    }
}
