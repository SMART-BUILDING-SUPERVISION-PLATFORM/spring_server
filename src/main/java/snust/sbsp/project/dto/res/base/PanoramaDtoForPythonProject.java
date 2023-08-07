package snust.sbsp.project.dto.res.base;

import lombok.Getter;
import snust.sbsp.project.domain.Panorama;

@Getter
public class PanoramaDtoForPythonProject {

	private final String src;

	public PanoramaDtoForPythonProject(
		Panorama panorama
	) {
		this.src = panorama.getSrc();
	}
}
