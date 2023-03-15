package snust.sbsp.project.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
import lombok.*;
import snust.sbsp.company.domain.Company;
import snust.sbsp.project.domain.type.CtrType;
import snust.sbsp.project.domain.type.DetailCtrType;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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
}
