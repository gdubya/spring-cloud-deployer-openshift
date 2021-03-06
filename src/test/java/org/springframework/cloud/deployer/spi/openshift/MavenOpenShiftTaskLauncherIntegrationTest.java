package org.springframework.cloud.deployer.spi.openshift;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.deployer.resource.maven.MavenProperties;
import org.springframework.cloud.deployer.resource.maven.MavenResource;
import org.springframework.cloud.deployer.spi.task.TaskLauncher;
import org.springframework.cloud.deployer.spi.test.AbstractTaskLauncherIntegrationTests;
import org.springframework.cloud.deployer.spi.test.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableMap;

import io.fabric8.openshift.client.OpenShiftClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = { MavenOpenShiftTaskLauncherIntegrationTest.Config.class,
		OpenShiftAutoConfiguration.class })
public class MavenOpenShiftTaskLauncherIntegrationTest
		extends AbstractTaskLauncherIntegrationTests {

	@ClassRule
	public static OpenShiftTestSupport openShiftTestSupport = new OpenShiftTestSupport();

	@Autowired
	private OpenShiftClient openShiftClient;

	@Autowired
	private ResourceAwareOpenShiftTaskLauncher taskLauncher;

	@Override
	protected TaskLauncher provideTaskLauncher() {
		return taskLauncher;
	}

	@Test
	@Override
	@Ignore("Currently reported as completed instead of cancelled")
	public void testSimpleCancel() throws InterruptedException {
		super.testSimpleCancel();
	}

	@Override
	protected String randomName() {
		// Kubernetes app names must start with a letter and can only be 24 characters
		return "task-" + UUID.randomUUID().toString().substring(0, 18);
	}

	@Override
	protected Timeout deploymentTimeout() {
		return new Timeout(20, 5000);
	}

	@Override
	protected Resource testApplication() {
		Properties properties = new Properties();
		try {
			properties.load(new ClassPathResource("integration-test-app.properties")
					.getInputStream());
		}
		catch (IOException e) {
			throw new RuntimeException(
					"Failed to determine which version of integration-test-app to use",
					e);
		}
		return new MavenResource.Builder().groupId("org.springframework.cloud")
				.artifactId("spring-cloud-deployer-spi-test-app").classifier("exec")
				.version(properties.getProperty("version")).extension("jar").build();
	}

	@Configuration
	public static class Config {

		@Bean
		@ConfigurationProperties("maven")
		public MavenProperties mavenProperties() {
			MavenProperties mavenProperties = new MavenProperties();
			mavenProperties.setRemoteRepositories(
					ImmutableMap.of("maven.remote-repositories.spring.url",
							new MavenProperties.RemoteRepository(
									"http://repo.spring.io/libs-snapshot")));
			return mavenProperties;
		}

	}

}
