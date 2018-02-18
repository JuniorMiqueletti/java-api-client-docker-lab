package com.juniormiqueletti.docker.api;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;

public class DockerAPISmokeTest {

	private static final String IMAGE_NAME = "busybox";
	private static final String IMAGE_NAME_TAG = "busybox:latest";
	private static final String DOCKER_WIN_IP = "tcp://192.168.99.100:2376";
	private static final String DOCKER_CONFIG = "C:\\Users\\user\\.docker\\machine\\machines\\default\\config.json";
	private static final String DOCKER_CERTIFICATE_PATH = "C:\\Users\\user\\.docker\\machine\\machines\\default";

	private DockerClient dockerClient;
	
	@Before
	public void setUp() {
		
		DockerClientConfig config = 
				DefaultDockerClientConfig.createDefaultConfigBuilder()
					.withDockerHost(DOCKER_WIN_IP)
					.withDockerTlsVerify(true)
					.withDockerCertPath(DOCKER_CERTIFICATE_PATH)
					.withDockerConfig(DOCKER_CONFIG)
						.build();

		dockerClient = DockerClientBuilder.getInstance(config).build();
	}
	
	@Test(expected = NotFoundException.class)
	public void containerNotFoundTest() {
		
		dockerClient.removeContainerCmd(IMAGE_NAME)
				.exec();
	}
	
	@Test
	public void ImageNotFoundIfTest() {
		
		List<Image> images = dockerClient
				.listImagesCmd()
				.withShowAll(true)
				.exec();
		
		long count = images
			.stream()
			.filter(i -> IMAGE_NAME_TAG.equals(i.getRepoTags()[0]))
			.count();
		
		assertTrue(count == 0);
	}
	
	@Test
	public void PullTest() throws InterruptedException {
		
		List<Image> images = dockerClient
				.listImagesCmd()
				.withShowAll(true)
				.exec();
		
		long count = images
				.stream()
				.filter(i -> IMAGE_NAME_TAG.equals(i.getRepoTags()[0]))
				.count();
		
		assertTrue(count == 0);
		
		dockerClient.pullImageCmd(IMAGE_NAME_TAG)
			.exec(new PullImageResultCallback())
			.awaitCompletion(10, TimeUnit.SECONDS);
		
		dockerClient
			.removeImageCmd(IMAGE_NAME_TAG)
			.withForce(true)
			.exec();
	}
	
	@Test
	public void pullAndRunTest() throws InterruptedException {
		
		try {
			dockerClient
				.removeImageCmd(IMAGE_NAME_TAG)
				.withForce(true)
				.exec();
	
		}catch (NotFoundException e) {
			// do nothing
		}
	
		dockerClient
			.pullImageCmd(IMAGE_NAME_TAG)
			.exec(new PullImageResultCallback())
			.awaitCompletion(10, TimeUnit.SECONDS);
		
		String newContainerName = IMAGE_NAME + "container";
		
		CreateContainerResponse container = 
				dockerClient.createContainerCmd(IMAGE_NAME_TAG)
				.withName(newContainerName)
				.exec();
		
		dockerClient.startContainerCmd(container.getId()).exec();
		
		try {
			dockerClient.stopContainerCmd(container.getId()).exec();
		}catch (NotModifiedException e) {
			//do nothing
		}

		dockerClient.removeContainerCmd(container.getId()).exec();
		
		dockerClient
			.removeImageCmd(IMAGE_NAME_TAG)
			.withForce(true)
			.exec();
	}
	
	@Test
	public void pullAndRunAndCommitTest() throws InterruptedException {
		
		try {
			dockerClient
				.removeImageCmd(IMAGE_NAME_TAG)
				.withForce(true)
				.exec();
	
		}catch (NotFoundException e) {
			// do nothing
		}
	
		dockerClient
			.pullImageCmd(IMAGE_NAME_TAG)
			.exec(new PullImageResultCallback())
			.awaitCompletion(10, TimeUnit.SECONDS);
		
		String newContainerName = IMAGE_NAME + "container";
		
		CreateContainerResponse container = 
				dockerClient
					.createContainerCmd(IMAGE_NAME_TAG)
					.withName(newContainerName)
					.exec();
		
		dockerClient.startContainerCmd(container.getId()).exec();
		
		//commit new image
		String newImageName = IMAGE_NAME + "container-new-image";
		
		dockerClient
			.commitCmd(container.getId())
				.withMessage("messageCommit")
				.withRepository(newImageName)
					.exec();
		
		//remove original container and image
		try {
			dockerClient.stopContainerCmd(container.getId()).exec();
		}catch (NotModifiedException e) {
			//do nothing
		}

		dockerClient.removeContainerCmd(container.getId()).exec();
		
		dockerClient
			.removeImageCmd(IMAGE_NAME_TAG)
			.withForce(true)
			.exec();
		
		//run new container over new Image
		CreateContainerResponse containerNew = 
				dockerClient
					.createContainerCmd(newImageName)
					.withName(newContainerName)
					.exec();
		
		//remove new container and image
		try {
			dockerClient.stopContainerCmd(containerNew.getId()).exec();
		}catch (NotModifiedException e) {
			//do nothing
		}

		dockerClient.removeContainerCmd(containerNew.getId()).exec();
		
		dockerClient
			.removeImageCmd(newImageName)
			.withForce(true)
			.exec();
	}
}
