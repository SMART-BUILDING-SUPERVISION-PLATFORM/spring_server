package snust.sbsp.project.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import snust.sbsp.company.domain.Company;
import snust.sbsp.project.domain.type.CtrType;
import snust.sbsp.project.domain.type.DetailCtrType;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "project")
public class Project {
  @JsonIgnore
  @OneToMany(mappedBy = "project")
  List<Participant> ParticipantList;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @NotNull
  @Column(name = "name")
  private String name;
  @NotNull
  @Column(name = "start_date")
  private LocalDate startDate;
  @NotNull
  @Column(name = "end_date")
  private LocalDate endDate;
  @NotNull
  @Column(name = "process_rate")
  private Integer processRate;
  @NotNull
  @Column(name = "ctr_type")
  @Enumerated(value = EnumType.STRING)
  private CtrType ctrType;
  @NotNull
  @Column(name = "detail_ctr_type")
  @Enumerated(value = EnumType.STRING)
  private DetailCtrType detailCtrType;
  @NotNull
  @Column(name = "thumbnail_url")
  private String thumbnailUrl;
  @NotNull
  @Column(name = "floor_url")
  private String floorUrl;
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id")
  private Company company;

  @Builder
  public Project(
    String name,
    LocalDate startDate,
    LocalDate endDate,
    Integer processRate,
    CtrType ctrType,
    DetailCtrType detailCtrType,
    String thumbnailUrl,
    String floorUrl,
    Company company
  ) {
    this.name = name;
    this.startDate = startDate;
    this.endDate = endDate;
    this.processRate = processRate;
    this.ctrType = ctrType;
    this.detailCtrType = detailCtrType;
    this.thumbnailUrl = thumbnailUrl;
    this.floorUrl = floorUrl;
    this.company = company;
  }
}
