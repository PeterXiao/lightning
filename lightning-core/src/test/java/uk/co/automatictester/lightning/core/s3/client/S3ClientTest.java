package uk.co.automatictester.lightning.core.s3.client;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.findify.s3mock.S3Mock;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

public class S3ClientTest {

    private static final String REGION = "eu-west-2";
    private AmazonS3 amazonS3Client;
    private S3Mock s3Mock;
    private S3Client lightning3Client = S3Client.getMockedInstance(REGION);
    private String key;
    private String content;
    private String bucket;

    @BeforeClass
    public void setupEnv() {
        int port = 8001;

        String serviceEndpoint = String.format("http://localhost:%d", port);
        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, REGION);
        amazonS3Client = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpointConfiguration)
                .build();

        s3Mock = new S3Mock.Builder().withPort(port).withInMemoryBackend().build();
        s3Mock.start();
    }

    @AfterClass
    public void teardown() {
        s3Mock.stop();
    }

    @BeforeMethod
    public void initialiseTestConfig() {
        key = getRandomKey();
        content = getRandomContent();
        bucket = getRandomBucketName();
        amazonS3Client.createBucket(bucket);
        lightning3Client.setS3Bucket(bucket);
    }

    @AfterMethod
    public void deleteBucket() {
        amazonS3Client.deleteBucket(bucket);
    }

    @Test
    public void testGetObjectAsString() {
        amazonS3Client.putObject(bucket, key, content);
        String retrievedContent = lightning3Client.getObjectAsString(key);
        assertThat(retrievedContent, is(equalTo(content)));
    }

    @Test
    public void testPutObject() {
        String generatedKey = lightning3Client.putObject(key, content);
        String retrievedContent = amazonS3Client.getObjectAsString(bucket, generatedKey);
        assertThat(retrievedContent, is(equalTo(content)));
    }

    @Test
    public void testPutObjectFromFile() throws IOException {
        String file = "csv/jmeter/10_transactions.csv";
        lightning3Client.putObjectFromFile(file);
        String objectContent = amazonS3Client.getObjectAsString(bucket, file);
        String fileContent = readFileToString("/" + file);
        assertThat(objectContent, is(equalTo(fileContent)));
    }

    @Test
    public void testCreateBucketIfDoesNotExist() {
        String bucket = getRandomBucketName();
        boolean bucketCreated = lightning3Client.createBucketIfDoesNotExist(bucket);
        assertThat(bucketCreated, is(true));
    }

    @Test
    public void testDoNotCreateBucketIfExists() {
        String bucket = getRandomBucketName();
        lightning3Client.createBucketIfDoesNotExist(bucket);
        boolean bucketCreatedIfAlreadyExists = lightning3Client.createBucketIfDoesNotExist(bucket);
        assertThat(bucketCreatedIfAlreadyExists, is(false));
        assertThat(amazonS3Client.doesBucketExistV2(bucket), is(true));
    }

    @Test
    public void testGetRealInstance() {
        S3Client realClient = S3Client.getInstance(REGION);
    }

    private String getRandomKey() {
        return RandomStringUtils.randomAlphabetic(20);
    }

    private String getRandomContent() {
        return RandomStringUtils.randomAlphanumeric(100);
    }

    private String getRandomBucketName() {
        return RandomStringUtils.randomAlphabetic(50).toLowerCase();
    }

    private String readFileToString(String filePath) throws IOException {
        File file = new File(this.getClass().getResource(filePath).getFile());
        return FileUtils.readFileToString(file);
    }
}