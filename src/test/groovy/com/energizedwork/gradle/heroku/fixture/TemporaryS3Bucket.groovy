package com.energizedwork.gradle.heroku.fixture

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.Bucket
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion
import com.amazonaws.services.s3.model.PutObjectRequest
import org.junit.rules.ExternalResource

import static com.amazonaws.services.s3.model.CannedAccessControlList.PublicRead

class TemporaryS3Bucket extends ExternalResource {

    private final String bucketName = UUID.randomUUID()

    private final AmazonS3 s3

    private Bucket bucket

    TemporaryS3Bucket(String awsAccessKey, String awsSecretKey) {
        s3 = new AmazonS3Client(new BasicAWSCredentials(awsAccessKey, awsSecretKey))
    }

    protected void before() throws Throwable {
        bucket = s3.createBucket(bucketName)
    }

    protected void after() {
        if (bucket) {
            clearBucket()
            s3.deleteBucket(bucketName)
        }
    }

    private void clearBucket() {
        List<KeyVersion> keys = s3.listObjects(bucketName).objectSummaries.collect { new KeyVersion(it.key) }
        if (keys) {
            def deleteRequest = new DeleteObjectsRequest(bucketName)
            s3.deleteObjects(deleteRequest.withKeys(keys))
        }
    }

    String uploadPublicly(File file) {
        def key = file.name
        s3.putObject(new PutObjectRequest(bucketName, key, file).withCannedAcl(PublicRead))
        s3.getUrl(bucketName, key)
    }
}
