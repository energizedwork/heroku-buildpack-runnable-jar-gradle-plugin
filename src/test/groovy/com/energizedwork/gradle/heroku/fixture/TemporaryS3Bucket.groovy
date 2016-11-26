/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
