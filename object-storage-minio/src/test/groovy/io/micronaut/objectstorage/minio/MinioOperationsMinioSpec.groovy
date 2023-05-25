package io.micronaut.objectstorage.minio

import io.micronaut.objectstorage.minio.util.MinioContainer
import io.micronaut.objectstorage.minio.util.MinioContainer.CredentialsProvider;
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import io.minio.credentials.Credentials
import spock.lang.AutoCleanup
import spock.lang.IgnoreIf
import spock.lang.Shared

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3

@MicronautTest
@IgnoreIf({ env.AWS_ACCESS_KEY_ID && env.AWS_SECRET_ACCESS_KEY && env.AWS_REGION })
class MinioOperationsMinioSpec extends AbstractMinioSpec implements TestPropertyProvider {

    @Shared
    //@AutoCleanup
    public credentials = new CredentialsProvider()

    @Shared
    @AutoCleanup
    public MinioContainer minio = new MinioContainer(null, credentials)

    @Override
    Map<String, String> getProperties() {
        minio.start()
        Object.getProperties() + [
                'micronaut.object-storage.minio.default.accessKeyId'         : credentials.accessKey,
                'micronaut.object-storage.minio.default.secretKey'           : credentials.secretKey,
                'micronaut.object-storage.minio.default.bucket'           : BUCKET_NAME,
                'micronaut.object-storage.minio.default.region'              : "eu-north-1",
                'micronaut.object-storage.minio.default.s3.endpoint-override': "http://"+minio.getHostAddress()
        ] as Map
    }
}
