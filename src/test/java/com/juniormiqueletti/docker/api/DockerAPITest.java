package com.juniormiqueletti.docker.api;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.SearchItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

public class DockerAPITest {

	@Test
	public void basicWindowsTest() {

		DockerClientConfig config = 
			DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost("tcp://192.168.99.100:2376").withDockerTlsVerify(true)
				.withDockerCertPath("C:\\Users\\user\\.docker\\machine\\machines\\default")
				.withDockerConfig("C:\\Users\\user\\.docker\\machine\\machines\\default\\config.json")
					.build();

		DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

		CreateContainerResponse container = 
				dockerClient.createContainerCmd("busybox")
				.withCmd("touch", "/test")
					.exec();

		List<SearchItem> dockerSearch = 
				dockerClient.searchImagesCmd("busybox")
				.exec();
		Assert.assertTrue(!dockerSearch.isEmpty());
	}
}
