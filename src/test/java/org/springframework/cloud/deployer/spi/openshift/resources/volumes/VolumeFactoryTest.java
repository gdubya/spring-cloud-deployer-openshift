package org.springframework.cloud.deployer.spi.openshift.resources.volumes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.HostPathVolumeSource;
import io.fabric8.kubernetes.api.model.HostPathVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.NFSVolumeSource;
import io.fabric8.kubernetes.api.model.NFSVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSource;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import org.junit.Test;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.cloud.deployer.spi.openshift.OpenShiftDeployerProperties;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class VolumeFactoryTest {

	private VolumeFactory volumeFactory;

	@Test
	public void addVolumeMounts() {
		OpenShiftDeployerProperties properties = new OpenShiftDeployerProperties();
		properties.setVolumes(ImmutableList.of(new VolumeBuilder().withName("testpvc")
				.withNewPersistentVolumeClaim("testClaim", true).build()));
		volumeFactory = new VolumeFactory(properties);
		AppDeploymentRequest request = new AppDeploymentRequest(
				new AppDefinition("testapp-source", null),
				//@formatter:off
				mock(Resource.class), ImmutableMap.of("spring.cloud.deployer.openshift.deployment.volumes",
					"["
						+ "{name: testhostpath, hostPath: { path: '/test/override/hostPath' }},"
						+ "{name: 'testnfs', nfs: { server: '192.168.1.1:111', path: '/test/override/nfs' }} "
					+ "]"));
				//@formatter:on

		List<Volume> volumes = volumeFactory.addObject(request, "1");

		HostPathVolumeSource testhostpath = new HostPathVolumeSourceBuilder().build();
		testhostpath.setPath("/test/override/hostPath");
		Volume hostPathVolume = new Volume();
		hostPathVolume.setName("testhostpath");
		hostPathVolume.setHostPath(testhostpath);

		PersistentVolumeClaimVolumeSource testpvc = new PersistentVolumeClaimVolumeSourceBuilder()
				.build();
		testpvc.setClaimName("testClaim");
		testpvc.setReadOnly(true);
		Volume persistentVolume = new Volume();
		persistentVolume.setName("testpvc");
		persistentVolume.setPersistentVolumeClaim(testpvc);

		NFSVolumeSource testnfs = new NFSVolumeSourceBuilder().build();
		testnfs.setPath("/test/override/nfs");
		testnfs.setServer("192.168.1.1:111");
		Volume nfsVolume = new Volume();
		nfsVolume.setName("testnfs");
		nfsVolume.setNfs(testnfs);

		assertThat(volumes).containsOnly(hostPathVolume, persistentVolume, nfsVolume);
	}

}
