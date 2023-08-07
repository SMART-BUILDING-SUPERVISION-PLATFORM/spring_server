package snust.sbsp.project.domain;

import lombok.*;
import snust.sbsp.company.domain.Company;
import snust.sbsp.project.domain.type.CtrType;
import snust.sbsp.project.domain.type.DetailCtrType;
import snust.sbsp.project.dto.req.ProjectReq;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Entity(name = "project")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Column(name = "process_rate")
	private int processRate;

	@Column(name = "ctr_type")
	@Enumerated(value = EnumType.STRING)
	private CtrType ctrType;

	@Column(name = "detail_ctr_type")
	@Enumerated(value = EnumType.STRING)
	private DetailCtrType detailCtrType;

	@Column(name = "thumbnail_url")
	private String thumbnailUrl;

	@Column(name = "floor_url")
	private String floorUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private Company company;

	@OneToMany(mappedBy = "project", orphanRemoval = true, cascade = CascadeType.PERSIST)
	private List<Participant> ParticipantList = new ArrayList<>();

	@OneToMany(mappedBy = "project", orphanRemoval = true, cascade = CascadeType.PERSIST)
	private List<Note> noteList = new ArrayList<>();

	// maybe unused
	@OneToMany(mappedBy = "project", orphanRemoval = true, cascade = CascadeType.PERSIST)
	private List<Panorama> panoramaList = new ArrayList<>();

	public void update(ProjectReq projectReq) {
		this.name = projectReq.getName();
		this.startDate = projectReq.getStartDate();
		this.endDate = projectReq.getEndDate();
		this.ctrType = CtrType.from(projectReq.getCtrType());
		this.detailCtrType = DetailCtrType.from(projectReq.getDetailCtrType());
		this.thumbnailUrl = projectReq.getThumbnailUrl();
		this.floorUrl = projectReq.getFloorPlanUrl();
	}
}
