package snust.sbsp.project.domain;

import lombok.*;
import snust.sbsp.company.domain.Company;
import snust.sbsp.project.domain.type.CtrType;
import snust.sbsp.project.domain.type.DetailCtrType;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity(name = "project")
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

  @OneToMany(mappedBy = "project")
  private List<Participant> ParticipantList = new ArrayList<>();
}
