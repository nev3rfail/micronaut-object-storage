package io.micronaut.objectstorage.minio

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.objectstorage.ObjectStorageOperations
import io.micronaut.objectstorage.ObjectStorageOperationsSpecification
import io.micronaut.objectstorage.request.UploadRequest
import io.micronaut.objectstorage.minio.MinioConfiguration
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.minio.MinioAsyncClient
import io.minio.MinioClient
import io.minio.ObjectWriteResponse
import io.minio.PutObjectArgs
import io.minio.errors.ErrorResponseException
import io.minio.errors.InsufficientDataException
import io.minio.errors.InternalException
import io.minio.errors.InvalidResponseException
import io.minio.errors.ServerException
import io.minio.errors.XmlParserException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.Specification

import java.lang.reflect.Constructor
import java.nio.file.Path
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException

@Property(name = "micronaut.object-storage.minio.default.bucket", value = "profile-pictures-bucket")
@Property(name = "spec.name", value = SPEC_NAME)
@MicronautTest
class MinioOperationsUploadWithConsumerSpec extends Specification {

    private static final String SPEC_NAME = "MinioOperationsUploadWithConsumerSpec"

    @Inject
    ObjectStorageOperations<PutObjectArgs.Builder, ObjectWriteResponse, Boolean> objectStorage

    @Inject
    S3ClientReplacement s3ClientReplacement

    void "consumer accept is invoked"() {
        given:
        Path path = ObjectStorageOperationsSpecification.createTempFile()
        UploadRequest uploadRequest = UploadRequest.fromPath(path)

        when:
        objectStorage.upload(uploadRequest, builder -> {
            builder.contentType("Testing")
        });

        then:
        s3ClientReplacement.request
        "Testing" == s3ClientReplacement.request.contentType()
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @Replaces(MinioClient)
    @Singleton
    static class S3ClientReplacement extends MinioClient {
        PutObjectArgs request

        S3ClientReplacement() {
            super(crutchInstance())

        }

        private static MinioClient crutchInstance() {
            Constructor<MinioClient> constructor = MinioClient.class.getDeclaredConstructor(MinioAsyncClient.class);
            constructor.setAccessible(true);
            return constructor.newInstance(MinioAsyncClient.builder().endpoint("http://localhost:9000").credentials("minioadmin", "minioadmin").build());
        }

        @Override
        ObjectWriteResponse putObject(PutObjectArgs putObjectRequest)
                throws ServerException, InsufficientDataException, ErrorResponseException,
        IOException, NoSuchAlgorithmException,
        InvalidResponseException, XmlParserException, InternalException,
                        InvalidKeyException {
            System.out.println("hehehehehe");
            this.request = putObjectRequest
            return new ObjectWriteResponse(null, null, null, null, "eTag", null)
        }
    }
}
