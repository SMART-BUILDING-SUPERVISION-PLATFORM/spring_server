package snust.sbsp.project.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import snust.sbsp.company.domain.Company;
import snust.sbsp.project.domain.type.CtrType;
import snust.sbsp.project.domain.type.DetailCtrType;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Project {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String name;

    private LocalDate start_date;

    private LocalDate end_date;

    private Integer process_rate;

    @Enumerated(value = EnumType.STRING)
    private CtrType ctrType;

    @Enumerated(value = EnumType.STRING)
    private DetailCtrType detailCtrType;

    private String thumbnail_url;

    private String floor_url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
}
