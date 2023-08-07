package snust.sbsp.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import snust.sbsp.project.domain.Panorama;

import java.util.List;

public interface PanoramaRepository extends JpaRepository<Panorama, Long> {
	List<Panorama> findByProjectId(Long projectId);

	void deleteAllByProjectId(Long projectId);
}
